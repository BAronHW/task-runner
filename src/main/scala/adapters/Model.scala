package adapters

sealed trait AdapterBlock {
  def name: String
  def command: String
  def description: Option[String]
  def dependsOn: Option[List[String]]
}

case class TaskRunnerYamlChildBlock(
    name: String,
    command: String,
    description: Option[String],
    dependsOn: Option[List[String]]
) extends AdapterBlock

case class NpmBlock(
    name: String,
    command: String
) extends AdapterBlock {
  override val description: Option[String] = None
  override val dependsOn: Option[List[String]] = None
}

case class PackageJson(
    name: Option[String],
    scripts: Option[Map[String, String]]
)

case class TaskRunnerConfig(tasks: List[TaskRunnerYamlChildBlock])
