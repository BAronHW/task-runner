package state

import cats.Monad
import cats.effect.unsafe.PollResult.Complete
import cats.effect.{IO, Ref}
import core.{SystemState, Task, TaskStatus}
import cats.implicits._
import core.TaskStatus.{Pending, Running}

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

  /** Given a single taskId returns the singular KV element associated to that taskId
    * @param taskId the taskId that you are fetching
    * @return Map[Task, TaskStatus] Singular KV map that represents the taskId and the taskState of that
    * taskId
    */
  def getSingleTaskState(taskId: UUID): F[Map[Task, TaskStatus]] = {
    ref.get.map { state =>
      state.taskStatuses.filter { case (key, value) => key.id == taskId }
    }
  }

  /** Allows you to update a single task state
    * @param taskId the taskId that you want to update
    * @param taskStatus the taskStatus that you want to set it to
    * @return
    */
  def updateSingleTaskState(
      taskId: UUID,
      taskStatus: TaskStatus
  ): F[Boolean] = {
    // takes a function that returns the new state and also function return
    ref.modify { state =>
      state.taskStatuses.find { case (key, _) => key.id == taskId } match {
        case Some((task, _)) =>
          (state.copy(state.taskStatuses.updated(task, taskStatus)), true)
        case None => (state, false)
      }
    }
  }

  /** This function is used to initialize the map that holds the state of all the tasks and sets their statuses to equal pending
    * @param tasks a list of tasks that the task runner has in its system
    * @return F[Unit]
    */
  def initializeTasks(tasks: List[Task]): F[Unit] = {
    val taskMap = tasks.map(task => (task, Pending)).toMap
    ref.set(SystemState(taskMap, List.empty[String]))
  }

  /**  This function returns true when all tasks in the state are not running and not pending otherwise it returns false
    * @return
    */
  def areAllTasksComplete(): F[Boolean] = {
    ref.get.map(state => {
      val statusArr = state.taskStatuses
      statusArr
        .forall(status => (status._2 != Running && status._2 != Pending))
    })
  }
}
