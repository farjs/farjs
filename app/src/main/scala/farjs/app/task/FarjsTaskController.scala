package farjs.app.task

import farjs.app.FarjsStateDef
import io.github.shogowada.scalajs.reactjs.React.Props
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.react.UiComponent
import scommons.react.redux.BaseStateController
import scommons.react.redux.task.{TaskManager, TaskManagerProps}

object FarjsTaskController
  extends BaseStateController[FarjsStateDef, TaskManagerProps] {

  lazy val uiComponent: UiComponent[TaskManagerProps] = {
    TaskManager.uiComponent = FarjsTaskManagerUi
    TaskManager.errorHandler = FarjsTaskManagerUi.errorHandler
    TaskManager
  }

  def mapStateToProps(dispatch: Dispatch, state: FarjsStateDef, props: Props[Unit]): TaskManagerProps = {
    TaskManagerProps(state.currentTask)
  }
}
