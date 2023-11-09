package farjs.filelist.popups

import farjs.filelist.FileListUiData
import farjs.filelist.popups.MenuController._
import farjs.filelist.stack.PanelStack
import farjs.filelist.stack.WithPanelStacksSpec.withContext
import farjs.ui.menu.MenuBarProps
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

    val onClose = mockFunction[Unit]
    val props = FileListUiData(showMenuPopup = true, onClose = onClose)
    val comp = testRender(withContext(
      <(MenuController())(^.wrapped := props)(),
      leftStack = new PanelStack(isActive = true, Nil, null),
      rightStack = new PanelStack(isActive = false, Nil, null)
    ))
    val popup = findComponentProps(comp, menuBarComp, plain = true)

    //then
    onClose.expects()
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

    val onClose = mockFunction[Unit]
    val props = FileListUiData(showMenuPopup = true, onClose = onClose)
    val comp = testRender(withContext(
      <(MenuController())(^.wrapped := props)(),
      leftStack = new PanelStack(isActive = true, Nil, null),
      rightStack = new PanelStack(isActive = false, Nil, null),
      leftInput = leftInput
    ))
    val popup = findComponentProps(comp, menuBarComp, plain = true)

    //then
    onClose.expects()
    onKey.expects("l", "M-l", false, true, false)

    //when
    popup.onAction(0, 4)
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

    val onClose = mockFunction[Unit]
    val props = FileListUiData(showMenuPopup = true, onClose = onClose)
    val comp = testRender(withContext(
      <(MenuController())(^.wrapped := props)(),
      leftStack = new PanelStack(isActive = true, Nil, null),
      rightStack = new PanelStack(isActive = false, Nil, null),
      rightInput = rightInput
    ))
    val popup = findComponentProps(comp, menuBarComp, plain = true)

    //then
    onClose.expects()
    onKey.expects("r", "M-r", false, true, false)

    //when
    popup.onAction(4, 4)
  }

  it should "call onClose when onClose" in {
    //given
    val onClose = mockFunction[Unit]
    val props = FileListUiData(showMenuPopup = true, onClose = onClose)
    val comp = testRender(withContext(
      <(MenuController())(^.wrapped := props)(),
      leftStack = new PanelStack(isActive = true, Nil, null),
      rightStack = new PanelStack(isActive = false, Nil, null)
    ))
    val popup = findComponentProps(comp, menuBarComp, plain = true)

    //then
    onClose.expects()

    //when
    popup.onClose()
  }

  it should "render MenuBar component" in {
    //given
    val props = FileListUiData(showMenuPopup = true)
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
    assertTestComponent(result, menuBarComp, plain = true) {
      case MenuBarProps(resItems, _, _) =>
        resItems.toList shouldBe items.toList
    }
  }
}
