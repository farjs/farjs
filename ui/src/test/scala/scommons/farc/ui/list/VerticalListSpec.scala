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
    findProps(root, VerticalItems).head.focusedPos shouldBe -1
    findProps(root, VerticalItems)(1).focusedPos shouldBe -1

    def check(x: Int, y: Int, focused1: Int, focused2: Int): Unit = {
      TestRenderer.act { () =>
        root.children(0).props.onClick(literal(x = x, y = y))
      }
      
      findProps(root, VerticalItems).head.focusedPos shouldBe focused1
      findProps(root, VerticalItems)(1).focusedPos shouldBe focused2
    }
    
    //when & then
    check(x = 6, y = 3, focused1 = 0, focused2 = -1)
    check(x = 6, y = 4, focused1 = 1, focused2 = -1)
    check(x = 8, y = 3, focused1 = -1, focused2 = 0)
    check(x = 8, y = 4, focused1 = -1, focused2 = 0)
  }

  it should "focus item when onKeypress" in {
    //given
    val props = VerticalListProps((7, 2), columns = 2, items = List(
      "item 1",
      "item 2",
      "item 3",
      "item 4",
      "item 5",
      "item 6",
      "item 7"
    ))
    val renderer = createRenderer()
    renderer.render(<(VerticalList())(^.wrapped := props)())
    findProps(renderer.getRenderOutput(), VerticalItems).head.focusedPos shouldBe -1
    
    def check(keyFull: String, f1: Int, f2: Int, i1: List[(String, Int)], i2: List[(String, Int)]): Unit = {
      renderer.getRenderOutput().props.onKeypress(null, literal(full = keyFull))
      
      val items = findProps(renderer.getRenderOutput(), VerticalItems)
      items.head.focusedPos shouldBe f1
      items.head.items shouldBe i1
      items(1).focusedPos shouldBe f2
      items(1).items shouldBe i2
    }
    
    //when & then
    check("unknown", f1 = -1, f2 = -1, i1 = List(("item 1", 0), ("item 2", 1)), i2 = List(("item 3", 2), ("item 4", 3))) //noop
    
    //when & then
    check("down", f1 = 0, f2 = -1, i1 = List(("item 1", 0), ("item 2", 1)), i2 = List(("item 3", 2), ("item 4", 3)))
    check("down", f1 = 1, f2 = -1, i1 = List(("item 1", 0), ("item 2", 1)), i2 = List(("item 3", 2), ("item 4", 3)))
    check("down", f1 = -1, f2 = 0, i1 = List(("item 1", 0), ("item 2", 1)), i2 = List(("item 3", 2), ("item 4", 3)))
    check("down", f1 = -1, f2 = 1, i1 = List(("item 1", 0), ("item 2", 1)), i2 = List(("item 3", 2), ("item 4", 3)))
    check("down", f1 = -1, f2 = 1, i1 = List(("item 2", 0), ("item 3", 1)), i2 = List(("item 4", 2), ("item 5", 3)))
    check("down", f1 = -1, f2 = 1, i1 = List(("item 3", 0), ("item 4", 1)), i2 = List(("item 5", 2), ("item 6", 3)))
    check("down", f1 = -1, f2 = 1, i1 = List(("item 4", 0), ("item 5", 1)), i2 = List(("item 6", 2), ("item 7", 3)))
    check("down", f1 = -1, f2 = 1, i1 = List(("item 4", 0), ("item 5", 1)), i2 = List(("item 6", 2), ("item 7", 3))) //noop

    //when & then
    check("up", f1 = -1, f2 = 0, i1 = List(("item 4", 0), ("item 5", 1)), i2 = List(("item 6", 2), ("item 7", 3)))
    check("up", f1 = 1, f2 = -1, i1 = List(("item 4", 0), ("item 5", 1)), i2 = List(("item 6", 2), ("item 7", 3)))
    check("up", f1 = 0, f2 = -1, i1 = List(("item 4", 0), ("item 5", 1)), i2 = List(("item 6", 2), ("item 7", 3)))
    check("up", f1 = 0, f2 = -1, i1 = List(("item 3", 0), ("item 4", 1)), i2 = List(("item 5", 2), ("item 6", 3)))
    check("up", f1 = 0, f2 = -1, i1 = List(("item 2", 0), ("item 3", 1)), i2 = List(("item 4", 2), ("item 5", 3)))
    check("up", f1 = 0, f2 = -1, i1 = List(("item 1", 0), ("item 2", 1)), i2 = List(("item 3", 2), ("item 4", 3)))
    check("up", f1 = 0, f2 = -1, i1 = List(("item 1", 0), ("item 2", 1)), i2 = List(("item 3", 2), ("item 4", 3))) //noop

    //when & then
    check("right", f1 = -1, f2 = 0, i1 = List(("item 1", 0), ("item 2", 1)), i2 = List(("item 3", 2), ("item 4", 3)))
    check("right", f1 = -1, f2 = 1, i1 = List(("item 3", 0), ("item 4", 1)), i2 = List(("item 5", 2), ("item 6", 3)))
    check("right", f1 = -1, f2 = 1, i1 = List(("item 4", 0), ("item 5", 1)), i2 = List(("item 6", 2), ("item 7", 3)))
    check("right", f1 = -1, f2 = 1, i1 = List(("item 4", 0), ("item 5", 1)), i2 = List(("item 6", 2), ("item 7", 3))) //noop

    //when & then
    check("left", f1 = 1, f2 = -1, i1 = List(("item 4", 0), ("item 5", 1)), i2 = List(("item 6", 2), ("item 7", 3)))
    check("left", f1 = 0, f2 = -1, i1 = List(("item 2", 0), ("item 3", 1)), i2 = List(("item 4", 2), ("item 5", 3)))
    check("left", f1 = 0, f2 = -1, i1 = List(("item 1", 0), ("item 2", 1)), i2 = List(("item 3", 2), ("item 4", 3)))
    check("left", f1 = 0, f2 = -1, i1 = List(("item 1", 0), ("item 2", 1)), i2 = List(("item 3", 2), ("item 4", 3))) //noop

    //when & then
    check("pagedown", f1 = -1, f2 = 1, i1 = List(("item 1", 0), ("item 2", 1)), i2 = List(("item 3", 2), ("item 4", 3)))
    check("pagedown", f1 = -1, f2 = 1, i1 = List(("item 4", 0), ("item 5", 1)), i2 = List(("item 6", 2), ("item 7", 3)))
    check("pagedown", f1 = -1, f2 = 1, i1 = List(("item 4", 0), ("item 5", 1)), i2 = List(("item 6", 2), ("item 7", 3))) //noop

    //when & then
    check("pageup", f1 = 0, f2 = -1, i1 = List(("item 4", 0), ("item 5", 1)), i2 = List(("item 6", 2), ("item 7", 3)))
    check("pageup", f1 = 0, f2 = -1, i1 = List(("item 1", 0), ("item 2", 1)), i2 = List(("item 3", 2), ("item 4", 3)))
    check("pageup", f1 = 0, f2 = -1, i1 = List(("item 1", 0), ("item 2", 1)), i2 = List(("item 3", 2), ("item 4", 3))) //noop

    //when & then
    check("end", f1 = -1, f2 = 1, i1 = List(("item 4", 0), ("item 5", 1)), i2 = List(("item 6", 2), ("item 7", 3)))
    check("end", f1 = -1, f2 = 1, i1 = List(("item 4", 0), ("item 5", 1)), i2 = List(("item 6", 2), ("item 7", 3))) //noop

    //when & then
    check("home", f1 = 0, f2 = -1, i1 = List(("item 1", 0), ("item 2", 1)), i2 = List(("item 3", 2), ("item 4", 3)))
    check("home", f1 = 0, f2 = -1, i1 = List(("item 1", 0), ("item 2", 1)), i2 = List(("item 3", 2), ("item 4", 3))) //noop
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
      assertNativeComponent(colWrap1, <.>(^.key := "0")(), { children: List[ShallowInstance] =>
        val List(sep, colItems) = children
        assertComponent(sep, VerticalLine) {
          case VerticalLineProps(pos, resLength, ch, style, start, end) =>
            pos shouldBe 2 -> -1
            resLength shouldBe 3
            ch shouldBe SingleBorder.verticalCh
            style shouldBe VerticalList.styles.normalItem
            start shouldBe Some(SingleBorder.topCh)
            end shouldBe Some(SingleBorder.bottomCh)
        }
        assertComponent(colItems, VerticalItems) {
          case VerticalItemsProps(resSize, left, boxStyle, itemStyle, items, focusedPos) =>
            resSize shouldBe 2 -> 1
            left shouldBe 0
            boxStyle shouldBe VerticalList.styles.normalItem
            itemStyle shouldBe VerticalList.styles.normalItem
            items shouldBe List(("item 1", 0))
            focusedPos shouldBe -1
        }
      })
      assertNativeComponent(colWrap2, <.>(^.key := "1")(), { children: List[ShallowInstance] =>
        val List(col2) = children
        assertComponent(col2, VerticalItems) {
          case VerticalItemsProps(resSize, left, boxStyle, itemStyle, items, focusedPos) =>
            resSize shouldBe 4 -> 1
            left shouldBe 3
            boxStyle shouldBe VerticalList.styles.normalItem
            itemStyle shouldBe VerticalList.styles.normalItem
            items shouldBe List(("item 2", 1))
            focusedPos shouldBe -1
        }
      })
    })
  }
}