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
  
  var errorHandler: js.Function1[Any, js.UndefOr[TaskError]] = _ => js.undefined
  
  private def defaultErrorHandler(error: Any): TaskError = {
    val details: js.UndefOr[String] = error match {
      case t: Throwable => TaskManager.printStackTrace(t)
      case _ => js.undefined
    }
    TaskError(error.toString, details)
  }
  
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
      errorHandler(e).fold(defaultErrorHandler(e))(identity)
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

  def printStackTrace(x: Throwable, sep: String = "&nbsp"): String = {
    val sb = new StringBuilder(x.toString)
    val trace = x.getStackTrace
    for (t <- trace) {
      sb.append(s"\n\tat$sep").append(t)
    }

    val cause = x.getCause
    if (cause != null) {
      printStackTraceAsCause(sb, cause, trace, sep)
    }

    sb.toString
  }

  /**
    * Print stack trace as a cause for the specified stack trace.
    */
  @annotation.tailrec
  private def printStackTraceAsCause(sb: StringBuilder,
                                     cause: Throwable,
                                     causedTrace: Array[StackTraceElement],
                                     sep: String): Unit = {

    // Compute number of frames in common between this and caused
    val trace = cause.getStackTrace
    var m = trace.length - 1
    var n = causedTrace.length - 1
    while (m >= 0 && n >= 0 && trace(m) == causedTrace(n)) {
      m -= 1
      n -= 1
    }

    val framesInCommon = trace.length - 1 - m
    sb.append("\nCaused by: " + cause)

    for (i <- 0 to m) {
      sb.append(s"\n\tat$sep").append(trace(i))
    }

    if (framesInCommon != 0) {
      sb.append(s"\n\t...$sep").append(framesInCommon).append(s"${sep}more")
    }

    // Recurse if we have a cause
    val ourCause = cause.getCause
    if (ourCause != null) {
      printStackTraceAsCause(sb, ourCause, trace, sep)
    }
  }
}
