package farjs.app

import farjs.ui.task.{Task, TaskReducer}

trait FarjsStateDef {

  def currentTask: Option[Task]
}

case class FarjsState(currentTask: Option[Task] = None) extends FarjsStateDef

object FarjsStateReducer {

  def apply(state: FarjsState, action: Any): FarjsState = FarjsState(
    currentTask = TaskReducer(state.currentTask, action)
  )
}
