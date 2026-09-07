#!/usr/bin/env bash
# PreToolUse hook: token guard for Read and Bash.
# Adapted from Spotify's Portal write-up (engineering.atspotify.com, 2026-09): most
# context-window burn is whole-file dumps the model never needed. Instead of an
# external cheap-model CLI, we redirect to what Claude Code already has —
# Grep + Read(offset/limit) for slices, or a haiku Explore subagent for surveys
# (see .claude/skills/bulk-read/SKILL.md).
#
# Blocks:
#   1. Read of a text file over $TOKEN_GUARD_MAX_LINES lines with no `limit` set
#   2. Bash `cat <files>` (no pipe/redirect — i.e. a pure context dump) over the same threshold
#
# Escape hatch: a Read with an explicit `limit` always passes — a full rewrite that
# genuinely needs the whole file just asks for it deliberately.
# Threshold override: "env" in .claude/settings.json or settings.local.json.

set -uo pipefail

INPUT=$(cat)
MAX=${TOKEN_GUARD_MAX_LINES:-350}

deny() {
    jq -nc --arg r "$1" \
        '{hookSpecificOutput:{hookEventName:"PreToolUse",permissionDecision:"deny",permissionDecisionReason:$r}}'
    exit 0
}

guidance() {
    local file="$1" lines="$2"
    printf '%s' "token-guard: $file has $lines lines (threshold $MAX). Don't dump it whole into context. Instead: (a) Grep for the symbol you need, then Read just that slice with offset+limit; (b) for a summary or multi-file survey, use the bulk-read skill (haiku Explore subagent); (c) if you truly need the entire file (e.g. before a full rewrite), call Read with an explicit limit >= $lines."
}

TOOL=$(jq -r '.tool_name // empty' <<<"$INPUT")

is_binary() {
    printf '%s' "$1" | grep -qiE '\.(png|jpe?g|gif|webp|ico|pdf|jar|class|zip|gz|tar|bin|so|dylib|woff2?)$'
}

case "$TOOL" in
Read)
    FILE=$(jq -r '.tool_input.file_path // empty' <<<"$INPUT")
    LIMIT=$(jq -r '.tool_input.limit // empty' <<<"$INPUT")
    [[ -n "$LIMIT" ]] && exit 0
    [[ -f "$FILE" ]] || exit 0
    is_binary "$FILE" && exit 0
    LINES=$(wc -l <"$FILE" | tr -d ' ')
    ((LINES > MAX)) && deny "$(guidance "$FILE" "$LINES")"
    ;;
Bash)
    CMD=$(jq -r '.tool_input.command // empty' <<<"$INPUT")
    # Only the pure-dump form: `cat file [file...]` with no pipe/redirect/chaining.
    printf '%s' "$CMD" | grep -qE '^[[:space:]]*(cat|bat)[[:space:]]+[^|;&<>]+$' || exit 0
    for f in $(printf '%s' "$CMD" | sed -E 's/^[[:space:]]*(cat|bat)[[:space:]]+//'); do
        [[ "$f" == -* ]] && continue
        [[ -f "$f" ]] || continue
        is_binary "$f" && continue
        LINES=$(wc -l <"$f" | tr -d ' ')
        ((LINES > MAX)) && deny "$(guidance "$f" "$LINES") For shell use, pipe through head/grep instead of dumping it."
    done
    ;;
esac

exit 0
