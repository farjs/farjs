package farjs.ui.task

import farjs.ui.popup._
import farjs.ui.task.FarjsTaskManagerUi._
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

class FarjsTaskManagerUiSpec extends TestSpec with TestRendererUtils {

  FarjsTaskManagerUi.statusPopupComp = "TaskStatusPopup".asInstanceOf[ReactClass]
  FarjsTaskManagerUi.messageBoxComp = "MessageBox".asInstanceOf[ReactClass]

  it should "return error if JavaScriptException in errorHandler" in {
    //given
    val currLogger = FarjsTaskManagerUi.logger
    val logger = mockFunction[String, Unit]
    FarjsTaskManagerUi.logger = logger
    val ex = JavaScriptException("test error")
    val value = ex
    val stackTrace = TaskManager.printStackTrace(ex, sep = " ")

    //then
    logger.expects(stackTrace)
    
    //when
    val TaskError(error, errorDetails) = FarjsTaskManagerUi.errorHandler(value).get

    //then
    error shouldBe "test error"
    errorDetails shouldBe stackTrace
    
    //cleanup
    FarjsTaskManagerUi.logger = currLogger
  }

  it should "return error if non-JavaScriptException in errorHandler" in {
    //given
    val currLogger = FarjsTaskManagerUi.logger
    val logger = mockFunction[String, Unit]
    FarjsTaskManagerUi.logger = logger
    val ex = new Exception("test error")
    val value = ex
    val stackTrace = TaskManager.printStackTrace(ex, sep = " ")

    //then
    logger.expects(stackTrace)

    //when
    val TaskError(error, errorDetails) = FarjsTaskManagerUi.errorHandler(value).get

    //then
    error shouldBe s"$ex"
    errorDetails shouldBe stackTrace

    //cleanup
    FarjsTaskManagerUi.logger = currLogger
  }

  it should "return error without details if custom error in errorHandler" in {
    //given
    val currLogger = FarjsTaskManagerUi.logger
    val logger = mockFunction[String, Unit]
    FarjsTaskManagerUi.logger = logger
    val ex = "test error"
    val value = ex

    //then
    logger.expects(ex)

    //when
    val TaskError(error, errorDetails) = FarjsTaskManagerUi.errorHandler(value).get

    //then
    error shouldBe s"$ex"
    errorDetails shouldBe js.undefined

    //cleanup
    FarjsTaskManagerUi.logger = currLogger
  }

  it should "call onCloseErrorPopup function when OK action in error popup" in {
    //given
    val onCloseErrorPopup = mockFunction[Unit]
    val props = getTaskManagerUiProps(
      error = "Some error",
      onCloseErrorPopup = onCloseErrorPopup
    )
    val renderer = createTestRenderer(withThemeContext(<(FarjsTaskManagerUi())(^.plain := props)()))
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
      error = "Some error",
      onCloseErrorPopup = onCloseErrorPopup
    )
    val renderer = createTestRenderer(withThemeContext(<(FarjsTaskManagerUi())(^.plain := props)()))
    inside(findComponents(renderer.root, messageBoxComp)) {
      case List(msgBox) => msgBox.props.asInstanceOf[MessageBoxProps].message shouldBe "Some error"
    }

    //then
    onCloseErrorPopup.expects().never()
    
    //when
    TestRenderer.act { () =>
      renderer.update(withThemeContext(<(FarjsTaskManagerUi())(^.plain := TaskManagerUiProps.copy(props)(error = js.undefined))()))
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
      error = "Some error",
      onCloseErrorPopup = onCloseErrorPopup
    )
    val renderer = createTestRenderer(withThemeContext(<(FarjsTaskManagerUi())(^.plain := props)()))
    inside(findComponents(renderer.root, messageBoxComp)) {
      case List(msgBox) => msgBox.props.asInstanceOf[MessageBoxProps].message shouldBe "Some error"
    }

    //when & then
    TestRenderer.act { () =>
      renderer.update(withThemeContext(
        <(FarjsTaskManagerUi())(^.plain := TaskManagerUiProps.copy(props)(error = "Test error2"))()
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
      status = "Some status message",
      error = "Error: \nSome prev error ",
      errorDetails = "Some prev error details"
    )
    val component = <(FarjsTaskManagerUi())(^.plain := props)()

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
      status = "Test status...",
      error = "Error: \nSome error ",
      errorDetails = "Some error details"
    )
    val component = <(FarjsTaskManagerUi())(^.plain := props)()

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
    val component = <(FarjsTaskManagerUi())(^.plain := props)()

    //when
    val result = createTestRenderer(withThemeContext(component)).root

    //then
    result.children.toList should be (empty)
  }

  private def getTaskManagerUiProps(showLoading: Boolean = false,
                                    status: js.UndefOr[String] = js.undefined,
                                    error: js.UndefOr[String] = js.undefined,
                                    errorDetails: js.UndefOr[String] = js.undefined,
                                    onCloseErrorPopup: () => Unit = () => ()
                                   ): TaskManagerUiProps = {
    TaskManagerUiProps(
      showLoading = showLoading,
      onHideStatus = () => (),
      onCloseErrorPopup = onCloseErrorPopup,
      status = status,
      error = error,
      errorDetails = errorDetails
    )
  }
}
