package core

//This is the enum for pending/running/failed/successful tasks
sealed abstract class TaskStatus(status: String)
object TaskStatus {
  final case object Failed extends TaskStatus("failed")
  final case object Success extends TaskStatus("success")
  final case object Pending extends TaskStatus("pending")
  final case object Running extends TaskStatus("running")
}
