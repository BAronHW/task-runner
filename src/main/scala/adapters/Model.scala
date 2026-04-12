package adapters

import fs2.io.file.Path

sealed trait AdapterBlock {
  def name: String
  def command: String
  def description: Option[String]
  def dependencies: Option[List[String]]
}

case class TaskRunnerYamlChildBlock(
    name: String,
    command: String,
    description: Option[String],
    dependencies: Option[List[String]]
) extends AdapterBlock

case class NpmBlock(
    name: String,
    command: String,
    path: Path
) extends AdapterBlock {
  override val description: Option[String] = None
  override val dependencies: Option[List[String]] = None
}

case class PackageJson(
    name: Option[String],
    scripts: Option[Map[String, String]]
)

case class TaskRunnerConfig(tasks: List[TaskRunnerYamlChildBlock])
