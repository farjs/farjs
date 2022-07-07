package farjs.ui.menu

import farjs.ui.border._
import farjs.ui.menu.SubMenu._
import farjs.ui.theme.Theme
import scommons.react.blessed._
import scommons.react.test._

class SubMenuSpec extends TestSpec with TestRendererUtils {

  SubMenu.doubleBorderComp = mockUiComponent("DoubleBorder")
  SubMenu.horizontalLineComp = mockUiComponent("HorizontalLine")
  SubMenu.buttonComp = mockUiComponent("Button")

  it should "render component" in {
    //given
    val props = SubMenuProps(
      selected = 0,
      items = List("item 1", SubMenu.separator, "item 2"),
      top = 1,
      left = 2
    )
    
    //when
    val result = testRender(<(SubMenu())(^.wrapped := props)())
    
    //then
    assertSubMenuPopup(result, props)
  }

  private def assertSubMenuPopup(result: TestInstance, props: SubMenuProps): Unit = {
    val textWidth = props.items.maxBy(_.length).length
    val width = 2 + textWidth
    val height = 2 + props.items.size
    val theme = Theme.current.popup.menu

    assertNativeComponent(result,
      <.box(
        ^.rbClickable := true,
        ^.rbAutoFocus := false,
        ^.rbWidth := width,
        ^.rbHeight := height,
        ^.rbTop := props.top,
        ^.rbLeft := props.left,
        ^.rbShadow := true,
        ^.rbStyle := theme
      )(
        <(doubleBorderComp())(^.assertWrapped(inside(_) {
          case DoubleBorderProps(size, style, pos, title) =>
            size shouldBe (width -> height)
            style shouldBe theme
            pos shouldBe (0 -> 0)
            title shouldBe None
        }))(),

        props.items.zipWithIndex.map { case (text, index) =>
          if (text == separator) {
            <(horizontalLineComp())(^.assertWrapped(inside(_) {
              case HorizontalLineProps(pos, length, lineCh, style, startCh, endCh) =>
                pos shouldBe (0 -> (1 + index))
                length shouldBe width
                lineCh shouldBe SingleBorder.horizontalCh
                style shouldBe theme
                startCh shouldBe Some(DoubleBorder.leftSingleCh)
                endCh shouldBe Some(DoubleBorder.rightSingleCh)
            }))()
          }
          else {
            <.text(
              ^.rbHeight := 1,
              ^.rbLeft := 1,
              ^.rbTop := 1 + index,
              ^.rbStyle := {
                if (props.selected == index) theme.focus.getOrElse(null)
                else theme
              },
              ^.content := text
            )()
          }
        }
      )
    )
  }
}
