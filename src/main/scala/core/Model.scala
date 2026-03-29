package core

import adapters.TaskSource

import java.util.UUID

case class DiscoveredTask(
    name: String,
    command: String,
    description: Option[String],
    dependencies: Option[List[String]],
    source: String
)

case class Task(
    id: UUID,
    name: String,
    command: String,
    dependencies: List[String],
    source: TaskSource
)

//This is the enum for pending/running/failed/successful tasks
sealed abstract class TaskStatus(status: String)
object TaskStatus {
  final case object Failed extends TaskStatus("failed")
  final case object Success extends TaskStatus("success")
  final case object Pending extends TaskStatus("pending")
  final case object Running extends TaskStatus("running")
}
