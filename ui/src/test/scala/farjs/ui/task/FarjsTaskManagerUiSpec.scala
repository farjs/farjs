package farjs.ui.task

import farjs.ui.popup._
import farjs.ui.task.FarjsTaskManagerUi._
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException
import scala.util.Failure

class FarjsTaskManagerUiSpec extends TestSpec with TestRendererUtils {

  FarjsTaskManagerUi.statusPopupComp = "TaskStatusPopup".asInstanceOf[ReactClass]
  FarjsTaskManagerUi.messageBoxComp = "MessageBox".asInstanceOf[ReactClass]

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
    val renderer = createTestRenderer(withThemeContext(<(FarjsTaskManagerUi())(^.wrapped := props)()))
    val msgBox = inside(findComponents(renderer.root, messageBoxComp)) {
      case List(msgBox) => msgBox.props.asInstanceOf[MessageBoxProps]
    }

    //then
    onCloseErrorPopup.expects()

    //when
    msgBox.actions.head.onAction()

    //then
    findComponents(renderer.root, messageBoxComp) should be (empty)
  }

  it should "not hide previous error popup when no error in updated props" in {
    //given
    val onCloseErrorPopup = mockFunction[Unit]
    val props = getTaskManagerUiProps(
      error = Some("Some error"),
      onCloseErrorPopup = onCloseErrorPopup
    )
    val renderer = createTestRenderer(withThemeContext(<(FarjsTaskManagerUi())(^.wrapped := props)()))
    inside(findComponents(renderer.root, messageBoxComp)) {
      case List(msgBox) => msgBox.props.asInstanceOf[MessageBoxProps].message shouldBe "Some error"
    }

    //then
    onCloseErrorPopup.expects().never()
    
    //when
    TestRenderer.act { () =>
      renderer.update(withThemeContext(<(FarjsTaskManagerUi())(^.wrapped := props.copy(error = None))()))
    }

    //then
    inside(findComponents(renderer.root, messageBoxComp)) {
      case List(msgBox) => msgBox.props.asInstanceOf[MessageBoxProps].message shouldBe "Some error"
    }
  }

  it should "stack error popups when several failed tasks" in {
    //given
    val onCloseErrorPopup = mockFunction[Unit]
    val props = getTaskManagerUiProps(
      error = Some("Some error"),
      onCloseErrorPopup = onCloseErrorPopup
    )
    val renderer = createTestRenderer(withThemeContext(<(FarjsTaskManagerUi())(^.wrapped := props)()))
    inside(findComponents(renderer.root, messageBoxComp)) {
      case List(msgBox) => msgBox.props.asInstanceOf[MessageBoxProps].message shouldBe "Some error"
    }

    //when & then
    TestRenderer.act { () =>
      renderer.update(withThemeContext(
        <(FarjsTaskManagerUi())(^.wrapped := props.copy(error = Some("Test error2")))()
      ))
    }
    inside(findComponents(renderer.root, messageBoxComp)) {
      case List(msgBox) => msgBox.props.asInstanceOf[MessageBoxProps].message shouldBe "Test error2"
    }

    //then
    onCloseErrorPopup.expects()
    
    //when & then
    inside(findComponents(renderer.root, messageBoxComp)) { case List(msgBox) =>
      msgBox.props.asInstanceOf[MessageBoxProps].actions.head.onAction()
      msgBox.props.asInstanceOf[MessageBoxProps].message shouldBe "Some error"
    }

    //then
    onCloseErrorPopup.expects()

    //when & then
    inside(findComponents(renderer.root, messageBoxComp)) {
      case List(msgBox) => msgBox.props.asInstanceOf[MessageBoxProps].actions.head.onAction()
    }
    findComponents(renderer.root, messageBoxComp) should be (empty)
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
    val result = createTestRenderer(withThemeContext(component)).root

    //then
    inside(result.children.toList) { case List(status) =>
      assertNativeComponent(status, <(statusPopupComp)(^.assertPlain[StatusPopupProps](inside(_) {
        case StatusPopupProps(message, title, onClose) =>
          message shouldBe props.status.getOrElse("")
          title shouldBe js.undefined
          onClose shouldBe js.undefined
      }))())
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
    val result = createTestRenderer(withThemeContext(component)).root

    //then
    inside(result.children.toList) { case List(msgBox) =>
      assertNativeComponent(msgBox, <(messageBoxComp)(^.assertPlain[MessageBoxProps](inside(_) {
        case MessageBoxProps(title, message, resActions, style) =>
          title shouldBe "Error"
          message shouldBe "Some error"
          inside(resActions.toList) { case List(ok) =>
            ok.label shouldBe "OK"
            ok.triggeredOnClose shouldBe true
          }
          style shouldBe DefaultTheme.popup.error
      }))())
    }
  }

  it should "render nothing if no loading and no error" in {
    //given
    val props = getTaskManagerUiProps()
    val component = <(FarjsTaskManagerUi())(^.wrapped := props)()

    //when
    val result = createTestRenderer(withThemeContext(component)).root

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
