package core
import adapters.TaskSource.{Npm, Yaml}

case class UnresolvedTask(task: Task, dependencyNames: List[String])

object TaskResolver {

  def resolveAll(discoveredTasks: List[DiscoveredTask]): List[Task] = {
    val unresolved = discoveredTasks.map(toUnresolvedTask)
    val allTasks = unresolved.map(_.task)
    unresolved.map(u => resolveDependencies(u, allTasks))
  }

  private def toUnresolvedTask(discoveredTask: DiscoveredTask): UnresolvedTask =
    UnresolvedTask(
      task = Task(
        id = java.util.UUID.randomUUID(),
        name = discoveredTask.name,
        command = discoveredTask.command,
        description = discoveredTask.description,
        dependencies = List.empty,
        source = discoveredTask.source
      ),
      dependencyNames = discoveredTask.dependencies
    )

  private def resolveDependencies(
      unresolved: UnresolvedTask,
      allTasks: List[Task]
  ): Task = {
    val deps = unresolved.task.source match {
      case Npm  => resolveNpmDeps(unresolved.task, allTasks)
      case Yaml => resolveYamlDeps(unresolved, allTasks)
    }
    unresolved.task.copy(dependencies = deps)
  }

  private def resolveNpmDeps(task: Task, allTasks: List[Task]): List[Task] =
    allTasks
      .filter(other => other.name != task.name)
      .filter(other => other.dependencies.contains(task.name))

  private def resolveYamlDeps(
      unresolved: UnresolvedTask,
      allTasks: List[Task]
  ): List[Task] =
    allTasks.filter(other => unresolved.dependencyNames.contains(other.name))
}
