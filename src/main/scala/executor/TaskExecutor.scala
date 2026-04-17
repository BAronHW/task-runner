package executor

import core.Task

/** This class accepts a sorted list of tasks from the TaskGraphResolver which then essentially
  * executes these tasks in an optimal way by executing tasks that have no dependencies in parallel
  * @param sortedList - This is a list of sorted tasks that the Task Executor is supposed to execute
  */
class TaskExecutor(sortedList: List[Task]) {

  def executeTasks() = ???

  /** This function is used to create a list of lists where each inner list represents a batch of
    * tasks to execute together. A task belongs in a batch when all the tasks that appear in its dependencies
    * already finished running in a batch before it.
    * @return listOfBatches - This is a list of tasks that are grouped together that represent
    */
  private def compileTaskBatch(): List[List[Task]] = {
    sortedList.foldLeft(List.empty[List[Task]]) { (batch, task) =>
      {
        // a set of unique task names for the current task
        val taskDependencies = task.dependencies.map(tsk => tsk.name).toSet()
        // find the index where all dependencies already appear before it
      }
    }
  }

  private def executeSingleTask() = {}
}
