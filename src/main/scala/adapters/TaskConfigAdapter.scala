package adapters

import cats.effect.IO
import core.DiscoveredTask
import error_handling.FormatError
import fs2.io.file.{Files, Path}
import io.circe.yaml.parser
import io.circe.generic.auto._

object TaskConfigAdapter extends TaskDiscoverer[IO] {

  override def name: TaskSource = TaskSource.Yaml

  override def detect(dir: Path): IO[Boolean] = {
    Files[IO]
      .walk(dir)
      .filter(_.fileName.toString.endsWith(".taskrunner.yaml"))
      .head
      .compile
      .last
      .map(_.isDefined)
  }

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

  private def createDiscoveredTask(
      taskRunnerChild: TaskRunnerYamlChildBlock
  ): DiscoveredTask = {
    DiscoveredTask(
      name = taskRunnerChild.name,
      command = taskRunnerChild.command,
      description = taskRunnerChild.description,
      source = this.name,
      dependencies = taskRunnerChild.dependsOn.getOrElse(List.empty)
    )
  }
}
