package executor

import cats.effect.IO
import core.Task
import fs2.io.process.{ProcessBuilder, Processes}
import fs2.text

/** This class accepts a sorted list of tasks from the TaskGraphResolver which then essentially
  * executes these tasks in an optimal way by executing tasks that have no dependencies in parallel
  * @param sortedList - This is a list of sorted tasks that the Task Executor is supposed to execute
  */
object TaskExecutor {

  def execute(sortedTasks: List[Task]) = {
    val batches = compileTaskBatch(sortedTasks)
    executeTaskBatch(batches)
  }

  private def executeTaskBatch(taskBatch: List[Task]): List[IO[Unit]] = {
    val batches = this.compileTaskBatch(taskBatch)
    batches.flatMap(batch => batch.map(task => runTask(task)))
  }

  private def runTask(task: Task): IO[Unit] = {
    ProcessBuilder("sh", List("-c", task.command))
      .withInheritEnv(true)
      .withWorkingDirectory(task.path)
      .spawn[IO]
      .use(process => {
        process.stdout
          .through(text.utf8.decode)
          .through(text.lines)
          .evalMap(line => IO.println(s"[${task.name}] $line"))
          .compile
          .drain
      })

  }

  /** This function is used to create a list of lists where each inner list represents a batch of
    * tasks to execute together. We create this by reducing on the given sortedList. The starting value
    * of the reduce function is going to be an empty list that
    * @return - A list of batches that individually can be run in parallel
    */
  private def compileTaskBatch(sortedList: List[Task]): List[List[Task]] = {
    sortedList.foldLeft(List.empty[List[Task]]) { (batch, task) =>
      {
        val taskDependenciesNames =
          task.dependencies.map(tsk => tsk.name).toSet

        val batchIndex = batch.zipWithIndex.indexWhere {
          case (_, index) => {
            val setOfTasks = batch.take(index).flatten.toSet
            taskDependenciesNames.subsetOf(setOfTasks.map(_.name))
          }
        }
        if (batchIndex == -1) batch :+ List(task)
        else batch.updated(batchIndex, batch(batchIndex) :+ task)
      }
    }
  }

}
