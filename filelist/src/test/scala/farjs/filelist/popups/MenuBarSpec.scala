package farjs.filelist.popups

import farjs.filelist.popups.MenuBar._
import farjs.ui.ButtonsPanelProps
import farjs.ui.popup.PopupProps
import farjs.ui.theme.Theme
import scommons.nodejs._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class MenuBarSpec extends TestSpec with TestRendererUtils {

  MenuBar.popupComp = mockUiComponent("Popup")
  MenuBar.buttonsPanel = mockUiComponent("ButtonsPanel")

  it should "call onClose when onKeypress(F10)" in {
    //given
    val onClose = mockFunction[Unit]
    val props = MenuBarProps(onClose)
    val comp = testRender(<(MenuBar())(^.wrapped := props)())
    val popupProps = findComponentProps(comp, popupComp)

    //then
    onClose.expects()

    //when & then
    popupProps.onKeypress("f10") shouldBe true
  }

  it should "emit keypress event when onKeypress(down)" in {
    //given
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

    val props = MenuBarProps(() => ())
    val comp = testRender(<(MenuBar())(^.wrapped := props)())
    val popupProps = findComponentProps(comp, popupComp)

    //then
    onKey.expects("enter", false, false, false)

    //when
    popupProps.onKeypress("down") shouldBe true

    //cleanup
    process.stdin.removeListener("keypress", listener)
  }

  it should "return true when onKeypress(up)" in {
    //given
    val props = MenuBarProps(() => ())
    val comp = testRender(<(MenuBar())(^.wrapped := props)())
    val popupProps = findComponentProps(comp, popupComp)

    //when & then
    popupProps.onKeypress("up") shouldBe true
  }
  
  it should "return false when onKeypress(other)" in {
    //given
    val props = MenuBarProps(() => ())
    val comp = testRender(<(MenuBar())(^.wrapped := props)())
    val popupProps = findComponentProps(comp, popupComp)

    //when & then
    popupProps.onKeypress("other") shouldBe false
  }
  
  it should "render component" in {
    //given
    val props = MenuBarProps(() => ())

    //when
    val result = testRender(<(MenuBar())(^.wrapped := props)())

    //then
    assertMenuBar(result, props)
  }
  
  private def assertMenuBar(result: TestInstance, props: MenuBarProps): Unit = {
    val theme = Theme.current.popup.menu
    
    assertNativeComponent(result,
      <(popupComp())(^.assertWrapped(inside(_) {
        case PopupProps(props.onClose, closable, focusable, _, _) =>
          closable shouldBe true
          focusable shouldBe true
      }))(
        <.box(
          ^.rbHeight := 1,
          ^.rbStyle := theme
        )(
          <.box(
            ^.rbWidth := 49,
            ^.rbHeight := 1,
            ^.rbLeft := 2
          )(
            <(buttonsPanel())(^.assertWrapped(inside(_) {
              case ButtonsPanelProps(top, actions, `theme`, padding, margin) =>
                top shouldBe 0
                actions.map(_._1) shouldBe List(
                "Left",
                "Files",
                "Commands",
                "Options",
                "Right"
              )
              padding shouldBe 2
              margin shouldBe 0
            }))()
          )
        )
      )
    )
  }
}
