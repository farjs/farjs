package farjs.filelist

import farjs.filelist.FileListView._
import farjs.filelist.api.FileListItem
import farjs.filelist.api.FileListItemSpec.assertFileListItems
import farjs.filelist.stack.WithStackSpec
import farjs.filelist.theme.FileListTheme
import farjs.filelist.theme.FileListThemeSpec.withThemeContext
import farjs.ui.border._
import org.scalatest.Assertion
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class FileListViewSpec extends TestSpec with TestRendererUtils {

  FileListView.verticalLineComp = "VerticalLine".asInstanceOf[ReactClass]
  FileListView.fileListColumnComp = mockUiComponent("FileListColumn")

  it should "call onWheel when onWheelup/onWheeldown" in {
    //given
    val onWheel = mockFunction[Boolean, Unit]
    val props = FileListViewProps(7, 7, columns = 2, items = js.Array(
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
    
    val renderer = createTestRenderer(withContext(withThemeContext(<(FileListView())(^.plain := props)()), input), { el =>
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
    val props = FileListViewProps(7, 3, columns = 2, items = js.Array(
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
    
    val renderer = createTestRenderer(withContext(withThemeContext(<(FileListView())(^.plain := props)()), input), { el =>
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
    val props = FileListViewProps(7, 3, columns = 2, items = js.Array(
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
    
    val renderer = createTestRenderer(withContext(withThemeContext(<(FileListView())(^.plain := props)()), input))
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
    val props = FileListViewProps(13, 1, columns = 2,
      items = js.Array(FileListItem("item 1"), FileListItem("item 2"))
    )

    //when
    val result = testRender(withContext(withThemeContext(<(FileListView())(^.plain := props)())))

    //then
    assertNativeComponent(result, <.box(^.rbWidth := props.width, ^.rbHeight := props.height)())
  }
  
  it should "render empty component when columns = 0" in {
    //given
    val props = FileListViewProps(13, 12, columns = 0, items = js.Array[FileListItem]())

    //when
    val result = testRender(withContext(withThemeContext(<(FileListView())(^.plain := props)())))

    //then
    assertNativeComponent(result, <.box(^.rbWidth := props.width, ^.rbHeight := props.height)())
  }
  
  it should "render empty component with 2 columns" in {
    //given
    val props = FileListViewProps(7, 2, columns = 2, items = js.Array[FileListItem]())

    //when
    val result = testRender(withContext(withThemeContext(<(FileListView())(^.plain := props)())))

    //then
    assertFileListView(result, props, List(
      (Nil, -1, Set.empty),
      (Nil, -1, Set.empty)
    ))
  }
  
  it should "render non-empty component with 2 columns" in {
    //given
    val props = FileListViewProps(7, 2, columns = 2,
      items = js.Array(FileListItem("item 1"), FileListItem("item 2")),
      focusedIndex = 1,
      selectedNames = js.Set("item 2")
    )

    //when
    val result = testRender(withContext(withThemeContext(<(FileListView())(^.plain := props)())))

    //then
    assertFileListView(result, props, List(
      (List(FileListItem("item 1")), -1, Set.empty),
      (List(FileListItem("item 2")), 0, Set("item 2"))
    ))
  }
  
  private def withContext(element: ReactElement, panelInput: BlessedElement = null): ReactElement = {
    WithStackSpec.withContext(element, panelInput = panelInput)
  }
  
  private def assertFileListView(result: TestInstance,
                                 props: FileListViewProps,
                                 expectedData: List[(List[FileListItem], Int, Set[String])]): Unit = {

    val currThem = FileListTheme.defaultTheme
    
    assertNativeComponent(result, <.box(
      ^.rbWidth := props.width,
      ^.rbHeight := props.height,
      ^.rbLeft := 1,
      ^.rbTop := 1
    )(), inside(_) { case List(sep, col1, col2) =>
      assertNativeComponent(sep, <(verticalLineComp)(^.assertPlain[VerticalLineProps](inside(_) {
        case VerticalLineProps(resLeft, resTop, resLength, ch, style, start, end) =>
          resLeft shouldBe 2
          resTop shouldBe -1
          resLength shouldBe 4
          ch shouldBe SingleChars.vertical
          style shouldBe currThem.fileList.regularItem
          start shouldBe DoubleChars.topSingle
          end shouldBe SingleChars.bottom
      }))())
      assertTestComponent(col1, fileListColumnComp, plain = true) {
        case FileListColumnProps(width, height, left, borderCh, items, focusedPos, selectedNames) =>
          width shouldBe 2
          height shouldBe 2
          left shouldBe 0
          borderCh shouldBe SingleChars.vertical
          assertFileListData((items.toList, focusedPos, selectedNames.toSet), expectedData.head)
      }
      assertTestComponent(col2, fileListColumnComp, plain = true) {
        case FileListColumnProps(width, height, left, borderCh, items, focusedPos, selectedNames) =>
          width shouldBe 4
          height shouldBe 2
          left shouldBe 3
          borderCh shouldBe DoubleChars.vertical
          assertFileListData((items.toList, focusedPos, selectedNames.toSet), expectedData(1))
      }
    })
  }
  
  private def assertFileListData(result: (Seq[FileListItem], Int, Set[String]),
                                 expected: (List[FileListItem], Int, Set[String])): Assertion = {

    val (resItems, resFocusedPos, resSelectedNames) = result
    val (items, focusedPos, selectedNames) = expected

    assertFileListItems(resItems, items)
    (resFocusedPos, resSelectedNames) shouldBe (focusedPos, selectedNames)
  }
}
