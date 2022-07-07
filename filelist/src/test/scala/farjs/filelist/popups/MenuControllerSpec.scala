package farjs.filelist.popups

import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.popups.MenuController._
import farjs.filelist.stack.PanelStack
import farjs.filelist.stack.WithPanelStacksSpec.withContext
import farjs.ui.popup.PopupOverlay
import scommons.nodejs._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class MenuControllerSpec extends TestSpec with TestRendererUtils {

  MenuController.menuBarComp = mockUiComponent("MenuBar")

  it should "emit keypress event globally when onAction" in {
    //given
    val onKey = mockFunction[String, String, Boolean, Boolean, Boolean, Unit]
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKey(
        key.name,
        key.full,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)

    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showMenuPopup = true))
    val comp = testRender(withContext(
      <(MenuController())(^.wrapped := props)(),
      leftStack = new PanelStack(isActive = true, Nil, null),
      rightStack = new PanelStack(isActive = false, Nil, null)
    ))
    val popup = findComponentProps(comp, menuBarComp)

    //then
    dispatch.expects(FileListPopupMenuAction(show = false))
    onKey.expects("f3", "f3", false, false, false)

    //when
    popup.onAction(1, 0)

    //cleanup
    process.stdin.removeListener("keypress", listener)
  }

  it should "emit keypress event for left panel when onAction" in {
    //given
    val onKey = mockFunction[String, String, Boolean, Boolean, Boolean, Unit]
    val emitter: js.Function3[String, js.Any, KeyboardKey, Unit] = { (_, _, key) =>
      onKey(
        key.name,
        key.full,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    val leftInput = js.Dynamic.literal(
      "emit" -> emitter
    ).asInstanceOf[BlessedElement]

    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showMenuPopup = true))
    val comp = testRender(withContext(
      <(MenuController())(^.wrapped := props)(),
      leftStack = new PanelStack(isActive = true, Nil, null),
      rightStack = new PanelStack(isActive = false, Nil, null),
      leftInput = leftInput
    ))
    val popup = findComponentProps(comp, menuBarComp)

    //then
    dispatch.expects(FileListPopupMenuAction(show = false))
    onKey.expects("q", "C-q", true, false, false)

    //when
    popup.onAction(0, 0)
  }

  it should "emit keypress event for right panel when onAction" in {
    //given
    val onKey = mockFunction[String, String, Boolean, Boolean, Boolean, Unit]
    val emitter: js.Function3[String, js.Any, KeyboardKey, Unit] = { (_, _, key) =>
      onKey(
        key.name,
        key.full,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    val rightInput = js.Dynamic.literal(
      "emit" -> emitter
    ).asInstanceOf[BlessedElement]

    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showMenuPopup = true))
    val comp = testRender(withContext(
      <(MenuController())(^.wrapped := props)(),
      leftStack = new PanelStack(isActive = true, Nil, null),
      rightStack = new PanelStack(isActive = false, Nil, null),
      rightInput = rightInput
    ))
    val popup = findComponentProps(comp, menuBarComp)

    //then
    dispatch.expects(FileListPopupMenuAction(show = false))
    onKey.expects("q", "C-q", true, false, false)

    //when
    popup.onAction(4, 0)
  }

  it should "dispatch FileListPopupMenuAction(show=false) when onClose" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showMenuPopup = true))
    val comp = testRender(withContext(
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
    val comp = testRender(withContext(
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
    val result = testRender(withContext(
      <(MenuController())(^.wrapped := props)(),
      leftStack = new PanelStack(isActive = true, Nil, null),
      rightStack = new PanelStack(isActive = false, Nil, null),
      leftInput = leftInput,
      rightInput = rightInput
    ))

    //then
    assertTestComponent(result, menuBarComp) {
      case MenuBarProps(resItems, _, _) =>
        resItems shouldBe items
    }
  }

  it should "render clickable box component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState())

    //when
    val result = testRender(withContext(
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
