package graph

import adapters.TaskRunnerConfig
import core.Task

import scala.annotation.tailrec

object TaskGraphResolver {

  /** Sorts a list of tasks into a valid execution order using Kahn's algorithm.
    * Tasks with no dependencies come first; tasks that depend on others follow
    * only after all their dependencies have been placed before them.
    * @param tasks - A list of fully resolved tasks (dependencies already linked)
    * @return Right with tasks in execution order, or Left with a CyclicalDependencyError
    *         if the dependency graph contains a cycle
    */
  def topologicalSort(
      tasks: List[Task]
  ): Either[CyclicalDependencyError, List[Task]] = {
    val indegreeMap = buildInDegreeMap(tasks)
    val reverseGraph = compileReverseGraph(tasks)

    val zeroIndegreeTasks = indegreeMap.filter { case (_, indegree) =>
      indegree == 0
    }
    val processQueue = zeroIndegreeTasks.keys.toList

    val sortedTaskAcc = List.empty[Task]

    loop(processQueue, indegreeMap, reverseGraph, sortedTaskAcc) match {
      case Right(sorted) if sorted.length < tasks.length =>
        Left(CyclicalDependencyError("Cyclical Dependency Error"))
      case Right(sorted) => Right(sorted.reverse)
      case left          => left
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

  /** Recursive implementation of Kahn's algorithm.
    *
    * Steps:
    * 1. Base case — if the queue is empty, return the accumulated list as Right(acc)
    * 2. Take the head task off the queue and append it to the accumulator
    * 3. Look up which tasks depend on head via the reverse graph (i.e. which tasks head unblocks)
    * 4. Decrement the in-degree of each of those dependents by 1, since head has now been processed
    * 5. Filter the dependents to find those whose in-degree has reached 0 (now fully unblocked)
    * 6. Append the newly unblocked tasks to the remaining queue and recurse
    *
    * Cycle detection is handled in topologicalSort after the loop completes —
    * if the output list is shorter than the input, some tasks were never unblocked (stuck in a cycle).
    *
    * @param currentQueue - Queue of tasks with an in-degree of 0, ready to be processed
    * @param inDegreeMap - A Map of each Task to its current in-degree count
    * @param reverseGraphMap - A Map of each Task to the list of tasks that depend on it
    * @param acc - Accumulated list of processed tasks in execution order
    * @return Right with a list of tasks in execution order, or Left with a CyclicalDependencyError if a cycle is detected
    */
  @tailrec
  private def loop(
      currentQueue: List[Task],
      inDegreeMap: Map[Task, Int],
      reverseGraphMap: Map[Task, List[Task]],
      acc: List[Task]
  ): Either[CyclicalDependencyError, List[Task]] = {
    currentQueue match {
      case Nil => Right(acc)
      case head :: tail => {
        val newAcc = head :: acc
        val dependents = reverseGraphMap.getOrElse(head, List.empty)
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
