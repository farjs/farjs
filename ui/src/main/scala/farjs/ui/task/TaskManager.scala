package farjs.ui.task

import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js
import scala.scalajs.js.{Error, JavaScriptException}

/**
  * Handles status of running tasks.
  */
object TaskManager extends FunctionComponent[TaskManagerProps] {

  var uiComponent: UiComponent[TaskManagerUiProps] = _
  
  var errorHandler: js.UndefOr[js.Function1[Any, TaskError]] = js.undefined
  
  private def defaultErrorHandler(error: Any): TaskError =
    TaskError(error.toString)
  
  private case class TaskManagerState(taskCount: Int = 0,
                                      status: js.UndefOr[String] = js.undefined,
                                      error: js.UndefOr[String] = js.undefined,
                                      errorDetails: js.UndefOr[String] = js.undefined)

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val (state, setState) = useStateUpdater(() => TaskManagerState())
    
    if (uiComponent == null) {
      throw JavaScriptException(Error("TaskManager.uiComponent is not specified"))
    }
    
    useLayoutEffect({ () =>
      props.startTask.foreach { task =>
        onTaskStart(setState, task)
      }
    }, List(props.startTask))
    
    <(uiComponent())(^.plain := TaskManagerUiProps(
      showLoading = state.taskCount > 0,
      onHideStatus = { () =>
        setState(_.copy(status = js.undefined))
      },
      onCloseErrorPopup = { () =>
        setState(_.copy(error = js.undefined, errorDetails = js.undefined))
      },
      status = state.status,
      error = state.error,
      errorDetails = state.errorDetails
    ))()
  }

  private def onTaskStart(setState: js.Function1[js.Function1[TaskManagerState, TaskManagerState], Unit],
                          task: Task): Unit = {

    val onFulfilled: js.Function1[Any, Any] = { value =>
      onTaskFinish(setState, task, js.undefined)
    }
    val onRejected: js.Function1[Any, Any] = { reason: Any =>
      onTaskFinish(setState, task, reason)
    }
    task.result.`then`[Any](onFulfilled, onRejected)

    setState(s => s.copy(
      taskCount = s.taskCount + 1,
      status = s"${task.message}..."
    ))
  }

  private def onTaskFinish(setState: js.Function1[js.Function1[TaskManagerState, TaskManagerState], Unit],
                           task: Task,
                           reason: js.UndefOr[Any]): Unit = {

    val durationMillis = js.Date.now() - task.startTime
    val statusMessage = s"${task.message}...Done ${formatDuration(durationMillis)} sec."
    val error: js.UndefOr[TaskError] = reason.map { e =>
      errorHandler.fold(defaultErrorHandler(e))(_.apply(e))
    }

    setState(s => s.copy(
      taskCount = s.taskCount - 1,
      status = statusMessage,
      error = error.map(_.error),
      errorDetails = error.flatMap(_.errorDetails)
    ))
  }

  private[task] def formatDuration(durationMillis: Double): String = {
    "%.3f".format(durationMillis / 1000.0)
  }
}
