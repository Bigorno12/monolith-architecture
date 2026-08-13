#!/usr/bin/env bash
# PostToolUse hook: runs after editing a Java OR Kotlin source file.
#   1. Source-root guard: src/main/kotlin is compiled by nothing in this project
#   2. FQCN check: warn when fully-qualified class names appear in code
#      example: mu.server.rest.advice.GlobalExceptionHandler
#
# NOTE: Spotless formatting lives in .githook/pre-commit (singular ".githook",
# installed automatically by `mvn initialize`) — reformatting files right after an
# edit invalidated Claude's read cache (file changed on disk behind the agent's back).

set -uo pipefail

INPUT=$(cat)
FILE=$(echo "$INPUT" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('tool_input',{}).get('file_path',''))" 2>/dev/null || true)

# Java AND Kotlin: this project keeps .kt files next to .java under src/main/java.
printf '%s' "$FILE" | grep -qE '\.(java|kt)$' || exit 0

# Generated output is not ours to correct — the OpenAPI spec / MapStruct interface is.
printf '%s' "$FILE" | grep -q '/target/generated-sources/' && exit 0

[[ -f "$FILE" ]] || exit 0

emit() {
    MSG="$1" python3 -c "
import json, os
print(json.dumps({'hookSpecificOutput': {'hookEventName': 'PostToolUse', 'additionalContext': os.environ['MSG']}}))
"
    exit 0
}

# ── 1. Wrong source root ──────────────────────────────────────────────────────
# kotlin-maven-plugin compiles src/main/java (both languages), and Spotless only
# includes src/main/java/**/*.kt. A file under src/main/kotlin is compiled by
# nothing and formatted by nothing — it just silently never ships.
if printf '%s' "$FILE" | grep -qE '/src/(main|test)/kotlin/'; then
    emit "$FILE is under src/main/kotlin — this project compiles Kotlin from src/main/java (kotlin-maven-plugin sourceDirs), and Spotless only formats src/main/java/**/*.kt. Move it under src/main/java/ or it will silently not build."
fi

# ── 2. FQCN check ─────────────────────────────────────────────────────────────
FQCNS=$(grep -nE '[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)+\.[A-Z][a-zA-Z0-9]*' "$FILE" \
    | grep -vE '^[0-9]+:[[:space:]]*(import |package |//|\*|/\*)' \
    2>/dev/null || true)

[[ -z "$FQCNS" ]] && exit 0

emit "FQCNs found in $FILE — replace with simple names + add imports (Spotless enforces the import order, so just add them and run 'mvn spotless:apply'):
$FQCNS"
