package farjs.app

import scommons.react.redux.task.{AbstractTask, TaskReducer}

trait FarjsStateDef {

  def currentTask: Option[AbstractTask]
}

case class FarjsState(currentTask: Option[AbstractTask]) extends FarjsStateDef

object FarjsStateReducer {

  def reduce(state: Option[FarjsState], action: Any): FarjsState = FarjsState(
    currentTask = TaskReducer(state.flatMap(_.currentTask), action)
  )
}
