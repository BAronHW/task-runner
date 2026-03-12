package adapters

import cats.effect.{ExitCode, IO}
import core.DiscoveredTask
import fs2.io.file.{Files, Path}

object NpmAdapter extends TaskDiscoverer[IO] {

  override def name: String = "npm"

  override def detect(dir: Path): IO[Boolean] = {
    Files[IO]
      .walk(dir)
      .filter(_.fileName.toString == "package.json")
      .head
      .compile
      .last
      .map(_.isDefined)  // True if at least one package.json exists in the tree
  }


    override def discover(dir: Path): IO[List[DiscoveredTask]] = {
    // TODO: Read and parse package.json to extract scripts
    val listOfDiscoveredTasks = this.detect(dir).flatMap { hasPackageJson =>
      if (hasPackageJson) {
        val files = Files[IO]
          .walk(dir)
          .filter(_.equals(dir/"package.json"))
          .map()
        files
      } else {
        IO.pure(List.empty)
      }
    }
  }
}
