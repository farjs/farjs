package farjs.filelist.popups

import farjs.filelist.popups.ExitController._
import farjs.filelist.popups.FileListPopupsActions._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.nodejs._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class ExitControllerSpec extends TestSpec with TestRendererUtils {

  ExitController.messageBoxComp = mockUiComponent("MessageBox")

  it should "dispatch FileListPopupExitAction and emit Ctrl+Q when YES action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showExitPopup = true))
    val comp = testRender(<(ExitController())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, messageBoxComp)
    val action = FileListPopupExitAction(show = false)

    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)

    //then
    dispatch.expects(action)
    onKey.expects("e", true, false, false)

    //when
    msgBox.actions.head.onAction()

    //cleanup
    process.stdin.removeListener("keypress", listener)
  }

  it should "dispatch FileListPopupExitAction when NO action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showExitPopup = true))
    val comp = testRender(<(ExitController())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, messageBoxComp)
    val action = FileListPopupExitAction(show = false)

    //then
    dispatch.expects(action)

    //when
    msgBox.actions(1).onAction()
  }

  it should "render popup component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showExitPopup = true))

    //when
    val result = testRender(<(ExitController())(^.wrapped := props)())

    //then
    assertTestComponent(result, messageBoxComp) {
      case MessageBoxProps(title, message, resActions, style) =>
        title shouldBe "Exit"
        message shouldBe "Do you really want to exit FAR.js?"
        inside(resActions) {
          case List(MessageBoxAction("YES", _, false), MessageBoxAction("NO", _, true)) =>
        }
        style shouldBe Theme.current.popup.regular
    }
  }

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState())

    //when
    val renderer = createTestRenderer(<(ExitController())(^.wrapped := props)())

    //then
    renderer.root.children.toList should be (empty)
  }
}
