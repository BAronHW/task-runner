package adapters

import cats.effect.IO
import core.DiscoveredTask
import errors.FormatError
import fs2.io.file.{Files, Path}
import io.circe.yaml.parser
import io.circe.generic.auto._

object TaskConfigAdapter extends TaskDiscoverer[IO] {

  override def name: TaskSource = TaskSource.Yaml

  /** Recursively traverses a given path and sees if a dir has the existence of a .taskrunner.yaml file
    * @param dir - The given path to search
    * @return A boolean to represent if the file is present
    */
  override def detect(dir: Path): IO[Boolean] = {
    Files[IO]
      .walk(dir)
      .filter(_.fileName.toString.endsWith(".taskrunner.yaml"))
      .head
      .compile
      .last
      .map(_.isDefined)
  }

  /** Recursively walks through a given path and parses any file that ends with .taskrunner.yaml
    * @param dir - The given path to search
    * @return This returns a list of Discovered Tasks
    */
  override def discover(dir: Path): IO[List[DiscoveredTask]] = {
    Files[IO]
      .walk(dir)
      .filter(_.fileName.toString.endsWith(".taskrunner.yaml"))
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
  }

  /** Reads a file at a given path and parses them into TaskRunnerConfig format
    * Once TaskRunnerConfig format is generated we map over them and turn them into discovered tasks
    * @param path - The path where the file exists that we are going to read and parse
    * @return a List of DiscoveredTasks
    */
  private def readAndParse(path: Path): IO[List[DiscoveredTask]] =
    Files[IO]
      .readUtf8(path)
      .compile
      .string
      .flatMap(content =>
        parser.decode[TaskRunnerConfig](content) match {
          case Left(error) => IO.raiseError(FormatError(error.getMessage))
          case Right(pkg)  => IO.pure(pkg)
        }
      )
      .map { pkg =>
        pkg.tasks.map { childTask =>
          createDiscoveredTask(childTask)
        }
      }

  /** A TaskRunnerConfig comprises multiple TaskRunnerYamlChildblocks
    * We take these blocks and turn them into discoveredTasks
    * @param taskRunnerChild - The TaskRunnerYamlChildBlock to turn into a DiscoveredTask
    * @return A DiscoveredTask
    */
  private def createDiscoveredTask(
      taskRunnerChild: TaskRunnerYamlChildBlock
  ): DiscoveredTask = {
    DiscoveredTask(
      name = taskRunnerChild.name,
      command = taskRunnerChild.command,
      description = taskRunnerChild.description,
      source = this.name,
      dependencies = taskRunnerChild.dependencies.getOrElse(List.empty)
    )
  }
}
