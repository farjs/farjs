package farjs.app

import farjs.ui.task.{AbstractTask, TaskReducer}

trait FarjsStateDef {

  def currentTask: Option[AbstractTask]
}

case class FarjsState(currentTask: Option[AbstractTask] = None) extends FarjsStateDef

object FarjsStateReducer {

  def apply(state: FarjsState, action: Any): FarjsState = FarjsState(
    currentTask = TaskReducer(state.currentTask, action)
  )
}
