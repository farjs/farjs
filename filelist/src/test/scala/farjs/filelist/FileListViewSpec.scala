package farjs.filelist

import farjs.filelist.FileListView._
import farjs.filelist.api.FileListItem
import farjs.filelist.stack.{PanelStack, PanelStackProps}
import farjs.ui.border._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class FileListViewSpec extends TestSpec with TestRendererUtils {

  FileListView.verticalLineComp = mockUiComponent("VerticalLine")
  FileListView.fileListColumnComp = mockUiComponent("FileListColumn")

  it should "call onWheel when onWheelup/onWheeldown" in {
    //given
    val onWheel = mockFunction[Boolean, Unit]
    val props = FileListViewProps((7, 7), columns = 2, items = List(
      FileListItem("item 1"),
      FileListItem("item 2")
    ), onWheel = onWheel)
    val onMock = mockFunction[String, js.Function, BlessedEventEmitter]
    val offMock = mockFunction[String, js.Function, BlessedEventEmitter]
    val input = literal(
      "on" -> onMock,
      "off" -> offMock
    ).asInstanceOf[BlessedElement]

    var wheelupListener: js.Function1[MouseData, Unit] = null
    var wheeldownListener: js.Function1[MouseData, Unit] = null
    onMock.expects("wheelup", *).onCall { (_: String, listener: js.Function) =>
      wheelupListener = listener.asInstanceOf[js.Function1[MouseData, Unit]]
      input
    }
    onMock.expects("wheeldown", *).onCall { (_: String, listener: js.Function) =>
      wheeldownListener = listener.asInstanceOf[js.Function1[MouseData, Unit]]
      input
    }
    onMock.expects("keypress", *)
    onMock.expects("click", *)
    
    val renderer = createTestRenderer(withContext(<(FileListView())(^.wrapped := props)(), input), { el =>
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) literal(aleft = 5, atop = 3)
      else null
    })

    def check(up: Boolean, shift: Boolean = false): Unit = {
      //then
      if (!shift) {
        onWheel.expects(up)
      }

      //when
      TestRenderer.act { () =>
        if (up) wheelupListener(literal("shift" -> shift).asInstanceOf[MouseData])
        else wheeldownListener(literal("shift" -> shift).asInstanceOf[MouseData])
      }
    }

    //when & then
    check(up = false)
    check(up = true)
    check(up = false, shift = true)
    check(up = true, shift = true)
    
    //cleanup
    offMock.expects("click", *)
    offMock.expects("keypress", *)
    offMock.expects("wheelup", wheelupListener)
    offMock.expects("wheeldown", wheeldownListener)
    renderer.unmount()
  }

  it should "call onClick when onClick" in {
    //given
    val onClick = mockFunction[Int, Unit]
    val props = FileListViewProps((7, 3), columns = 2, items = List(
      FileListItem("item 1"),
      FileListItem("item 2"),
      FileListItem("item 3")
    ), onClick = onClick)
    val onMock = mockFunction[String, js.Function, BlessedEventEmitter]
    val offMock = mockFunction[String, js.Function, BlessedEventEmitter]
    val input = literal(
      "on" -> onMock,
      "off" -> offMock
    ).asInstanceOf[BlessedElement]

    var clickListener: js.Function1[MouseData, Unit] = null
    onMock.expects("click", *).onCall { (_: String, listener: js.Function) =>
      clickListener = listener.asInstanceOf[js.Function1[MouseData, Unit]]
      input
    }
    onMock.expects("keypress", *)
    onMock.expects("wheelup", *)
    onMock.expects("wheeldown", *)
    
    val renderer = createTestRenderer(withContext(<(FileListView())(^.wrapped := props)(), input), { el =>
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) literal(aleft = 5, atop = 3)
      else null
    })

    def check(x: Int, y: Int, index: Int): Unit = {
      //then
      onClick.expects(index)

      //when
      TestRenderer.act { () =>
        clickListener(literal(x = x, y = y).asInstanceOf[MouseData])
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
    
    //cleanup
    offMock.expects("click", clickListener)
    offMock.expects("keypress", *)
    offMock.expects("wheelup", *)
    offMock.expects("wheeldown", *)
    renderer.unmount()
  }

  it should "call onKeypress when onKeypress(...)" in {
    //given
    val onKeypress = mockFunction[BlessedScreen, String, Unit]
    val props = FileListViewProps((7, 3), columns = 2, items = List(
      FileListItem("item 1"),
      FileListItem("item 2")
    ), onKeypress = onKeypress)
    val onMock = mockFunction[String, js.Function, BlessedEventEmitter]
    val offMock = mockFunction[String, js.Function, BlessedEventEmitter]
    val screen = literal().asInstanceOf[BlessedScreen]
    val input = literal(
      "screen" -> screen,
      "on" -> onMock,
      "off" -> offMock
    ).asInstanceOf[BlessedElement]

    var keyListener: js.Function2[js.Object, KeyboardKey, Unit] = null
    onMock.expects("keypress", *).onCall { (_: String, listener: js.Function) =>
      keyListener = listener.asInstanceOf[js.Function2[js.Object, KeyboardKey, Unit]]
      input
    }
    onMock.expects("wheelup", *)
    onMock.expects("wheeldown", *)
    onMock.expects("click", *)
    
    val renderer = createTestRenderer(withContext(<(FileListView())(^.wrapped := props)(), input))
    val keyFull = "some-key"
    
    //then
    onKeypress.expects(screen, keyFull)
    
    //when
    keyListener(null, literal(full = keyFull).asInstanceOf[KeyboardKey])

    //cleanup
    offMock.expects("keypress", keyListener)
    offMock.expects("wheelup", *)
    offMock.expects("wheeldown", *)
    offMock.expects("click", *)
    renderer.unmount()
  }

  it should "render empty component when height < 2" in {
    //given
    val props = FileListViewProps((13, 1), columns = 2,
      items = List(FileListItem("item 1"), FileListItem("item 2"))
    )
    val (width, height) = props.size

    //when
    val result = testRender(withContext(<(FileListView())(^.wrapped := props)()))

    //then
    assertNativeComponent(result, <.box(^.rbWidth := width, ^.rbHeight := height)())
  }
  
  it should "render empty component when columns = 0" in {
    //given
    val props = FileListViewProps((13, 12), columns = 0, items = Nil)
    val (width, height) = props.size

    //when
    val result = testRender(withContext(<(FileListView())(^.wrapped := props)()))

    //then
    assertNativeComponent(result, <.box(^.rbWidth := width, ^.rbHeight := height)())
  }
  
  it should "render empty component with 2 columns" in {
    //given
    val props = FileListViewProps((7, 2), columns = 2, items = Nil)

    //when
    val result = testRender(withContext(<(FileListView())(^.wrapped := props)()))

    //then
    assertFileListView(result, props, List(
      (Nil, -1, Set.empty),
      (Nil, -1, Set.empty)
    ))
  }
  
  it should "render non-empty component with 2 columns" in {
    //given
    val props = FileListViewProps((7, 2), columns = 2,
      items = List(FileListItem("item 1"), FileListItem("item 2")),
      focusedIndex = 1,
      selectedNames = Set("item 2")
    )

    //when
    val result = testRender(withContext(<(FileListView())(^.wrapped := props)()))

    //then
    assertFileListView(result, props, List(
      (List(FileListItem("item 1")), -1, Set.empty),
      (List(FileListItem("item 2")), 0, Set("item 2"))
    ))
  }
  
  private def withContext(element: ReactElement, panelInput: BlessedElement = null): ReactElement = {
    <(PanelStack.Context.Provider)(^.contextValue := PanelStackProps(isRight = false, panelInput))(
      element
    )
  }
  
  private def assertFileListView(result: TestInstance,
                                 props: FileListViewProps,
                                 expectedData: List[(List[FileListItem], Int, Set[String])]): Unit = {
    
    assertNativeComponent(result, <.box(
      ^.rbWidth := props.size._1,
      ^.rbHeight := props.size._2,
      ^.rbLeft := 1,
      ^.rbTop := 1
    )(), inside(_) { case List(sep, col1, col2) =>
      assertTestComponent(sep, verticalLineComp) {
        case VerticalLineProps(pos, resLength, ch, style, start, end) =>
          pos shouldBe 2 -> -1
          resLength shouldBe 4
          ch shouldBe SingleBorder.verticalCh
          style shouldBe Theme.current.fileList.regularItem
          start shouldBe Some(DoubleBorder.topSingleCh)
          end shouldBe Some(SingleBorder.bottomCh)
      }
      assertTestComponent(col1, fileListColumnComp) {
        case FileListColumnProps(resSize, left, borderCh, items, focusedPos, selectedNames) =>
          resSize shouldBe 2 -> 2
          left shouldBe 0
          borderCh shouldBe SingleBorder.verticalCh
          (items, focusedPos, selectedNames) shouldBe expectedData.head
      }
      assertTestComponent(col2, fileListColumnComp) {
        case FileListColumnProps(resSize, left, borderCh, items, focusedPos, selectedNames) =>
          resSize shouldBe 4 -> 2
          left shouldBe 3
          borderCh shouldBe DoubleBorder.verticalCh
          (items, focusedPos, selectedNames) shouldBe expectedData(1)
      }
    })
  }
}
