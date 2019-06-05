package scommons.farc.ui.filelist

import scommons.farc.ui.border._
import scommons.react._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.{ShallowInstance, TestRenderer}
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class FileListViewSpec extends TestSpec
  with ShallowRendererUtils
  with TestRendererUtils {

  it should "call onWheelup/onWheeldown" in {
    //given
    val onWheelUp = mockFunction[Unit]
    val onWheelDown = mockFunction[Unit]
    val props = FileListViewProps((7, 7), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2"
    ), onWheelUp = onWheelUp, onWheelDown = onWheelDown)
    val root = createTestRenderer(<(FileListView())(^.wrapped := props)(), { el =>
      if (el.`type` == "button".asInstanceOf[js.Any]) literal(aleft = 5, atop = 3)
      else null
    }).root

    def check(up: Boolean, shift: Boolean = false): Unit = {
      //then
      if (!shift) {
        if (up) onWheelUp.expects()
        else onWheelDown.expects()
      }
      
      //when
      TestRenderer.act { () =>
        if (up) root.children(0).props.onWheelup(literal("shift" -> shift))
        else root.children(0).props.onWheeldown(literal("shift" -> shift))
      }
    }
    
    //when & then
    check(up = false)
    check(up = true)
    check(up = false, shift = true)
    check(up = true, shift = true)
  }

  it should "call onClick when onClick" in {
    //given
    val onClick = mockFunction[Int, Unit]
    val props = FileListViewProps((7, 3), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2",
      3 -> "item 3"
    ), onClick = onClick)
    val root = createTestRenderer(<(FileListView())(^.wrapped := props)(), { el =>
      if (el.`type` == "button".asInstanceOf[js.Any]) literal(aleft = 5, atop = 3)
      else null
    }).root

    def check(x: Int, y: Int, index: Int): Unit = {
      //then
      onClick.expects(index)

      //when
      TestRenderer.act { () =>
        root.children(0).props.onClick(literal(x = x, y = y))
      }
    }
    
    //when & then
    check(x = 6, y = 3, index = 0) // header in col 1
    check(x = 6, y = 4, index = 0) // first item in col 1
    check(x = 6, y = 5, index = 1) // second item in col 1

    //when & then
    check(x = 8, y = 3, index = 2) // header in col 2
    check(x = 8, y = 4, index = 2) // first item in col 2
    check(x = 8, y = 5, index = 3) // last item in col 2
  }

  it should "call onKeypress when onKeypress" in {
    //given
    val onKeypress = mockFunction[String, Unit]
    val props = FileListViewProps((7, 3), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2"
    ), onKeypress = onKeypress)
    val comp = shallowRender(<(FileListView())(^.wrapped := props)())
    val keyFull = "some-key"
    
    //then
    onKeypress.expects(keyFull)
    
    //when
    comp.props.onKeypress(null, literal(full = keyFull))
  }

  it should "render empty component when height < 2" in {
    //given
    val props = FileListViewProps((1, 1), columns = 2,
      items = List(1 -> "item 1", 2 -> "item 2")
    )

    //when
    val result = shallowRender(<(FileListView())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.button(^.rbMouse := true)())
  }
  
  it should "render empty component when columns = 0" in {
    //given
    val props = FileListViewProps((1, 2), columns = 0, items = Nil)

    //when
    val result = shallowRender(<(FileListView())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.button(^.rbMouse := true)())
  }
  
  it should "render empty component with 2 columns" in {
    //given
    val props = FileListViewProps((7, 2), columns = 2, items = Nil)
    val comp = <(FileListView())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileListView(result, props, List(
      (Nil, -1, Set.empty),
      (Nil, -1, Set.empty)
    ))
  }
  
  it should "render non-empty component with 2 columns" in {
    //given
    val props = FileListViewProps((7, 2), columns = 2,
      items = List(1 -> "item 1", 2 -> "item 2"),
      focusedIndex = 1,
      selectedIds = Set(2)
    )
    val comp = <(FileListView())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileListView(result, props, List(
      (List(1 -> "item 1"), -1, Set.empty),
      (List(2 -> "item 2"), 0, Set(2))
    ))
  }
  
  private def assertFileListView(result: ShallowInstance,
                                 props: FileListViewProps,
                                 expectedData: List[(List[(Int, String)], Int, Set[Int])]): Unit = {
    
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
            style shouldBe FileListView.styles.normalItem
            start shouldBe Some(DoubleBorder.topSingleCh)
            end shouldBe Some(SingleBorder.bottomCh)
        }
        assertComponent(col1, FileListColumn) {
          case FileListColumnProps(resSize, left, borderCh, items, focusedPos, selectedIds) =>
            resSize shouldBe 2 -> 2
            left shouldBe 0
            borderCh shouldBe SingleBorder.verticalCh
            (items, focusedPos, selectedIds) shouldBe expectedData.head
        }
      })
      assertNativeComponent(colWrap2, <.>(^.key := "1")(), { children: List[ShallowInstance] =>
        val List(col2) = children
        assertComponent(col2, FileListColumn) {
          case FileListColumnProps(resSize, left, borderCh, items, focusedPos, selectedIds) =>
            resSize shouldBe 4 -> 2
            left shouldBe 3
            borderCh shouldBe DoubleBorder.verticalCh
            (items, focusedPos, selectedIds) shouldBe expectedData(1)
        }
      })
    })
  }
}
