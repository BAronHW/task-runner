package core

import adapters.TaskSource
import fs2.io.file.Path

import java.util.UUID

case class DiscoveredTask(
    name: String,
    command: String,
    description: Option[String],
    dependencies: List[String],
    source: TaskSource,
    path: Path
)

case class Task(
    id: UUID,
    name: String,
    command: String,
    description: Option[String],
    dependencies: List[Task],
    source: TaskSource,
    path: Path
)

case class UnresolvedTask(task: Task, dependencyNames: List[String])

//This is the enum for pending/running/failed/successful tasks
sealed trait TaskStatus { val status: String }
object TaskStatus {
  final case object Failed extends TaskStatus { val status = "failed" }
  final case object Success extends TaskStatus { val status = "success" }
  final case object Pending extends TaskStatus { val status = "pending" }
  final case object Running extends TaskStatus { val status = "running" }
  final case object Skipped extends TaskStatus { val status = "skipped" }
}
