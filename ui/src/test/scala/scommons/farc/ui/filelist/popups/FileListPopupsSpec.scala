package scommons.farc.ui.filelist.popups

import scommons.farc.ui.filelist.popups.FileListPopupsActions._
import scommons.farc.ui.popup._
import scommons.react._
import scommons.react.test.TestSpec
import scommons.react.test.util.ShallowRendererUtils

class FileListPopupsSpec extends TestSpec with ShallowRendererUtils {

  it should "dispatch FileListHelpAction when onClose in helpPopup" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showHelpPopup = true))
    val comp = shallowRender(<(FileListPopups())(^.wrapped := props)())
    val popupProps = findComponentProps(comp, OkPopup)
    val action = FileListHelpAction(show = false)

    //then
    dispatch.expects(action)

    //when
    popupProps.onClose()
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
      assertComponent(helpPopup, OkPopup) { case OkPopupProps(title, message, style, _) =>
        title shouldBe "Help"
        message shouldBe "//TODO: show help/about info"
        style shouldBe Popup.Styles.normal
      }
    })
  }
}
