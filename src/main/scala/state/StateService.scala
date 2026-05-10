package state

import cats.effect.{IO, Ref}

/** This is a service that exposes an interface for modifying the system state so that the user can
  * understand what tasks have been run and which ones have not been run yet
  */
object StateService {
  val ref = Ref[IO]

  def setState(): IO[Unit] = {}
}
