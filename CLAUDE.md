# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
sbt compile       # Compile
sbt run           # Run (prompts for directory, or pass as argument)
sbt "run /path"   # Run against a specific directory
sbt test          # Run tests
sbt clean         # Clean build artifacts
```

## Architecture

This is a Scala CLI tool that discovers and runs tasks from various build systems/package managers. It uses Cats Effect for IO and FS2 for streaming filesystem traversal.

### Core Abstractions

**`adapters/TaskSource.scala`** — The central extension point. Defines:
- `TaskSource` sealed ADT: `Npm | Gradle | Maven | Make`
- `TaskDiscoverer[F[_]]` trait: the interface all adapters must implement
  - `detect(dir: Path): F[Boolean]` — check if this source exists in the directory
  - `discover(dir: Path): F[List[DiscoveredTask]]` — extract tasks from the directory

**`core/Task.scala`** — Data models:
- `DiscoveredTask(name, command, description, source)` — lightweight, for display/selection
- `Task` — full model with UUID, dependencies, and `TaskSource`

### Adapter Pattern

Each build system gets its own adapter implementing `TaskDiscoverer[IO]`. Currently only `NpmAdapter` is implemented — it walks the filesystem for `package.json` files using FS2 streams and extracts scripts via Circe JSON decoding. Errors are recovered gracefully with logging so one bad file doesn't abort discovery.

To add a new adapter (e.g., `MakeAdapter`):
1. Add a `case object Make` to `TaskSource` (already exists)
2. Create `src/main/scala/adapters/MakeAdapter.scala` implementing `TaskDiscoverer[IO]`
3. Wire it into `Main.scala`

### Effect System

- All I/O is `IO` from Cats Effect — keep it that way
- FS2 streams (`Stream[IO, _]`) are used for lazy filesystem traversal
- Use `IO.fromEither` + `handleErrorWith` for error recovery (see `NpmAdapter` for the pattern)
- JSON parsing uses Circe with `io.circe.generic.auto._` for automatic codec derivation

### Unimplemented Areas

- `config/TaskConfig.scala` and `config/TaskConfigParser.scala` — stubs only
- `tui/` — empty, planned for interactive terminal UI
- Adapters for Gradle, Maven, Make
- Task execution and dependency resolution

# TaskRunner

A functional TUI task runner for TypeScript and Gradle projects, built
with Scala 2.13, Cats Effect, and fs2.

## What It Does

Discovers tasks from multiple build tools in the current directory,
displays them in an interactive TUI, and runs them with live output
streaming. Supports cross-tool task dependencies via a custom config.

## Task Sources

- **npm** — reads `scripts` from `package.json`
- **Gradle** — runs `./gradlew tasks --all` and parses output
- **Custom** — defined in `.taskrunner.yaml` at the project root

## Key Architecture
```
adapters/       discovers tasks from npm, Gradle, custom config
core/           domain types — Task, TaskStatus, AppError
config/         parses and validates .taskrunner.yaml
graph/          builds DAG, topological sort, cycle detection
runner/         spawns processes, streams output via fs2
tui/            event loop, AppState (Ref), renderer
```

## Core Tech

- `IO` + `IOApp` — effect system entry point
- `Resource` — process lifecycle, guaranteed cleanup
- `Ref[IO, AppState]` — shared TUI state across fibers
- `fs2.Stream` — live process output line by line
- `Queue[IO, Event]` — keyboard + output fibers feed render loop
- `ValidatedNel` — accumulates all config/graph errors upfront
- `parTraverse` — runs adapters and independent tasks concurrently

## Config Format
```yaml
tasks:
  test:
    cmd: vitest
    depends: [compile]   # can depend on npm/gradle tasks too
```

## Build
```bash
sbt run          # run the app
sbt test         # run tests
sbt assembly     # fat JAR for distribution
```

## Current Status

Working through development phases:

- [ ] Phase 1 — Domain types
- [ ] Phase 2 — Config parsing
- [ ] Phase 3 — Task graph
- [ ] Phase 4 — Task execution
- [ ] Phase 5 — Adapters
- [ ] Phase 6 — CLI end-to-end
- [ ] Phase 7 — TUI
- [ ] Phase 8 — Integration
