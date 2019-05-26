package scommons.farc.ui.filelist

import org.scalactic.source.Position
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

  it should "focus item when onWheelup/onWheeldown" in {
    //given
    val props = FileListProps((7, 7), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2",
      3 -> "item 3",
      4 -> "item 4",
      5 -> "item 5",
      6 -> "item 6",
      7 -> "item 7",
      8 -> "item 8",
      9 -> "item 9"
    ))
    val root = createTestRenderer(<(FileList())(^.wrapped := props)(), { el =>
      if (el.`type` == "button".asInstanceOf[js.Any]) literal(aleft = 5, atop = 3)
      else null
    }).root
    findProps(root, FileListColumn).head.focusedPos shouldBe -1
    findProps(root, FileListColumn)(1).focusedPos shouldBe -1

    def check(up: Boolean,
              focused1: Int,
              focused2: Int,
              shift: Boolean = false
             )(implicit pos: Position): Unit = {
      
      TestRenderer.act { () =>
        if (up) root.children(0).props.onWheelup(literal("shift" -> shift))
        else root.children(0).props.onWheeldown(literal("shift" -> shift))
      }
      
      findProps(root, FileListColumn).head.focusedPos shouldBe focused1
      findProps(root, FileListColumn)(1).focusedPos shouldBe focused2
    }
    
    //when & then
    check(up = false, focused1 = 4, focused2 = -1)
    check(up = false, focused1 = -1, focused2 = 2)
    check(up = false, focused1 = -1, focused2 = 2) //noop

    //when & then
    check(up = true, focused1 = 3, focused2 = -1)
    check(up = true, focused1 = 0, focused2 = -1)
    check(up = true, focused1 = 0, focused2 = -1) //noop

    //when & then
    check(up = false, focused1 = 0, focused2 = -1, shift = true) //noop
    check(up = true, focused1 = 0, focused2 = -1, shift = true) //noop
  }

  it should "focus item when onClick" in {
    //given
    val props = FileListProps((7, 3), columns = 2, items = List(
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

    def check(x: Int, y: Int, focused1: Int, focused2: Int)(implicit pos: Position): Unit = {
      TestRenderer.act { () =>
        root.children(0).props.onClick(literal(x = x, y = y))
      }
      
      findProps(root, FileListColumn).head.focusedPos shouldBe focused1
      findProps(root, FileListColumn)(1).focusedPos shouldBe focused2
    }
    
    //when & then
    check(x = 6, y = 3, focused1 = 0, focused2 = -1) // header in col 1
    check(x = 6, y = 4, focused1 = 0, focused2 = -1) // first item in col 1
    check(x = 6, y = 5, focused1 = 1, focused2 = -1) // second item in col 1

    //when & then
    check(x = 8, y = 3, focused1 = -1, focused2 = 0) // header in col 2
    check(x = 8, y = 4, focused1 = -1, focused2 = 0) // first item in col 2
    check(x = 8, y = 5, focused1 = -1, focused2 = 0) // last item in col 2
  }

  it should "focus and select item when onKeypress" in {
    //given
    val props = FileListProps((7, 3), columns = 2, items = List(
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
    
    def check(keyFull: String,
              expectedData: List[(List[(Int, String)], Int, Set[Int])]
             )(implicit pos: Position): Unit = {
      
      renderer.getRenderOutput().props.onKeypress(null, literal(full = keyFull))
      
      val List(col1, col2) = findProps(renderer.getRenderOutput(), FileListColumn)
      (col1.items, col1.focusedPos, col1.selectedIds) shouldBe expectedData.head
      (col2.items, col2.focusedPos, col2.selectedIds) shouldBe expectedData(1)
    }
    
    //when & then
    check("unknown", List(
      (List(1 -> "item 1", 2 -> "item 2"), -1, Set.empty),
      (List(3 -> "item 3", 4 -> "item 4"), -1, Set.empty)
    )) //noop
    
    //when & then
    check("down", List(
      (List(1 -> "item 1", 2 -> "item 2"), 0, Set.empty),
      (List(3 -> "item 3", 4 -> "item 4"), -1, Set.empty)
    ))
    check("S-down", List(
      (List(1 -> "item 1", 2 -> "item 2"), 1, Set(1)),
      (List(3 -> "item 3", 4 -> "item 4"), -1, Set.empty)
    ))
    check("S-down", List(
      (List(1 -> "item 1", 2 -> "item 2"), -1, Set(1, 2)),
      (List(3 -> "item 3", 4 -> "item 4"), 0, Set.empty)
    ))
    check("down", List(
      (List(1 -> "item 1", 2 -> "item 2"), -1, Set(1, 2)),
      (List(3 -> "item 3", 4 -> "item 4"), 1, Set.empty)
    ))
    check("down", List(
      (List(2 -> "item 2", 3 -> "item 3"), -1, Set(2)),
      (List(4 -> "item 4", 5 -> "item 5"), 1, Set.empty)
    ))
    check("S-down", List(
      (List(3 -> "item 3", 4 -> "item 4"), -1, Set.empty),
      (List(5 -> "item 5", 6 -> "item 6"), 1, Set(5))
    ))
    check("S-down", List(
      (List(4 -> "item 4", 5 -> "item 5"), -1, Set(5)),
      (List(6 -> "item 6", 7 -> "item 7"), 1, Set(6, 7))
    ))
    check("down", List(
      (List(4 -> "item 4", 5 -> "item 5"), -1, Set(5)),
      (List(6 -> "item 6", 7 -> "item 7"), 1, Set(6, 7))
    )) //noop

    //when & then
    check("S-up", List(
      (List(4 -> "item 4", 5 -> "item 5"), -1, Set(5)),
      (List(6 -> "item 6", 7 -> "item 7"), 0, Set(6))
    ))
    check("S-up", List(
      (List(4 -> "item 4", 5 -> "item 5"), 1, Set(5)),
      (List(6 -> "item 6", 7 -> "item 7"), -1, Set.empty)
    ))
    check("S-up", List(
      (List(4 -> "item 4", 5 -> "item 5"), 0, Set.empty),
      (List(6 -> "item 6", 7 -> "item 7"), -1, Set.empty)
    ))
    check("up", List(
      (List(3 -> "item 3", 4 -> "item 4"), 0, Set.empty),
      (List(5 -> "item 5", 6 -> "item 6"), -1, Set.empty)
    ))
    check("up", List(
      (List(2 -> "item 2", 3 -> "item 3"), 0, Set(2)),
      (List(4 -> "item 4", 5 -> "item 5"), -1, Set.empty)
    ))
    check("S-up", List(
      (List(1 -> "item 1", 2 -> "item 2"), 0, Set.empty),
      (List(3 -> "item 3", 4 -> "item 4"), -1, Set.empty)
    ))
    check("up", List(
      (List(1 -> "item 1", 2 -> "item 2"), 0, Set.empty),
      (List(3 -> "item 3", 4 -> "item 4"), -1, Set.empty)
    )) //noop

    //when & then
    check("S-right", List(
      (List(1 -> "item 1", 2 -> "item 2"), -1, Set(1, 2)),
      (List(3 -> "item 3", 4 -> "item 4"), 0, Set.empty)
    ))
    check("right", List(
      (List(3 -> "item 3", 4 -> "item 4"), -1, Set.empty),
      (List(5 -> "item 5", 6 -> "item 6"), 0, Set.empty)
    ))
    check("S-right", List(
      (List(4 -> "item 4", 5 -> "item 5"), -1, Set(5)),
      (List(6 -> "item 6", 7 -> "item 7"), 1, Set(6, 7))
    ))
    check("right", List(
      (List(4 -> "item 4", 5 -> "item 5"), -1, Set(5)),
      (List(6 -> "item 6", 7 -> "item 7"), 1, Set(6, 7))
    )) //noop

    //when & then
    check("S-left", List(
      (List(4 -> "item 4", 5 -> "item 5"), 1, Set(5)),
      (List(6 -> "item 6", 7 -> "item 7"), -1, Set.empty)
    ))
    check("left", List(
      (List(2 -> "item 2", 3 -> "item 3"), 1, Set(2)),
      (List(4 -> "item 4", 5 -> "item 5"), -1, Set(5))
    ))
    check("S-left", List(
      (List(1 -> "item 1", 2 -> "item 2"), 0, Set(1, 2)),
      (List(3 -> "item 3", 4 -> "item 4"), -1, Set(3))
    ))
    check("left", List(
      (List(1 -> "item 1", 2 -> "item 2"), 0, Set(1, 2)),
      (List(3 -> "item 3", 4 -> "item 4"), -1, Set(3))
    )) //noop

    //when & then
    check("S-pagedown", List(
      (List(1 -> "item 1", 2 -> "item 2"), -1, Set.empty),
      (List(3 -> "item 3", 4 -> "item 4"), 1, Set.empty)
    ))
    check("S-pagedown", List(
      (List(4 -> "item 4", 5 -> "item 5"), -1, Set(4, 5)),
      (List(6 -> "item 6", 7 -> "item 7"), 1, Set(6, 7))
    ))
    check("pagedown", List(
      (List(4 -> "item 4", 5 -> "item 5"), -1, Set(4, 5)),
      (List(6 -> "item 6", 7 -> "item 7"), 1, Set(6, 7))
    )) //noop

    //when & then
    check("S-pageup", List(
      (List(4 -> "item 4", 5 -> "item 5"), 0, Set(4)),
      (List(6 -> "item 6", 7 -> "item 7"), -1, Set.empty)
    ))
    check("S-pageup", List(
      (List(1 -> "item 1", 2 -> "item 2"), 0, Set.empty),
      (List(3 -> "item 3", 4 -> "item 4"), -1, Set.empty)
    ))
    check("pageup", List(
      (List(1 -> "item 1", 2 -> "item 2"), 0, Set.empty),
      (List(3 -> "item 3", 4 -> "item 4"), -1, Set.empty)
    )) //noop

    //when & then
    check("S-end", List(
      (List(4 -> "item 4", 5 -> "item 5"), -1, Set(4, 5)),
      (List(6 -> "item 6", 7 -> "item 7"), 1, Set(6, 7))
    ))
    check("end", List(
      (List(4 -> "item 4", 5 -> "item 5"), -1, Set(4, 5)),
      (List(6 -> "item 6", 7 -> "item 7"), 1, Set(6, 7))
    )) //noop

    //when & then
    check("S-home", List(
      (List(1 -> "item 1", 2 -> "item 2"), 0, Set.empty),
      (List(3 -> "item 3", 4 -> "item 4"), -1, Set.empty)
    ))
    check("home", List(
      (List(1 -> "item 1", 2 -> "item 2"), 0, Set.empty),
      (List(3 -> "item 3", 4 -> "item 4"), -1, Set.empty)
    )) //noop
  }

  it should "render empty component when height < 2" in {
    //given
    val props = FileListProps((1, 1), columns = 2, items = List(
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
  
  it should "render empty component with 2 columns" in {
    //given
    val props = FileListProps((7, 2), columns = 2, items = Nil)
    val comp = <(FileList())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileList(result, props, List(
      Nil,
      Nil
    ))
  }
  
  it should "render non-empty component with 2 columns" in {
    //given
    val props = FileListProps((7, 2), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2",
      3 -> "item 3"
    ))
    val comp = <(FileList())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileList(result, props, List(
      List(1 -> "item 1"),
      List(2 -> "item 2")
    ))
  }
  
  private def assertFileList(result: ShallowInstance,
                             props: FileListProps,
                             colItems: List[List[(Int, String)]]): Unit = {
    
    assertNativeComponent(result, <.button(
      ^.rbWidth := props.size._1,
      ^.rbHeight := props.size._2,
      ^.rbLeft := 1,
      ^.rbTop := 1,
      ^.rbMouse := true
    )(), { children: List[ShallowInstance] =>
      val List(colWrap1, colWrap2) = children
      assertNativeComponent(colWrap1, <.>(^.key := "0")(), { children: List[ShallowInstance] =>
        val List(sep, col1) = children
        assertComponent(sep, VerticalLine) {
          case VerticalLineProps(pos, resLength, ch, style, start, end) =>
            pos shouldBe 2 -> -1
            resLength shouldBe 4
            ch shouldBe SingleBorder.verticalCh
            style shouldBe FileList.styles.normalItem
            start shouldBe Some(DoubleBorder.topSingleCh)
            end shouldBe Some(SingleBorder.bottomCh)
        }
        assertComponent(col1, FileListColumn) {
          case FileListColumnProps(resSize, left, boxStyle, itemStyle, items, focusedPos, selectedIds) =>
            resSize shouldBe 2 -> 2
            left shouldBe 0
            boxStyle shouldBe FileList.styles.normalItem
            itemStyle shouldBe FileList.styles.normalItem
            items shouldBe colItems.head
            focusedPos shouldBe -1
            selectedIds shouldBe Set.empty
        }
      })
      assertNativeComponent(colWrap2, <.>(^.key := "1")(), { children: List[ShallowInstance] =>
        val List(col2) = children
        assertComponent(col2, FileListColumn) {
          case FileListColumnProps(resSize, left, boxStyle, itemStyle, items, focusedPos, selectedIds) =>
            resSize shouldBe 4 -> 2
            left shouldBe 3
            boxStyle shouldBe FileList.styles.normalItem
            itemStyle shouldBe FileList.styles.normalItem
            items shouldBe colItems(1)
            focusedPos shouldBe -1
            selectedIds shouldBe Set.empty
        }
      })
    })
  }
}
