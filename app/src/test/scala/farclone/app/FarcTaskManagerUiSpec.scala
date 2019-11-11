package farclone.app

import farclone.ui.popup.{OkPopup, OkPopupProps, Popup}
import org.scalatest.Succeeded
import scommons.react._
import scommons.react.redux.task.{TaskManager, TaskManagerUiProps}
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

import scala.scalajs.js.JavaScriptException
import scala.util.Failure

class FarcTaskManagerUiSpec extends TestSpec with ShallowRendererUtils {

  it should "return error if JavaScriptException in errorHandler" in {
    //given
    val currLogger = FarcTaskManagerUi.logger
    val logger = mockFunction[String, Unit]
    FarcTaskManagerUi.logger = logger
    val ex = JavaScriptException("test error")
    val value = Failure(ex)
    val stackTrace = TaskManager.printStackTrace(ex, sep = " ")

    //then
    logger.expects(stackTrace)
    
    //when
    val (error, errorDetails) = FarcTaskManagerUi.errorHandler(value)

    //then
    error shouldBe Some("test error")
    errorDetails shouldBe Some(stackTrace)
    
    //cleanup
    FarcTaskManagerUi.logger = currLogger
  }

  it should "return error if non-JavaScriptException in errorHandler" in {
    //given
    val currLogger = FarcTaskManagerUi.logger
    val logger = mockFunction[String, Unit]
    FarcTaskManagerUi.logger = logger
    val ex = new Exception("test error")
    val value = Failure(ex)
    val stackTrace = TaskManager.printStackTrace(ex, sep = " ")

    //then
    logger.expects(stackTrace)

    //when
    val (error, errorDetails) = FarcTaskManagerUi.errorHandler(value)

    //then
    error shouldBe Some(s"$ex")
    errorDetails shouldBe Some(stackTrace)

    //cleanup
    FarcTaskManagerUi.logger = currLogger
  }

  it should "call onCloseErrorPopup function when onClose error popup" in {
    //given
    val onCloseErrorPopup = mockFunction[Unit]
    val props = getTaskManagerUiProps(
      error = Some("Some error"),
      onCloseErrorPopup = onCloseErrorPopup
    )
    val comp = shallowRender(<(FarcTaskManagerUi())(^.wrapped := props)())
    val errorProps = findComponentProps(comp, OkPopup)

    //then
    onCloseErrorPopup.expects()

    //when
    errorProps.onClose()
  }

  it should "render error" in {
    //given
    val props = getTaskManagerUiProps(
      error = Some("Some error"),
      errorDetails = Some("Some error details")
    )
    val component = <(FarcTaskManagerUi())(^.wrapped := props)()

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
                                    onCloseErrorPopup: () => Unit = () => ()): TaskManagerUiProps = {

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
        assertComponent(errorPopup.get, OkPopup) { case OkPopupProps(title, message, style, onClose) =>
          title shouldBe "Error"
          message shouldBe props.error.getOrElse("")
          //details shouldBe props.errorDetails
          style shouldBe Popup.Styles.error
          onClose shouldBe props.onCloseErrorPopup
        }
      }
      
      Succeeded
    })
  }
}
