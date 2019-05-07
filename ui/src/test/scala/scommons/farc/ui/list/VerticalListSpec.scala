package scommons.farc.ui.list

import scommons.farc.ui.border._
import scommons.react._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.{ShallowInstance, TestRenderer}
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class VerticalListSpec extends TestSpec
  with ShallowRendererUtils
  with TestRendererUtils {

  it should "focus item when onClick" in {
    //given
    val props = VerticalListProps((7, 2), columns = 2, items = List("item 1", "item 2", "item 3"))
    val root = createTestRenderer(<(VerticalList())(^.wrapped := props)(), { el =>
      if (el.`type` == "button".asInstanceOf[js.Any]) {
        literal(aleft = 5, atop = 3)
      }
      else null
    }).root
    findProps(root, VerticalItems).head.focusedIndex shouldBe -1

    def check(x: Int, y: Int, focusedIndex: Int): Unit = {
      TestRenderer.act { () =>
        root.children(0).props.onClick(literal(x = x, y = y))
      }
      
      findProps(root, VerticalItems).head.focusedIndex shouldBe focusedIndex
    }
    
    //when & then
    check(x = 6, y = 3, focusedIndex = 0)
    check(x = 6, y = 4, focusedIndex = 1)
    check(x = 8, y = 3, focusedIndex = 2)
    check(x = 8, y = 4, focusedIndex = 2)
  }

  it should "focus item when onKeypress" in {
    //given
    val props = VerticalListProps((7, 1), columns = 2, items = List("item 1", "item 2", "item 3"))
    val renderer = createRenderer()
    renderer.render(<(VerticalList())(^.wrapped := props)())
    findProps(renderer.getRenderOutput(), VerticalItems).head.focusedIndex shouldBe -1
    
    def check(keyFull: String, focusedIndex: Int, items1: List[(String, Int)], items2: List[(String, Int)]): Unit = {
      renderer.getRenderOutput().props.onKeypress(null, literal(full = keyFull))
      
      val items = findProps(renderer.getRenderOutput(), VerticalItems)
      items.head.focusedIndex shouldBe focusedIndex
      items.head.items shouldBe items1
      items(1).focusedIndex shouldBe focusedIndex
      items(1).items shouldBe items2
    }
    
    //when & then
    check("down", focusedIndex = 0, items1 = List(("item 1", 0)), items2 = List(("item 2", 1)))
    check("down", focusedIndex = 1, items1 = List(("item 1", 0)), items2 = List(("item 2", 1)))
    check("down", focusedIndex = 1, items1 = List(("item 2", 0)), items2 = List(("item 3", 1)))
    check("down", focusedIndex = 1, items1 = List(("item 2", 0)), items2 = List(("item 3", 1))) //noop
    check("up", focusedIndex = 0, items1 = List(("item 2", 0)), items2 = List(("item 3", 1)))
    check("up", focusedIndex = 0, items1 = List(("item 1", 0)), items2 = List(("item 2", 1)))
    check("up", focusedIndex = 0, items1 = List(("item 1", 0)), items2 = List(("item 2", 1))) //noop
    check("unknown", focusedIndex = 0, items1 = List(("item 1", 0)), items2 = List(("item 2", 1))) //noop
  }

  it should "render empty component when height = 0" in {
    //given
    val props = VerticalListProps((1, 0), columns = 2, items = List("item 1", "item 2", "item 3"))

    //when
    val result = shallowRender(<(VerticalList())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.button(^.rbMouse := true)())
  }
  
  it should "render empty component when columns = 0" in {
    //given
    val props = VerticalListProps((1, 2), columns = 0, items = List("item 1", "item 2", "item 3"))

    //when
    val result = shallowRender(<(VerticalList())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.button(^.rbMouse := true)())
  }
  
  it should "render component with 2 columns" in {
    //given
    val props = VerticalListProps((7, 1), columns = 2, items = List("item 1", "item 2", "item 3"))
    val comp = <(VerticalList())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertNativeComponent(result, <.button(^.rbMouse := true)(), { children: List[ShallowInstance] =>
      val List(colWrap1, colWrap2) = children
      assertNativeComponent(colWrap1, <.>()(), { children: List[ShallowInstance] =>
        val List(sep, colItems) = children
        sep.key shouldBe "sep0"
        assertComponent(sep, VerticalLine) {
          case VerticalLineProps(pos, resLength, ch, style, start, end) =>
            pos shouldBe 2 -> -1
            resLength shouldBe 3
            ch shouldBe "\u2502"
            style shouldBe VerticalList.styles.normalItem
            start shouldBe Some("\u2564")
            end shouldBe Some("\u2567")
        }
        colItems.key shouldBe "col0"
        assertComponent(colItems, VerticalItems) {
          case VerticalItemsProps(resSize, left, boxStyle, itemStyle, items, focusedIndex) =>
            resSize shouldBe 2 -> 1
            left shouldBe 0
            boxStyle shouldBe VerticalList.styles.normalItem
            itemStyle shouldBe VerticalList.styles.normalItem
            items shouldBe List(("item 1", 0))
            focusedIndex shouldBe -1
        }
      })
      assertNativeComponent(colWrap2, <.>()(), { children: List[ShallowInstance] =>
        val List(col2) = children
        col2.key shouldBe "col1"
        assertComponent(col2, VerticalItems) {
          case VerticalItemsProps(resSize, left, boxStyle, itemStyle, items, focusedIndex) =>
            resSize shouldBe 4 -> 1
            left shouldBe 3
            boxStyle shouldBe VerticalList.styles.normalItem
            itemStyle shouldBe VerticalList.styles.normalItem
            items shouldBe List(("item 2", 1))
            focusedIndex shouldBe -1
        }
      })
    })
  }
}
