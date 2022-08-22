package farjs.filelist.popups

import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.popups.HelpController._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react.test._

class HelpControllerSpec extends TestSpec with TestRendererUtils {

  HelpController.messageBoxComp = mockUiComponent("MessageBox")

  it should "dispatch FileListPopupHelpAction when OK action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showHelpPopup = true))
    val comp = testRender(<(HelpController())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, messageBoxComp, plain = true)
    val action = FileListPopupHelpAction(show = false)

    //then
    dispatch.expects(action)

    //when
    msgBox.actions.head.onAction()
  }

  it should "render popup component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showHelpPopup = true))

    //when
    val result = testRender(<(HelpController())(^.wrapped := props)())

    //then
    assertTestComponent(result, messageBoxComp, plain = true) {
      case MessageBoxProps(title, message, resActions, style) =>
        title shouldBe "Help"
        message shouldBe "//TODO: show help/about info"
        inside(resActions.toList) {
          case List(MessageBoxAction("OK", _, true)) =>
        }
        style shouldBe Theme.current.popup.regular
    }
  }

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState())

    //when
    val renderer = createTestRenderer(<(HelpController())(^.wrapped := props)())

    //then
    renderer.root.children.toList should be (empty)
  }
}
