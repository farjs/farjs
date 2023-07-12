package farjs.ui.menu

import farjs.ui.border._
import farjs.ui.menu.SubMenu._
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class SubMenuSpec extends TestSpec with TestRendererUtils {

  SubMenu.doubleBorderComp = mockUiComponent("DoubleBorder")
  SubMenu.horizontalLineComp = "HorizontalLine".asInstanceOf[ReactClass]

  it should "call onClick with item index when onClick" in {
    //given
    val onClick = mockFunction[Int, Unit]
    val props = SubMenuProps(
      selected = 0,
      items = List("item 1", SubMenu.separator, "item 2"),
      top = 1,
      left = 2,
      onClick = onClick
    )
    val comp = testRender(withThemeContext(<(SubMenu())(^.wrapped := props)()))
    val textEl = inside(findComponents(comp, <.text.name)) {
      case List(_, text) => text
    }
    
    //then
    onClick.expects(2)
    
    //when
    textEl.props.onClick(null)
  }

  it should "render component" in {
    //given
    val props = SubMenuProps(
      selected = 0,
      items = List("item 1", SubMenu.separator, "item 2"),
      top = 1,
      left = 2,
      _ => ()
    )
    
    //when
    val result = testRender(withThemeContext(<(SubMenu())(^.wrapped := props)()))
    
    //then
    assertSubMenuPopup(result, props)
  }

  private def assertSubMenuPopup(result: TestInstance, props: SubMenuProps): Unit = {
    val textWidth = props.items.maxBy(_.length).length
    val width = 2 + textWidth
    val height = 2 + props.items.size
    val theme = DefaultTheme.popup.menu

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
        <(doubleBorderComp())(^.assertPlain[DoubleBorderProps](inside(_) {
          case DoubleBorderProps(resWidth, resHeight, style, resLeft, resTop, title, footer) =>
            resWidth shouldBe width
            resHeight shouldBe height
            style shouldBe theme
            resLeft shouldBe js.undefined
            resTop shouldBe js.undefined
            title shouldBe js.undefined
            footer shouldBe js.undefined
        }))(),

        props.items.zipWithIndex.map { case (text, index) =>
          if (text == separator) {
            <(horizontalLineComp)(^.assertPlain[HorizontalLineProps](inside(_) {
              case HorizontalLineProps(resLeft, resTop, length, lineCh, style, startCh, endCh) =>
                resLeft shouldBe 0
                resTop shouldBe (1 + index)
                length shouldBe width
                lineCh shouldBe SingleChars.horizontal
                style shouldBe theme
                startCh shouldBe DoubleChars.leftSingle
                endCh shouldBe DoubleChars.rightSingle
            }))()
          }
          else {
            <.text(
              ^.rbHeight := 1,
              ^.rbLeft := 1,
              ^.rbTop := 1 + index,
              ^.rbClickable := true,
              ^.rbMouse := true,
              ^.rbAutoFocus := false,
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
