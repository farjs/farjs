package farclone.app

import farclone.ui.popup.{OkPopup, OkPopupProps, Popup}
import scommons.react._
import scommons.react.redux.task.{TaskManager, TaskManagerUiProps}

import scala.scalajs.js.JavaScriptException
import scala.util.{Failure, Try}

/**
  * Displays status of running tasks.
  */
object FarcTaskManagerUi extends FunctionComponent[TaskManagerUiProps] {

  private[app] var logger: String => Unit = println
  
  val errorHandler: PartialFunction[Try[_], (Option[String], Option[String])] = {
    case Failure(ex@JavaScriptException(error)) =>
      val stackTrace = TaskManager.printStackTrace(ex, sep = " ")
      logger(stackTrace)
      (Some(s"$error"), Some(stackTrace))
    case Failure(ex) =>
      val stackTrace = TaskManager.printStackTrace(ex, sep = " ")
      logger(stackTrace)
      (Some(s"$ex"), Some(stackTrace))
  }

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val showError = props.error.isDefined
    val errorMessage = props.error.getOrElse("")

    <.>()(
      if (showError) Some(
        <(OkPopup())(^.wrapped := OkPopupProps(
          title = "Error",
          message = s"$errorMessage${props.errorDetails.map(d => s"\n\n$d").getOrElse("")}",
          style = Popup.Styles.error,
          onClose = props.onCloseErrorPopup
        ))()
      ) else None
    )
  }
}
