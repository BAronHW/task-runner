# Plan: TaskExecutor

## Overview

Implement concurrent task execution with dependency-aware scheduling. Tasks with no mutual dependency run concurrently via `parTraverse`. Each task signals completion to its dependents using a `Deferred[IO, TaskStatus]`, avoiding polling. If a dependency fails or is skipped, all downstream tasks are marked `Skipped` rather than run.

---

## Files to Change

| File | Change |
|---|---|
| `src/main/scala/core/Model.scala` | Add `Skipped` to `TaskStatus` |
| `src/main/scala/runner/TaskExecutor.scala` | **Create** — main executor |
| `src/main/scala/Main.scala` | Wire executor into the `Right(sorted)` branch |

---

## Core Design

### Concurrency model

All tasks are launched simultaneously via `parTraverse` — no topological pre-ordering is needed. Each task blocks internally on its own dependency `Deferred`s before deciding whether to run or skip. This means ordering emerges naturally from the dependency graph rather than being imposed upfront.

### Completion signalling

A `Map[Task, Deferred[IO, TaskStatus]]` is initialised before any tasks start. When a task finishes (success, failure, or skip), it completes its `Deferred` with the terminal `TaskStatus`. Dependents unblock as soon as all their deps have resolved.

### Skip propagation

After awaiting all dependency `Deferred`s, a task checks whether any resolved to `Failed` or `Skipped`. If so, it marks itself `Skipped` and completes its own `Deferred` immediately without running. This cascades down the dependency tree.

### Process execution

Tasks are run via `cmd /c <command>` (Windows shell). stdout and stderr are streamed line-by-line and prefixed with the task name. Both streams must be drained **concurrently** — sequential draining risks a pipe-buffer deadlock if the process writes stderr while stdout is still being consumed. Exit code 0 → `Success`, anything else → `Failed`.

### Shared status ref

A `Ref[IO, Map[Task, TaskStatus]]` tracks live task state. This is updated at each transition (`Pending → Running → Success/Failed/Skipped`) and is intended as the data source for the TUI renderer in a later phase.

---

## Missing `TaskStatus` case

`Skipped` is not yet in `TaskStatus`. Add it as a terminal case alongside `Success` and `Failed`.

---

## Wiring into Main

The existing `Right(sorted)` branch currently does nothing useful. Replace it to:
1. Create a fresh `Ref` for status tracking
2. Pass both the task list and the ref to `TaskExecutor.executeAll`

---

## Verification

- `sbt compile` must pass with no errors
- Running against a directory with a `.taskrunner.yaml` or `package.json` should show `[taskName]`-prefixed output lines
- Tasks whose dependencies fail should print a skip message rather than running
