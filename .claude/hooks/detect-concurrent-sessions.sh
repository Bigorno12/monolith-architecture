#!/usr/bin/env bash
# SessionStart hook: detect other live Claude Code sessions in this same folder
# and nudge the agent to isolate its work in a git worktree before editing.
#
# Why: multiple concurrent Claude Code sessions in one checkout race on the
# working tree and git index — concurrent edits, overwritten files, half-staged
# commits. Catching it at SessionStart lets the agent call EnterWorktree first.
#
# Strategy: per-cwd lock file under ~/.claude/locks/, keyed by hash($PWD).
# Each file holds one PID per line. On every SessionStart we:
#   1. read existing PIDs, keep only those still alive (kill -0)
#   2. if any live one is NOT us, emit an additionalContext warning
#   3. append our $PPID (the Claude Code process) and rewrite the file
# Dead PIDs from crashed sessions are pruned on the next run, so no SessionEnd
# cleanup is required.
#
# Vendored into the repo (rather than referencing ~/.claude/hooks) so it travels
# with the project for every teammate who checks it out.

set -uo pipefail

CWD="$PWD"
LOCK_DIR="$HOME/.claude/locks"
mkdir -p "$LOCK_DIR"

HASH=$(printf '%s' "$CWD" | shasum -a 256 | cut -c1-12)
LOCK_FILE="$LOCK_DIR/session-${HASH}.lock"

declare -a LIVE_OTHER=()
declare -a KEEP=()

if [[ -f "$LOCK_FILE" ]]; then
  while IFS= read -r pid || [[ -n "$pid" ]]; do
    [[ -z "$pid" ]] && continue
    if kill -0 "$pid" 2>/dev/null; then
      KEEP+=("$pid")
      if [[ "$pid" != "$PPID" && "$pid" != "$$" ]]; then
        LIVE_OTHER+=("$pid")
      fi
    fi
  done < "$LOCK_FILE"
fi

# Add our own PPID (the parent of this hook shell — typically the Claude Code process)
KEEP+=("$PPID")

# Rewrite lock file with deduped live PIDs
printf '%s\n' "${KEEP[@]}" | sort -u > "$LOCK_FILE"

if (( ${#LIVE_OTHER[@]} > 0 )); then
  PRETTY_CWD="${CWD/#$HOME/~}"
  COUNT=${#LIVE_OTHER[@]}
  PIDS_STR=$(IFS=,; echo "${LIVE_OTHER[*]}")
  MSG="⚠️ WARNING: We have detected ${COUNT} other active Claude Code sessions in this folder (${PRETTY_CWD}). Concurrent PIDs: ${PIDS_STR}. To avoid conflicts (concurrent edits, Git race conditions, overwritten files), use the EnterWorktree tool to start working in a separate Git worktree before making any changes."
  jq -nc --arg ctx "$MSG" '{
    hookSpecificOutput: {
      hookEventName: "SessionStart",
      additionalContext: $ctx
    }
  }'
fi

exit 0