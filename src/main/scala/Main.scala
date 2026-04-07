import adapters.{NpmAdapter, TaskConfigAdapter}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxParallelTraverse1
import core.TaskResolver
import fs2.io.file.Path
import graph.TaskGraphResolver

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val dir = args.headOption.getOrElse(System.getProperty("user.dir"))
    val path = Path(dir)

    List(NpmAdapter, TaskConfigAdapter)
      .parTraverse(_.discover(path))
      .map(_.flatten)
      .map(TaskResolver.resolveAll)
      .map(tasks => TaskGraphResolver.topologicalSort(tasks))
      .flatMap {
        case Left(error) => IO.println(s"Cycle detected: ${error.message}")
        case Right(sorted) =>
          IO {
            sorted.foreach(t => println(s"${t.name} from ${t.source}"))
          }
      }
      .as(ExitCode.Success)
  }
}
