package executor

import core.Task

/** This class accepts a sorted list of tasks from the TaskGraphResolver which then essentially
  * executes these tasks in an optimal way by executing tasks that have no dependencies in parallel
  * @param sortedList - This is a list of sorted tasks that the Task Executor is supposed to execute
  */
class TaskExecutor(sortedList: List[Task]) {}
