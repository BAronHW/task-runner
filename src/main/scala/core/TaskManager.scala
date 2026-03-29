package core
import core.TaskManager

object TaskManager {

  /** converts a DiscoveredTask into a Task by handing conversion logic to different private methods depending on their source type
    * @param discoveredTask - The DiscoveredTask that is being converted into a Task
    * @param allDiscTasks - The whole list of DiscoveredTask
    * @return Single Task with id and dependencies List
    */
  def convertDiscTaskToTask(
      discoveredTask: DiscoveredTask,
      allDiscTasks: List[DiscoveredTask]
  ): Task = {
    val allDiscTasksExcept =
      allDiscTasks.filter(currTask => currTask.name != discoveredTask.name)

    discoveredTask.source match {
      case "npm"         => convertNpmDiscTask(discoveredTask)
      case "task_config" => convertYamlDiscTask(discoveredTask)
    }
  }

  private def convertNpmDiscTask(
      discoveredTask: DiscoveredTask
  ): Task = {}

  private def convertYamlDiscTask(
      discoveredTask: DiscoveredTask
  ): Task = {
    val uuid = java.util.UUID.randomUUID
    discoveredTask.dependencies match {
      case Some(deps) =>
        Task(
          id = uuid,
          name = discoveredTask.name,
          command = discoveredTask.command,
          dependencies = deps,
          source = discoveredTask.source
        )
      case None =>
        Task(
          id = uuid,
          name = discoveredTask.name,
          command = discoveredTask.command,
          dependencies = Nil,
          source = discoveredTask.source
        )
    }
  }
}
