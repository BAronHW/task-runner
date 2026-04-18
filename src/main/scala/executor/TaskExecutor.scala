package executor

import core.Task

/** This class accepts a sorted list of tasks from the TaskGraphResolver which then essentially
  * executes these tasks in an optimal way by executing tasks that have no dependencies in parallel
  * @param sortedList - This is a list of sorted tasks that the Task Executor is supposed to execute
  */
object TaskExecutor {

  def executeTasks() = ???

  /** This function is used to create a list of lists where each inner list represents a batch of
    * tasks to execute together. We create this by reducing on the given sortedList. The starting value
    * of the reduce function is going to be an empty list that
    * @return - A list of batches that individually can be run in parallel
    */
  private def compileTaskBatch(sortedList: List[Task]): List[List[Task]] = {
    sortedList.foldLeft(List.empty[List[Task]]) { (batch, task) =>
      {
        // a set of unique task names for the current task
        val taskDependenciesNames =
          task.dependencies.map(tsk => tsk.name).toSet
        // find the index where all dependencies already appear before it
        // check if all of current task.dependency exists in previous batches
        val batchIndex = batch.indexWhere(innerBatch =>
          // find the index where current tasks dependency list is a subset of the inner batch
          // do this for every inner batch
          taskDependenciesNames.subsetOf(
            innerBatch.map(_.name).toSet
          )
        )
        if (batchIndex == -1) batch :+ List(task)
        else batch.updated(batchIndex + 1, batch(batchIndex + 1) :+ task)
      }
    }

  }
}
