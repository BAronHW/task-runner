package state

import cats.Monad
import cats.effect.{IO, Ref}
import core.{SystemState, Task, TaskStatus}
import cats.implicits._

import java.util.UUID

/** This is a service that exposes an interface for modifying the system state so that the user can
  * understand what tasks have been run and which ones have not been run yet
  */
class StateService[F[_]: Monad](ref: Ref[F, SystemState]) {

  def setState(state: SystemState): F[Unit] = {
    ref.set(state)
  }

  def getAllState(): F[SystemState] = {
    ref.get
  }

  def getSingleTaskState(taskId: UUID): F[Map[Task, TaskStatus]] = {
    ref.get.map { state =>
      state.taskStatuses.filter { case (key, value) => key.id == taskId }
    }
  }
}
