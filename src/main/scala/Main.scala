import adapters.{NpmAdapter, TaskConfigAdapter}
import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.kernel.Ref
import cats.implicits._
import core.{SystemState, TaskResolver}
import executor.TaskExecutor
import fs2.io.file.Path
import graph.TaskGraphResolver
import state.StateService

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val dir = args.headOption.getOrElse(System.getProperty("user.dir"))
    val path = Path(dir)

    List(NpmAdapter, TaskConfigAdapter)
      .parTraverse(a =>
        a.detect(path).flatMap {
          case false => IO.pure(Nil)
          case true  => a.discover(path)
        }
      )
      .map(_.flatten)
      .map(TaskResolver.resolveAll)
      .map(tasks => TaskGraphResolver.topologicalSort(tasks))
      .flatMap {
        case Left(error) => IO.println(s"Cycle detected: ${error.message}")
        case Right(sorted) =>
          for {
            ref <- Ref.of[IO, SystemState](SystemState(Map.empty, List.empty))
            stateService = new StateService[IO](ref)
            _ <- stateService.initializeTasks(sorted)
            result <- TaskExecutor.execute(sorted, stateService)
          } yield (result)
      }
      .as(ExitCode.Success)
  }
}
