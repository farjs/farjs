package farjs.app.task

import farjs.app.task.FarjsTaskManagerUi._
import farjs.ui.popup._
import farjs.ui.task.{TaskManager, TaskManagerUiProps}
import farjs.ui.theme.Theme
import scommons.react.test._

import scala.scalajs.js.JavaScriptException
import scala.util.Failure

class FarjsTaskManagerUiSpec extends TestSpec with TestRendererUtils {

  FarjsTaskManagerUi.statusPopupComp = mockUiComponent("TaskStatusPopup")
  FarjsTaskManagerUi.messageBoxComp = mockUiComponent("MessageBox")

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
    val renderer = createTestRenderer(<(FarjsTaskManagerUi())(^.wrapped := props)())
    val msgBox = findComponentProps(renderer.root, messageBoxComp, plain = true)

    //then
    onCloseErrorPopup.expects()

    //when
    msgBox.actions.head.onAction()

    //then
    findProps(renderer.root, messageBoxComp, plain = true) should be (empty)
  }

  it should "not hide previous error popup when no error in updated props" in {
    //given
    val onCloseErrorPopup = mockFunction[Unit]
    val props = getTaskManagerUiProps(
      error = Some("Some error"),
      onCloseErrorPopup = onCloseErrorPopup
    )
    val renderer = createTestRenderer(<(FarjsTaskManagerUi())(^.wrapped := props)())
    findComponentProps(renderer.root, messageBoxComp, plain = true).message shouldBe "Some error"

    //then
    onCloseErrorPopup.expects().never()
    
    //when
    TestRenderer.act { () =>
      renderer.update(<(FarjsTaskManagerUi())(^.wrapped := props.copy(error = None))())
    }

    //then
    findComponentProps(renderer.root, messageBoxComp, plain = true).message shouldBe "Some error"
  }

  it should "stack error popups when several failed tasks" in {
    //given
    val onCloseErrorPopup = mockFunction[Unit]
    val props = getTaskManagerUiProps(
      error = Some("Some error"),
      onCloseErrorPopup = onCloseErrorPopup
    )
    val renderer = createTestRenderer(<(FarjsTaskManagerUi())(^.wrapped := props)())
    findComponentProps(renderer.root, messageBoxComp, plain = true).message shouldBe "Some error"

    //when & then
    TestRenderer.act { () =>
      renderer.update(<(FarjsTaskManagerUi())(^.wrapped := props.copy(error = Some("Test error2")))())
    }
    findComponentProps(renderer.root, messageBoxComp, plain = true).message shouldBe "Test error2"

    //then
    onCloseErrorPopup.expects()
    
    //when & then
    findComponentProps(renderer.root, messageBoxComp, plain = true).actions.head.onAction()
    findComponentProps(renderer.root, messageBoxComp, plain = true).message shouldBe "Some error"

    //then
    onCloseErrorPopup.expects()

    //when & then
    findComponentProps(renderer.root, messageBoxComp, plain = true).actions.head.onAction()
    findProps(renderer.root, messageBoxComp, plain = true) should be (empty)
  }

  it should "render only status popup if loading and prev error" in {
    //given
    val props = getTaskManagerUiProps(
      showLoading = true,
      status = Some("Some status message"),
      error = Some("Error: \nSome prev error "),
      errorDetails = Some("Some prev error details")
    )
    val component = <(FarjsTaskManagerUi())(^.wrapped := props)()

    //when
    val result = createTestRenderer(component).root

    //then
    inside(result.children.toList) { case List(status) =>
      assertTestComponent(status, statusPopupComp) {
        case StatusPopupProps(message, title, closable, _) =>
          message shouldBe props.status.getOrElse("")
          title shouldBe "Status"
          closable shouldBe false
      }
    }
  }

  it should "render MessageBox if error and no status/loading" in {
    //given
    val props = getTaskManagerUiProps(
      status = Some("Test status..."),
      error = Some("Error: \nSome error "),
      errorDetails = Some("Some error details")
    )
    val component = <(FarjsTaskManagerUi())(^.wrapped := props)()

    //when
    val result = createTestRenderer(component).root

    //then
    inside(result.children.toList) { case List(msgBox) =>
      assertTestComponent(msgBox, messageBoxComp, plain = true) {
        case MessageBoxProps(title, message, resActions, style) =>
          title shouldBe "Error"
          message shouldBe "Some error"
          inside(resActions.toList) { case List(ok) =>
            ok.label shouldBe "OK"
            ok.triggeredOnClose shouldBe true
          }
          style shouldBe Theme.current.popup.error
      }
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
