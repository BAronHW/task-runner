package executor

import cats.data.{EitherT, Validated, ValidatedNel}
import cats.effect.IO
import cats.implicits._
import core.Task
import fs2.io.process.ProcessBuilder
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

  private def executeTaskBatch(
      batches: List[List[Task]]
  ): EitherT[IO, List[String], Unit] = {
    val res = batches.foldLeft(IO.unit) { (prev, batch) =>
      prev >> batch.parTraverse_ { task =>
        val taskResult = runTask(task)
        for {
          validNel <- taskResult

        } yield ()

      }
    }
  }

  /** Is responsible for running a singular task. Does this by using the fs2 ProcessBuilder
    * in order to stream output. Uses local machine's environment variables and runs the task on its discovered path
    * streams the processes result and decode its bytes into text then get its texts lines
    * then printOut its values.
    * @param task - A single Task case class
    * @return - IO[Unit]
    */
  private def runTask(task: Task): IO[ValidatedNel[String, Unit]] = {
    ProcessBuilder("sh", List("-c", task.command))
      .withInheritEnv(true)
      .withWorkingDirectory(task.path)
      .spawn[IO]
      .use(process => {
        val stdout = process.stdout
          .through(text.utf8.decode)
          .through(text.lines)
          .evalMap(line => IO.println(s"[${task.name}] $line"))
          .compile
          .drain

        val stderr = process.stderr
          .through(text.utf8.decode)
          .through(text.lines)
          .compile
          .toList

        for {
          both <- IO.both(stdout, stderr)
          (_, errors) = both
          validation <-
            if (errors.nonEmpty) {
              IO.pure(errors.mkString("\n").invalidNel)
            } else
              IO.pure(().validNel)
        } yield (validation)
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
