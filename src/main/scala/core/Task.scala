package core

import adapters.TaskSource
import java.util.UUID

case class DiscoveredTask(
  name: String,
  command: String,
  description: Option[String],
  source: String
)

case class Task(
  id: UUID, // this should be a uuid
  name: String,
  command: String,
  dependencies: List[String],
  source: TaskSource
)


