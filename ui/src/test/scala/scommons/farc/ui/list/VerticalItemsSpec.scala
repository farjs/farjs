package scommons.farc.ui.list

import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.util.ShallowRendererUtils

class VerticalItemsSpec extends TestSpec with ShallowRendererUtils {

  it should "render component" in {
    //given
    val props = VerticalItemsProps(
      size = (5, 2),
      left = 3,
      boxStyle = new BlessedStyle {},
      itemStyle = new BlessedStyle {},
      items = List(
        ("item 1", 10),
        ("item 2", 11),
        ("item 3", 12)
      ),
      focusedIndex = 11
    )
    val comp = <(VerticalItems())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertNativeComponent(result, <.box(
      ^.rbWidth := props.size._1,
      ^.rbHeight := props.size._2,
      ^.rbLeft := props.left,
      ^.rbStyle := props.boxStyle
    )(), { case List(comp1, comp2, comp3) =>
      assertComponent(comp1, ListItem) { case ListItemProps(width, top, style, text, focused) =>
        width shouldBe props.size._1
        top shouldBe 0
        style shouldBe props.itemStyle
        text shouldBe "item 1"
        focused shouldBe false
      }
      assertComponent(comp2, ListItem) { case ListItemProps(width, top, style, text, focused) =>
        width shouldBe props.size._1
        top shouldBe 1
        style shouldBe props.itemStyle
        text shouldBe "item 2"
        focused shouldBe true
      }
      assertComponent(comp3, ListItem) { case ListItemProps(width, top, style, text, focused) =>
        width shouldBe props.size._1
        top shouldBe 2
        style shouldBe props.itemStyle
        text shouldBe "item 3"
        focused shouldBe false
      }
    })
  }
}
