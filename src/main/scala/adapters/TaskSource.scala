package adapters
import cats.effect.Sync
import cats.syntax.all._
import core.DiscoveredTask
import fs2.io.file.Path

sealed trait TaskSource { val name: String }
object TaskSource {
  final case object Npm    extends TaskSource { val name = "npm"    }
  final case object Gradle extends TaskSource { val name = "gradle" }
  final case object Maven  extends TaskSource { val name = "maven"  }
  final case object Make   extends TaskSource { val name = "make"   }
  final case object Yaml   extends TaskSource { val name = "yaml"   }
}

trait TaskDiscoverer[F[_]] {
  def name: TaskSource
  def detect(dir: Path): F[Boolean]
  def discover(dir: Path): F[List[DiscoveredTask]]
}
