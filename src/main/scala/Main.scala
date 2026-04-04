import adapters.{NpmAdapter, TaskConfigAdapter}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxParallelTraverse1
import core.TaskResolver
import fs2.io.file.Path
import graph.TaskGraph

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val dir = args.headOption.getOrElse(System.getProperty("user.dir"))
    val path = Path(dir)

    List(NpmAdapter, TaskConfigAdapter)
      .parTraverse(_.discover(path))
      .map(_.flatten)
      .map(TaskResolver.resolveAll)
      .map(tasks => TaskGraph.topologicalSort(tasks))
      .flatMap {
        case Left(error) => IO.println(s"Cycle detected: ${error.message}")
        case Right(sorted) =>
          IO {
            sorted.foreach(t => println(s"${t.name}"))
          }
      }
      .as(ExitCode.Success)
  }
}
