import adapters.{NpmAdapter, TaskConfigAdapter}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxParallelTraverse1
import core.TaskResolver
import fs2.io.file.Path

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val dir = args.headOption.getOrElse(System.getProperty("user.dir"))
    val path = Path(dir)

    List(NpmAdapter, TaskConfigAdapter)
      .parTraverse(_.discover(path))
      .map(_.flatten)
      .map(TaskResolver.resolveAll)
      .flatMap(tasks =>
        IO(tasks.foreach { t =>
          println(
            s"${t.name} -> [${t.dependencies.map(_.name).mkString(", ")}]"
          )
        })
      )
      .as(ExitCode.Success)
  }
}
