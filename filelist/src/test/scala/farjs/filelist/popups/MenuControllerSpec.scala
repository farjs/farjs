package farjs.filelist.popups

import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.popups.MenuController._
import farjs.filelist.stack.{PanelStack, WithPanelStacksSpec}
import farjs.ui.popup.PopupOverlay
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class MenuControllerSpec extends TestSpec with TestRendererUtils {

  MenuController.menuBarComp = mockUiComponent("MenuBar")

  it should "dispatch FileListPopupMenuAction(show=false) when onClose" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showMenuPopup = true))
    val comp = testRender(WithPanelStacksSpec.withContext(
      <(MenuController())(^.wrapped := props)(),
      leftStack = new PanelStack(isActive = true, Nil, null),
      rightStack = new PanelStack(isActive = false, Nil, null)
    ))
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
    val comp = testRender(WithPanelStacksSpec.withContext(
      <(MenuController())(^.wrapped := props)(),
      leftStack = new PanelStack(isActive = true, Nil, null),
      rightStack = new PanelStack(isActive = false, Nil, null)
    ))
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
    val leftInput = js.Dynamic.literal().asInstanceOf[BlessedElement]
    val rightInput = js.Dynamic.literal().asInstanceOf[BlessedElement]

    //when
    val result = testRender(WithPanelStacksSpec.withContext(
      <(MenuController())(^.wrapped := props)(),
      leftStack = new PanelStack(isActive = true, Nil, null),
      rightStack = new PanelStack(isActive = false, Nil, null),
      leftInput = leftInput,
      rightInput = rightInput
    ))

    //then
    assertTestComponent(result, menuBarComp) {
      case MenuBarProps(left, right, _) =>
        left shouldBe leftInput
        right shouldBe rightInput
    }
  }

  it should "render clickable box component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState())

    //when
    val result = testRender(WithPanelStacksSpec.withContext(
      <(MenuController())(^.wrapped := props)(),
      leftStack = new PanelStack(isActive = true, Nil, null),
      rightStack = new PanelStack(isActive = false, Nil, null)
    ))

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
