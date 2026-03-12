package adapters
import cats.effect.Sync
import cats.syntax.all._
import core.DiscoveredTask
import fs2.io.file.Path

sealed abstract class TaskSource(name: String)
object TaskSource {
  final case object Npm extends TaskSource(name = "npm")
  final case object Gradle extends TaskSource(name = "gradle")
  final case object Maven extends TaskSource(name = "maven")
  final case object Make extends TaskSource(name = "make")
}

trait TaskDiscoverer[F[_]] {
  def name: String
  def detect(dir: Path): F[Boolean]
  def discover(dir: Path): F[List[DiscoveredTask]]
}
