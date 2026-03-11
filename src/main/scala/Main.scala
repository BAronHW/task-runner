import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import fs2.io.file.{Files, Path}

object Main extends IOApp {

  def findJsonFiles(dir: Path): Stream[IO, Path] =
    Files[IO]
      .walk(dir)
      .filter(_.extName == ".json")

  override def run(args: List[String]): IO[ExitCode] = {
    val dir = args.headOption.getOrElse(System.getProperty("user.dir"))
    val path = Path(dir)

    findJsonFiles(path)
      .evalMap(p => IO.println(p.toString))
      .compile
      .drain
      .as(ExitCode.Success)
  }
}