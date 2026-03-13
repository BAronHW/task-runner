package adapters

import cats.effect.{ExitCode, IO}
import core.DiscoveredTask
import fs2.io.file.{Files, Path}
import io.circe.parser
import io.circe.generic.auto._

case class PackageJson(name: Option[String], scripts: Option[Map[String, String]])

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
      .evalMap(readAndParse)
      .flatMap(fs2.Stream.emits)
      .compile
      .toList

  def readAndParse(path: Path): IO[List[DiscoveredTask]] =
    Files[IO]
      .readUtf8(path)
      .compile
      .string
      .flatMap(content => IO.fromEither(parser.decode[PackageJson](content)))
      .map { pkg =>
        pkg.scripts.getOrElse(Map.empty).map { case (scriptName, command) =>
          DiscoveredTask(name = scriptName, command = command, description = None, source = "npm")
        }.toList
      }
}
