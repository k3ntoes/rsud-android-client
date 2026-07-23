# Issue tracker: Beads

Issues and specs for this repo live in a Beads Dolt database. Use the `bd` CLI for all operations.

## Conventions

- **Create an issue**: `bd create <title>`
- **List issues**: `bd list`
- **View issue details**: `bd show <issue-id>`
- **Update issue status**: `bd update <issue-id> [--claim | --status <state>]`
- **Close an issue**: `bd update <issue-id> --status done`
- **Sync with remote**: `bd dolt push` / `bd dolt pull`

All issue data lives in `.beads/` in the repo root.

## When a skill says "publish to the issue tracker"

Run `bd create <title>` with the issue body passed via stdin or as a title string.

## When a skill says "fetch the relevant ticket"

Run `bd show <issue-id>`.

## Wayfinding operations

Used by `/wayfinder`. (Beads-specific wayfinding not yet mapped; future enhancement.)
