import adapters.NpmAdapter
import cats.effect.{ExitCode, IO, IOApp}
import fs2.io.file.Path

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val dir = args.headOption.getOrElse(System.getProperty("user.dir"))
    val path = Path(dir)

    NpmAdapter.discover(path)
      .flatMap(tasks => IO(tasks.foreach(println)))
      .as(ExitCode.Success)
  }
}