package farjs.filelist.popups

import farjs.filelist.FileListUiData
import farjs.filelist.popups.HelpController._
import farjs.ui.popup._
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import scommons.react.ReactClass
import scommons.react.test._

class HelpControllerSpec extends TestSpec with TestRendererUtils {

  HelpController.messageBoxComp = "MessageBox".asInstanceOf[ReactClass]

  it should "dispatch FileListPopupHelpAction when OK action" in {
    //given
    val onClose = mockFunction[Unit]
    val props = FileListUiData(showHelpPopup = true, onClose = onClose)
    val comp = testRender(withThemeContext(<(HelpController())(^.wrapped := props)()))
    val msgBox = inside(findComponents(comp, messageBoxComp)) {
      case List(msgBox) => msgBox.props.asInstanceOf[MessageBoxProps]
    }

    //then
    onClose.expects()

    //when
    msgBox.actions.head.onAction()
  }

  it should "render popup component" in {
    //given
    val props = FileListUiData(showHelpPopup = true)

    //when
    val result = testRender(withThemeContext(<(HelpController())(^.wrapped := props)()))

    //then
    val currTheme = DefaultTheme
    assertNativeComponent(result, <(messageBoxComp)(^.assertPlain[MessageBoxProps](inside(_) {
      case MessageBoxProps(title, message, resActions, style) =>
        title shouldBe "Help"
        message shouldBe "//TODO: show help/about info"
        inside(resActions.toList) {
          case List(MessageBoxAction("OK", _, true)) =>
        }
        style shouldBe currTheme.popup.regular
    }))())
  }

  it should "render empty component" in {
    //given
    val props = FileListUiData()

    //when
    val renderer = createTestRenderer(withThemeContext(<(HelpController())(^.wrapped := props)()))

    //then
    renderer.root.children.toList should be (empty)
  }
}
