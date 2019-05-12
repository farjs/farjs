package scommons.farc.ui.filelist

import scommons.farc.ui.border._
import scommons.react._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.{ShallowInstance, TestRenderer}
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class FileListSpec extends TestSpec
  with ShallowRendererUtils
  with TestRendererUtils {

  it should "focus item when onClick" in {
    //given
    val props = FileListProps((7, 2), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2",
      3 -> "item 3"
    ))
    val root = createTestRenderer(<(FileList())(^.wrapped := props)(), { el =>
      if (el.`type` == "button".asInstanceOf[js.Any]) {
        literal(aleft = 5, atop = 3)
      }
      else null
    }).root
    findProps(root, FileListColumn).head.focusedPos shouldBe -1
    findProps(root, FileListColumn)(1).focusedPos shouldBe -1

    def check(x: Int, y: Int, focused1: Int, focused2: Int): Unit = {
      TestRenderer.act { () =>
        root.children(0).props.onClick(literal(x = x, y = y))
      }
      
      findProps(root, FileListColumn).head.focusedPos shouldBe focused1
      findProps(root, FileListColumn)(1).focusedPos shouldBe focused2
    }
    
    //when & then
    check(x = 6, y = 3, focused1 = 0, focused2 = -1)
    check(x = 6, y = 4, focused1 = 1, focused2 = -1)
    check(x = 8, y = 3, focused1 = -1, focused2 = 0)
    check(x = 8, y = 4, focused1 = -1, focused2 = 0)
  }

  it should "focus item when onKeypress" in {
    //given
    val props = FileListProps((7, 2), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2",
      3 -> "item 3",
      4 -> "item 4",
      5 -> "item 5",
      6 -> "item 6",
      7 -> "item 7"
    ))
    val renderer = createRenderer()
    renderer.render(<(FileList())(^.wrapped := props)())
    findProps(renderer.getRenderOutput(), FileListColumn).head.focusedPos shouldBe -1
    
    def check(keyFull: String, f1: Int, f2: Int, i1: List[(Int, String)], i2: List[(Int, String)]): Unit = {
      renderer.getRenderOutput().props.onKeypress(null, literal(full = keyFull))
      
      val items = findProps(renderer.getRenderOutput(), FileListColumn)
      items.head.focusedPos shouldBe f1
      items.head.items shouldBe i1
      items(1).focusedPos shouldBe f2
      items(1).items shouldBe i2
    }
    
    //when & then
    check("unknown", f1 = -1, f2 = -1, i1 = List(1 -> "item 1", 2 -> "item 2"), i2 = List(3 -> "item 3", 4 -> "item 4")) //noop
    
    //when & then
    check("down", f1 = 0, f2 = -1, i1 = List(1 -> "item 1", 2 -> "item 2"), i2 = List(3 -> "item 3", 4 -> "item 4"))
    check("down", f1 = 1, f2 = -1, i1 = List(1 -> "item 1", 2 -> "item 2"), i2 = List(3 -> "item 3", 4 -> "item 4"))
    check("down", f1 = -1, f2 = 0, i1 = List(1 -> "item 1", 2 -> "item 2"), i2 = List(3 -> "item 3", 4 -> "item 4"))
    check("down", f1 = -1, f2 = 1, i1 = List(1 -> "item 1", 2 -> "item 2"), i2 = List(3 -> "item 3", 4 -> "item 4"))
    check("down", f1 = -1, f2 = 1, i1 = List(2 -> "item 2", 3 -> "item 3"), i2 = List(4 -> "item 4", 5 -> "item 5"))
    check("down", f1 = -1, f2 = 1, i1 = List(3 -> "item 3", 4 -> "item 4"), i2 = List(5 -> "item 5", 6 -> "item 6"))
    check("down", f1 = -1, f2 = 1, i1 = List(4 -> "item 4", 5 -> "item 5"), i2 = List(6 -> "item 6", 7 -> "item 7"))
    check("down", f1 = -1, f2 = 1, i1 = List(4 -> "item 4", 5 -> "item 5"), i2 = List(6 -> "item 6", 7 -> "item 7")) //noop

    //when & then
    check("up", f1 = -1, f2 = 0, i1 = List(4 -> "item 4", 5 -> "item 5"), i2 = List(6 -> "item 6", 7 -> "item 7"))
    check("up", f1 = 1, f2 = -1, i1 = List(4 -> "item 4", 5 -> "item 5"), i2 = List(6 -> "item 6", 7 -> "item 7"))
    check("up", f1 = 0, f2 = -1, i1 = List(4 -> "item 4", 5 -> "item 5"), i2 = List(6 -> "item 6", 7 -> "item 7"))
    check("up", f1 = 0, f2 = -1, i1 = List(3 -> "item 3", 4 -> "item 4"), i2 = List(5 -> "item 5", 6 -> "item 6"))
    check("up", f1 = 0, f2 = -1, i1 = List(2 -> "item 2", 3 -> "item 3"), i2 = List(4 -> "item 4", 5 -> "item 5"))
    check("up", f1 = 0, f2 = -1, i1 = List(1 -> "item 1", 2 -> "item 2"), i2 = List(3 -> "item 3", 4 -> "item 4"))
    check("up", f1 = 0, f2 = -1, i1 = List(1 -> "item 1", 2 -> "item 2"), i2 = List(3 -> "item 3", 4 -> "item 4")) //noop

    //when & then
    check("right", f1 = -1, f2 = 0, i1 = List(1 -> "item 1", 2 -> "item 2"), i2 = List(3 -> "item 3", 4 -> "item 4"))
    check("right", f1 = -1, f2 = 1, i1 = List(3 -> "item 3", 4 -> "item 4"), i2 = List(5 -> "item 5", 6 -> "item 6"))
    check("right", f1 = -1, f2 = 1, i1 = List(4 -> "item 4", 5 -> "item 5"), i2 = List(6 -> "item 6", 7 -> "item 7"))
    check("right", f1 = -1, f2 = 1, i1 = List(4 -> "item 4", 5 -> "item 5"), i2 = List(6 -> "item 6", 7 -> "item 7")) //noop

    //when & then
    check("left", f1 = 1, f2 = -1, i1 = List(4 -> "item 4", 5 -> "item 5"), i2 = List(6 -> "item 6", 7 -> "item 7"))
    check("left", f1 = 0, f2 = -1, i1 = List(2 -> "item 2", 3 -> "item 3"), i2 = List(4 -> "item 4", 5 -> "item 5"))
    check("left", f1 = 0, f2 = -1, i1 = List(1 -> "item 1", 2 -> "item 2"), i2 = List(3 -> "item 3", 4 -> "item 4"))
    check("left", f1 = 0, f2 = -1, i1 = List(1 -> "item 1", 2 -> "item 2"), i2 = List(3 -> "item 3", 4 -> "item 4")) //noop

    //when & then
    check("pagedown", f1 = -1, f2 = 1, i1 = List(1 -> "item 1", 2 -> "item 2"), i2 = List(3 -> "item 3", 4 -> "item 4"))
    check("pagedown", f1 = -1, f2 = 1, i1 = List(4 -> "item 4", 5 -> "item 5"), i2 = List(6 -> "item 6", 7 -> "item 7"))
    check("pagedown", f1 = -1, f2 = 1, i1 = List(4 -> "item 4", 5 -> "item 5"), i2 = List(6 -> "item 6", 7 -> "item 7")) //noop

    //when & then
    check("pageup", f1 = 0, f2 = -1, i1 = List(4 -> "item 4", 5 -> "item 5"), i2 = List(6 -> "item 6", 7 -> "item 7"))
    check("pageup", f1 = 0, f2 = -1, i1 = List(1 -> "item 1", 2 -> "item 2"), i2 = List(3 -> "item 3", 4 -> "item 4"))
    check("pageup", f1 = 0, f2 = -1, i1 = List(1 -> "item 1", 2 -> "item 2"), i2 = List(3 -> "item 3", 4 -> "item 4")) //noop

    //when & then
    check("end", f1 = -1, f2 = 1, i1 = List(4 -> "item 4", 5 -> "item 5"), i2 = List(6 -> "item 6", 7 -> "item 7"))
    check("end", f1 = -1, f2 = 1, i1 = List(4 -> "item 4", 5 -> "item 5"), i2 = List(6 -> "item 6", 7 -> "item 7")) //noop

    //when & then
    check("home", f1 = 0, f2 = -1, i1 = List(1 -> "item 1", 2 -> "item 2"), i2 = List(3 -> "item 3", 4 -> "item 4"))
    check("home", f1 = 0, f2 = -1, i1 = List(1 -> "item 1", 2 -> "item 2"), i2 = List(3 -> "item 3", 4 -> "item 4")) //noop
  }

  it should "render empty component when height = 0" in {
    //given
    val props = FileListProps((1, 0), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2",
      3 -> "item 3"
    ))

    //when
    val result = shallowRender(<(FileList())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.button(^.rbMouse := true)())
  }
  
  it should "render empty component when columns = 0" in {
    //given
    val props = FileListProps((1, 2), columns = 0, items = List(
      1 -> "item 1",
      2 -> "item 2",
      3 -> "item 3"
    ))

    //when
    val result = shallowRender(<(FileList())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.button(^.rbMouse := true)())
  }
  
  it should "render component with 2 columns" in {
    //given
    val props = FileListProps((7, 1), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2",
      3 -> "item 3"
    ))
    val comp = <(FileList())(^.wrapped := props)()

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
            style shouldBe FileList.styles.normalItem
            start shouldBe Some(SingleBorder.topCh)
            end shouldBe Some(SingleBorder.bottomCh)
        }
        assertComponent(colItems, FileListColumn) {
          case FileListColumnProps(resSize, left, boxStyle, itemStyle, items, focusedPos) =>
            resSize shouldBe 2 -> 1
            left shouldBe 0
            boxStyle shouldBe FileList.styles.normalItem
            itemStyle shouldBe FileList.styles.normalItem
            items shouldBe List(1 -> "item 1")
            focusedPos shouldBe -1
        }
      })
      assertNativeComponent(colWrap2, <.>(^.key := "1")(), { children: List[ShallowInstance] =>
        val List(col2) = children
        assertComponent(col2, FileListColumn) {
          case FileListColumnProps(resSize, left, boxStyle, itemStyle, items, focusedPos) =>
            resSize shouldBe 4 -> 1
            left shouldBe 3
            boxStyle shouldBe FileList.styles.normalItem
            itemStyle shouldBe FileList.styles.normalItem
            items shouldBe List(2 -> "item 2")
            focusedPos shouldBe -1
        }
      })
    })
  }
}
