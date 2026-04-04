package graph

import adapters.TaskRunnerConfig
import core.Task

object TaskGraph {

  def topologicalSort(
      tasks: List[Task]
  ): Either[CyclicalDependancyError, List[Task]] = {
    val indegreeMap = buildInDegreeMap(tasks)
    val reverseGraph = compileReverseGraph(tasks)

    val zeroIndegreeTasks = indegreeMap.filter { case (_, indegree) =>
      indegree == 0
    }
    val processQueue = zeroIndegreeTasks.keys.toList

    val sortedTaskAcc = List.empty[Task]

    loop(processQueue, indegreeMap, reverseGraph, sortedTaskAcc) match {
      case Right(sorted) if sorted.length < tasks.length =>
        Left(CyclicalDependancyError("Cyclical Dependency Error"))
      case result => result
    }
  }

  /** Creates an in degree map for every task in a given task list
    * an in-degree map is a map that has a task as a key and the values are the number of other tasks that point to it
    * @param tasks - A list of resolved tasks
    * @return Returns an in degree map
    */
  private def buildInDegreeMap(tasks: List[Task]): Map[Task, Int] = {
    tasks.map { task => (task, task.dependencies.length) }.toMap
  }

  /** For every task, find which other tasks depend on it (i.e. which tasks it unblocks)
    * @param tasks - the task list to compute the inverse adjacency for
    * @return a Map where the key is a task and the value is the list of tasks that depend on it
    */
  private def compileReverseGraph(tasks: List[Task]): Map[Task, List[Task]] = {
    tasks.map { task =>
      (task, tasks.filter(_.dependencies.exists(_.name == task.name)))
    }.toMap
  }

  private def loop(
      currentQueue: List[Task],
      inDegreeMap: Map[Task, Int],
      reverseGraphMap: Map[Task, List[Task]],
      acc: List[Task]
  ): Either[CyclicalDependancyError, List[Task]] = {
    currentQueue match {
      case Nil => Right(acc)
      case head :: tail => {
        val newAcc = acc :+ head
        // the list of tasks from the head of the queue
        val dependents = reverseGraphMap.getOrElse(head, List.empty)
        // TODO: decrement indegrees, find newly zero-indegree tasks, recurse
        val newInDegreeMap = dependents.foldLeft(inDegreeMap) {
          (currentMap, task) =>
            currentMap.updated(task, (currentMap.getOrElse(task, 0) - 1))
        }
        val zeroIndegreeTask = dependents.filter { task =>
          newInDegreeMap.getOrElse(task, 0) == 0
        }
        loop(
          tail ++ zeroIndegreeTask,
          newInDegreeMap,
          reverseGraphMap,
          newAcc
        )
      }
    }
  }
}
