package farclone.app

import farclone.ui.popup.{OkPopup, OkPopupProps, Popup}
import scommons.react._
import scommons.react.redux.task.TaskManagerUiProps

import scala.scalajs.js.JavaScriptException
import scala.util.{Failure, Try}

/**
  * Displays status of running tasks.
  */
object FarcTaskManagerUi extends FunctionComponent[TaskManagerUiProps] {

  var errorHandler: PartialFunction[Try[_], (Option[String], Option[String])] = {
    case Failure(JavaScriptException(error)) =>
      println(s"$error")
      (Some(s"$error"), None)
    case Failure(error) =>
      println(s"$error")
      (Some(s"$error"), None)
  }

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val showError = props.error.isDefined
    val errorMessage = props.error.getOrElse("")

    <.>()(
      if (showError) Some(
        <(OkPopup())(^.wrapped := OkPopupProps(
          title = "Error",
          message = errorMessage, //props.errorDetails
          style = Popup.Styles.error,
          onClose = props.onCloseErrorPopup
        ))()
      ) else None
    )
  }
}
