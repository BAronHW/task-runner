//package graph
//
//import adapters.TaskRunnerConfig
//import core.DiscoveredTask
//
//// This may only work with taskRunner.Yaml files for now
//
//object TaskGraph {
//
//  def topologicalSort(
//      tasks: List[DiscoveredTask]
//  ): Either[CyclicalDependancyError, List[String]] = {}
//
//  /** Computes the number of dependant tasks on each task in a list of tasks
//    *
//    * @return A map where of tasks and their respective number of dependants
//    */
//  private def computeInDegrees(tasks: List[DiscoveredTask]) = {
//    tasks.map { task =>
//      (task.command)
//    }
//  }
//}
