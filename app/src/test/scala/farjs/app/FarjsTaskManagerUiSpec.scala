package farjs.app

import farjs.ui.popup._
import org.scalatest.Succeeded
import scommons.react._
import scommons.react.redux.task.{TaskManager, TaskManagerUiProps}
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

import scala.scalajs.js.JavaScriptException
import scala.util.Failure

class FarjsTaskManagerUiSpec extends TestSpec with ShallowRendererUtils {

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
    val comp = shallowRender(<(FarjsTaskManagerUi())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, MessageBox)

    //then
    onCloseErrorPopup.expects()

    //when
    msgBox.actions.head.onAction()
  }

  it should "render error" in {
    //given
    val props = getTaskManagerUiProps(
      error = Some("Some error"),
      errorDetails = Some("Some error details")
    )
    val component = <(FarjsTaskManagerUi())(^.wrapped := props)()

    //when
    val result = shallowRender(component)

    //then
    assertRenderingResult(result, props)
  }

  private def getTaskManagerUiProps(showLoading: Boolean = false,
                                    status: Option[String] = None,
                                    onHideStatus: () => Unit = () => (),
                                    error: Option[String],
                                    errorDetails: Option[String] = None,
                                    onCloseErrorPopup: () => Unit = () => ()
                                   ): TaskManagerUiProps = {
    TaskManagerUiProps(
      showLoading,
      status,
      onHideStatus,
      error,
      errorDetails,
      onCloseErrorPopup
    )
  }
  
  private def assertRenderingResult(result: ShallowInstance, props: TaskManagerUiProps): Unit = {
    val showError = props.error.isDefined
    
    assertNativeComponent(result, <.>()(), { children =>
      val errorPopup = children match {
        case List(ep) if showError => Some(ep)
      }

      if (showError) {
        errorPopup should not be None
        assertComponent(errorPopup.get, MessageBox) {
          case MessageBoxProps(title, message, actions, style) =>
            title shouldBe "Error"
            message shouldBe props.error.getOrElse("")
            //details shouldBe props.errorDetails
            actions shouldBe List(MessageBoxAction.OK(props.onCloseErrorPopup))
            style shouldBe Popup.Styles.error
        }
      }
      
      Succeeded
    })
  }
}
