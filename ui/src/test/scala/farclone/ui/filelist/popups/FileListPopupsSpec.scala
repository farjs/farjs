package farclone.ui.filelist.popups

import farclone.ui.filelist.popups.FileListPopupsActions._
import farclone.ui.popup._
import scommons.react._
import scommons.react.test.TestSpec
import scommons.react.test.util.ShallowRendererUtils

class FileListPopupsSpec extends TestSpec with ShallowRendererUtils {

  it should "dispatch FileListHelpAction when OK action in helpPopup" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showHelpPopup = true))
    val comp = shallowRender(<(FileListPopups())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, MessageBox)
    val action = FileListHelpAction(show = false)

    //then
    dispatch.expects(action)

    //when
    msgBox.actions.head.onAction()
  }

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState())

    //when
    val result = shallowRender(<(FileListPopups())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.>()())
  }
  
  it should "render help popup" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showHelpPopup = true))

    //when
    val result = shallowRender(<(FileListPopups())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.>()(), { case List(helpPopup) =>
      assertComponent(helpPopup, MessageBox) {
        case MessageBoxProps(title, message, actions, style) =>
          title shouldBe "Help"
          message shouldBe "//TODO: show help/about info"
          inside(actions) {
            case List(MessageBoxAction("OK", _, true)) =>
          }
          style shouldBe Popup.Styles.normal
      }
    })
  }
}
