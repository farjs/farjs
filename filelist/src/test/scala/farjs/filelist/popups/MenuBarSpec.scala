package farjs.filelist.popups

import farjs.filelist.popups.MenuBar._
import farjs.ui.ButtonsPanelProps
import farjs.ui.popup.PopupProps
import farjs.ui.theme.Theme
import scommons.react.blessed._
import scommons.react.test._

class MenuBarSpec extends TestSpec with TestRendererUtils {

  MenuBar.popupComp = mockUiComponent("Popup")
  MenuBar.buttonsPanel = mockUiComponent("ButtonsPanel")

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
        case PopupProps(props.onClose, closable, focusable, _) =>
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
