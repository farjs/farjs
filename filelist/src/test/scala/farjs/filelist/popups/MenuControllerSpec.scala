package farjs.filelist.popups

import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.popups.MenuController._
import farjs.ui.popup.PopupOverlay
import org.scalatest.Succeeded
import scommons.react.blessed._
import scommons.react.test._

class MenuControllerSpec extends TestSpec with TestRendererUtils {

  MenuController.menuBarComp = mockUiComponent("MenuBar")

  it should "dispatch FileListPopupMenuAction(show=false) when onClose" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showMenuPopup = true))
    val comp = testRender(<(MenuController())(^.wrapped := props)())
    val popup = findComponentProps(comp, menuBarComp)

    //then
    dispatch.expects(FileListPopupMenuAction(show = false))

    //when
    popup.onClose()
  }

  it should "dispatch FileListPopupMenuAction(show=true) when onClick" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState())
    val comp = testRender(<(MenuController())(^.wrapped := props)())
    val boxComp = inside(findComponents(comp, <.box.name)) {
      case List(box) => box
    }

    //then
    dispatch.expects(FileListPopupMenuAction(show = true))

    //when
    boxComp.props.onClick(null)
  }

  it should "render MenuBar component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showMenuPopup = true))

    //when
    val result = testRender(<(MenuController())(^.wrapped := props)())

    //then
    assertTestComponent(result, menuBarComp) {
      case MenuBarProps(_) => Succeeded
    }
  }

  it should "render clickable box component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState())

    //when
    val result = testRender(<(MenuController())(^.wrapped := props)())

    //then
    assertNativeComponent(result,
      <.box(
        ^.rbHeight := 1,
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := PopupOverlay.style
      )()
    )
  }
}
