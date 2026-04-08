package adapters

import cats.effect.IO
import core.DiscoveredTask
import errors.FormatError
import fs2.io.file.{Files, Path}
import io.circe.parser
import io.circe.generic.auto._

object NpmAdapter extends TaskDiscoverer[IO] {

  override def name: TaskSource = TaskSource.Npm

  /** Detects the existence of a package.json file recursively within the current working directory
    * @param dir - The current working directory/path
    * @return a Boolean to signal if there exists a package.json within this dir
    */
  override def detect(dir: Path): IO[Boolean] = {
    Files[IO]
      .walk(dir)
      .filter(_.fileName.toString == "package.json")
      .head
      .compile
      .last
      .map(_.isDefined)
  }

  /** Walks through the whole CWD and finds all package.json files
    * converts all found files into readable Json structures via usage of circe parser
    * converts the json structure into DiscoveredTask case classes
    * @param dir - the current working directory
    * @return a list of all discovered Tasks
    */
  override def discover(dir: Path): IO[List[DiscoveredTask]] =
    Files[IO]
      .walk(dir)
      .filter(_.fileName.toString == "package.json")
      .evalMap { path =>
        readAndParse(path).handleErrorWith { e =>
          IO.println(s"Error reading $path: $e") >> IO.pure(
            List.empty[NpmBlock]
          )
        }
      }
      .flatMap(fs2.Stream.emits)
      .map(block =>
        DiscoveredTask(
          name = block.name,
          command = block.command,
          description = block.description,
          dependencies = List(),
          source = this.name
        )
      )
      .compile
      .toList

  /** Converts file at a given path into utf8 and parses it into readable json with circe
    * handles error if incorrect format with json
    * converts the json into Npmblock case class
    * @param path - path of the current file being processed
    * @return a list of NpmBlocks which contain the name of the script and command that will be run
    */
  private def readAndParse(path: Path): IO[List[NpmBlock]] =
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
            NpmBlock(name = scriptName, command = command)
          }
          .toList
      }
}
