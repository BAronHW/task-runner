package core
import adapters.TaskSource
import adapters.TaskSource.{Npm, Yaml}

object TaskResolver {

  /** The main function that is exposed on this object that is called by the main runtime
    * public facing api to allow all discoveredTasks to be resolved and given dependencies
    * @param discoveredTasks - A list of all the discoveredTasks from all sourcs
    */
  def resolveAll(discoveredTasks: List[DiscoveredTask]): List[Task] = {
    val unresolved =
      discoveredTasks.distinctBy(d => (d.name, d.source)).map(toUnresolvedTask)

    val allTasks = unresolved.map(_.task)
    unresolved.map(u => resolveDependencies(u, allTasks))
  }

  /** *
    * creates an UnresolvedTask case class that has all the values of the discoveredTask
    * with a random uuid
    * @param discoveredTask - The discoveredTask you want to transform
    * @return A UnresolvedTask
    */
  private def toUnresolvedTask(
      discoveredTask: DiscoveredTask
  ): UnresolvedTask = {
    UnresolvedTask(
      task = Task(
        id = java.util.UUID.randomUUID(),
        name = discoveredTask.name,
        command = discoveredTask.command,
        description = discoveredTask.description,
        dependencies = List.empty,
        source = discoveredTask.source,
        path = discoveredTask.path
      ),
      dependencyNames = discoveredTask.dependencies
    )
  }

  /** Resolves an unresolvedTasks dependencies by passing logic to helper methods depending on
    * Task sourceType
    * @param unresolved - The unresolved task that is being resolved
    * @param allTasks - All Tasks that were discovered
    * @return Returns a Task with dependencies list resolved
    */
  private def resolveDependencies(
      unresolved: UnresolvedTask,
      allTasks: List[Task]
  ): Task = {
    val deps = unresolved.task.source match {
      case Npm  => resolveNpmDeps(unresolved.task, allTasks)
      case Yaml => resolveYamlDeps(unresolved, allTasks)
      case _    => List.empty
    }
    unresolved.task.copy(dependencies = deps)
  }

  /** Resolved task dependency by comparing them against all other Tasks
    * Compares against all other tasks and checks if their commands contain their command
    * This is the helper method for resolving npm Tasks
    * @param task - current task that you are trying to resolve
    * @param allTasks - All Tasks that have been discovred by the system
    * @return Returns a List of tasks that link back to your given task
    */
  private def resolveNpmDeps(task: Task, allTasks: List[Task]): List[Task] =
    allTasks
      .filter(other => other.source == TaskSource.Npm)
      .filter(other => other.name != task.name)
      .filter(other => task.command.contains(s"npm run ${other.name}"))

  /** Resolves task dependencies by comparing them against all other tasks
    * Compares against all other tasks and links them together
    * By seeing if the other task has the unresolvedTasks name
    * @param unresolved - The unresolved task that we are trying to resolve/link to another task
    * @param allTasks - List of all other tasks that have been discovered
    */
  private def resolveYamlDeps(
      unresolved: UnresolvedTask,
      allTasks: List[Task]
  ): List[Task] =
    allTasks
      .filter(other => other.source == TaskSource.Yaml)
      .filter(other => other.name != unresolved.task.name)
      .filter(other => unresolved.dependencyNames.contains(other.name))
}
