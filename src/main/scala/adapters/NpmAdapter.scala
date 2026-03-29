package adapters

import cats.effect.IO
import core.DiscoveredTask
import error_handling.FormatError
import fs2.io.file.{Files, Path}
import io.circe.parser
import io.circe.generic.auto._

case class PackageJson(
    name: Option[String],
    scripts: Option[Map[String, String]]
)

object NpmAdapter extends TaskDiscoverer[IO] {

  override def name: String = "npm"

  override def detect(dir: Path): IO[Boolean] = {
    Files[IO]
      .walk(dir)
      .filter(_.fileName.toString == "package.json")
      .head
      .compile
      .last
      .map(_.isDefined)
  }

  override def discover(dir: Path): IO[List[DiscoveredTask]] =
    Files[IO]
      .walk(dir)
      .filter(_.fileName.toString == "package.json")
      .evalMap { path =>
        readAndParse(path).handleErrorWith { e =>
          IO.println(s"Error reading $path: $e") >> IO.pure(
            List.empty[DiscoveredTask]
          )
        }
      }
      .flatMap(fs2.Stream.emits)
      .compile
      .toList

  private def readAndParse(path: Path): IO[List[DiscoveredTask]] =
    Files[IO]
      .readUtf8(path)
      .compile
      .string
      .flatMap(content =>
        parser.decode[PackageJson](content) match {
          case Left(error) => IO.raiseError(FormatError(error.getMessage))
          case Right(pkg)  => IO.pure(pkg)
        }
      )
      .map { pkg =>
        pkg.scripts
          .getOrElse(Map.empty)
          .map { case (scriptName, command) =>
            DiscoveredTask(
              name = scriptName,
              command = command,
              description = None,
              source = this.name
            )
          }
          .toList
      }
}
