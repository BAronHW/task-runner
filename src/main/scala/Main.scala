import adapters.{NpmAdapter, TaskConfigAdapter}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import core.TaskResolver
import fs2.io.file.Path
import graph.TaskGraphResolver

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val dir = args.headOption.getOrElse(System.getProperty("user.dir"))
    val path = Path(dir)

    List(NpmAdapter, TaskConfigAdapter)
      .parTraverse(a => a.detect(path).flatMap {
        case false => IO.pure(Nil)
        case true  => a.discover(path)
      })
      .map(_.flatten)
      .map(TaskResolver.resolveAll)
      .map(tasks => TaskGraphResolver.topologicalSort(tasks))
      .flatMap {
        case Left(error) => IO.println(s"Cycle detected: ${error.message}")
        case Right(sorted) =>
          sorted.traverse_(t => IO.println(s"${t.name} from ${t.source}"))
      }
      .as(ExitCode.Success)
  }
}
