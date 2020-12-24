package farjs.app

import farjs.ui.popup._
import scommons.react._
import scommons.react.redux.task.{TaskManager, TaskManagerUiProps}

import scala.scalajs.js.JavaScriptException
import scala.util.{Failure, Try}

/**
  * Displays status of running tasks.
  */
object FarjsTaskManagerUi extends FunctionComponent[TaskManagerUiProps] {

  private[app] var logger: String => Unit = println
  private[app] var messageBoxComp: UiComponent[MessageBoxProps] = MessageBox
  
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

    if (showError) {
      <(messageBoxComp())(^.wrapped := MessageBoxProps(
        title = "Error",
        message = errorMessage,
        //message = s"$errorMessage${props.errorDetails.map(d => s"\n\n$d").getOrElse("")}",
        actions = List(MessageBoxAction.OK(props.onCloseErrorPopup)),
        style = Popup.Styles.error
      ))()
    } else null
  }
}
