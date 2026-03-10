package adapters
import cats.effect.Sync
import cats.syntax.all._

sealed abstract class TaskSource(name: String)
object TaskSource {
  final case object Npm extends TaskSource(name = "npm")
  final case object Gradle extends TaskSource(name = "gradle")
  final case object Maven extends TaskSource(name = "maven")
  final case object Make extends TaskSource(name = "make")
}
