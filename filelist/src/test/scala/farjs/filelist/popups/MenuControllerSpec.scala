package farjs.filelist.popups

import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.popups.MenuController._
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

class MenuControllerSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  MenuController.menuBarComp = mockUiComponent("MenuBar")

  it should "dispatch FileListPopupMenuAction when Close action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showMenuPopup = true))
    val comp = testRender(<(MenuController())(^.wrapped := props)())
    val popup = findComponentProps(comp, menuBarComp)

    //then
    dispatch.expects(FileListPopupMenuAction(show = false))

    //when
    popup.onClose()

    Succeeded
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

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState())

    //when
    val renderer = createTestRenderer(<(MenuController())(^.wrapped := props)())

    //then
    renderer.root.children.toList should be (empty)
  }
}
