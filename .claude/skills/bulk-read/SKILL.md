---
name: bulk-read
description: Survey or summarize large/many files through a cheap haiku Explore subagent instead of dumping them into the main context. Use when the token-guard hook blocks a read, or before any multi-file sweep ("where is X used", "summarize module Y").
---

# Bulk read — cheap-model delegation

Grunt-work reading goes to a cheap model; the main (frontier) context keeps only the
conclusions. Adapted from Spotify's Portal approach using only built-in Claude Code:

Spawn **one** subagent per question:

```
Agent(
  subagent_type: "Explore",
  model: "haiku",
  prompt: "<one precise question>. Report conclusions with file:line citations only —
           do not quote file contents beyond the lines that answer the question."
)
```

Rules:

- **Ask a question, not for a copy.** "Which services call KeycloakService and from
  which methods?" — never "read these files and give them back to me". The savings come
  from the file bodies staying out of this context.
- Demand `file:line` citations so you can follow up with a targeted `Read(offset, limit)`.
- Batch related questions into one prompt; parallel-spawn only for independent topics.
- Search breadth: say "medium" or "very thorough" in the prompt.

## Do NOT delegate (do these in main context, with sliced reads)

- **Editing** — subagent line numbers go stale; Grep + `Read(offset, limit)` + Edit yourself.
- **Reasoning and debugging** — root-causing is exactly what the frontier model is for.
- **Security-sensitive analysis** — `SecurityConfig`, filters, `Permission`/`@PreAuthorize`,
  crypto (`AESConverter`): read the real slices yourself.
- **Anything touching `.env` secrets** — off-limits to every model, per CLAUDE.md.
