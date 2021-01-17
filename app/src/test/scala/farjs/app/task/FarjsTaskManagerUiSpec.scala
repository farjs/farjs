package farjs.app.task

import farjs.app.task.FarjsTaskManagerUi._
import farjs.ui.popup._
import scommons.react._
import scommons.react.redux.task._
import scommons.react.test._

import scala.scalajs.js.JavaScriptException
import scala.util.Failure

class FarjsTaskManagerUiSpec extends TestSpec with TestRendererUtils {

  FarjsTaskManagerUi.statusPopupComp = () => "TaskStatusPopup".asInstanceOf[ReactClass]
  FarjsTaskManagerUi.messageBoxComp = () => "MessageBox".asInstanceOf[ReactClass]

  it should "return error if JavaScriptException in errorHandler" in {
    //given
    val currLogger = FarjsTaskManagerUi.logger
    val logger = mockFunction[String, Unit]
    FarjsTaskManagerUi.logger = logger
    val ex = JavaScriptException("test error")
    val value = Failure(ex)
    val stackTrace = TaskManager.printStackTrace(ex, sep = " ")

    //then
    logger.expects(stackTrace)
    
    //when
    val (error, errorDetails) = FarjsTaskManagerUi.errorHandler(value)

    //then
    error shouldBe Some("test error")
    errorDetails shouldBe Some(stackTrace)
    
    //cleanup
    FarjsTaskManagerUi.logger = currLogger
  }

  it should "return error if non-JavaScriptException in errorHandler" in {
    //given
    val currLogger = FarjsTaskManagerUi.logger
    val logger = mockFunction[String, Unit]
    FarjsTaskManagerUi.logger = logger
    val ex = new Exception("test error")
    val value = Failure(ex)
    val stackTrace = TaskManager.printStackTrace(ex, sep = " ")

    //then
    logger.expects(stackTrace)

    //when
    val (error, errorDetails) = FarjsTaskManagerUi.errorHandler(value)

    //then
    error shouldBe Some(s"$ex")
    errorDetails shouldBe Some(stackTrace)

    //cleanup
    FarjsTaskManagerUi.logger = currLogger
  }

  it should "call onCloseErrorPopup function when OK action in error popup" in {
    //given
    val onCloseErrorPopup = mockFunction[Unit]
    val props = getTaskManagerUiProps(
      error = Some("Some error"),
      onCloseErrorPopup = onCloseErrorPopup
    )
    val comp = testRender(<(FarjsTaskManagerUi())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, messageBoxComp)

    //then
    onCloseErrorPopup.expects()

    //when
    msgBox.actions.head.onAction()
  }

  it should "render status popup if loading" in {
    //given
    val props = getTaskManagerUiProps(
      showLoading = true,
      status = Some("Some status message")
    )
    val component = <(FarjsTaskManagerUi())(^.wrapped := props)()

    //when
    val result = testRender(component)

    //then
    assertTestComponent(result, statusPopupComp) {
      case TaskStatusPopupProps(message) =>
        message shouldBe props.status.getOrElse("")
    }
  }

  it should "render MessageBox if error" in {
    //given
    val props = getTaskManagerUiProps(
      error = Some("Some error"),
      errorDetails = Some("Some error details")
    )
    val component = <(FarjsTaskManagerUi())(^.wrapped := props)()

    //when
    val result = testRender(component)

    //then
    assertTestComponent(result, messageBoxComp) {
      case MessageBoxProps(title, message, actions, style) =>
        title shouldBe "Error"
        message shouldBe props.error.getOrElse("")
        //details shouldBe props.errorDetails
        actions shouldBe List(MessageBoxAction.OK(props.onCloseErrorPopup))
        style shouldBe Popup.Styles.error
    }
  }

  it should "render nothing if no loading and no error" in {
    //given
    val props = getTaskManagerUiProps()
    val component = <(FarjsTaskManagerUi())(^.wrapped := props)()

    //when
    val result = createTestRenderer(component).root

    //then
    result.children.toList should be (empty)
  }

  private def getTaskManagerUiProps(showLoading: Boolean = false,
                                    status: Option[String] = None,
                                    error: Option[String] = None,
                                    errorDetails: Option[String] = None,
                                    onCloseErrorPopup: () => Unit = () => ()
                                   ): TaskManagerUiProps = {
    TaskManagerUiProps(
      showLoading,
      status,
      onHideStatus = () => (),
      error,
      errorDetails,
      onCloseErrorPopup
    )
  }
}
