package farjs.filelist

import farjs.filelist.FileListPanelView._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.sort.{FileListSort, SortIndicatorProps}
import farjs.filelist.stack.{PanelStack, PanelStackItem, WithStackSpec}
import farjs.filelist.theme.FileListTheme
import farjs.filelist.theme.FileListThemeSpec.withThemeContext
import farjs.ui._
import farjs.ui.border._
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class FileListPanelViewSpec extends TestSpec with TestRendererUtils {

  FileListPanelView.doubleBorderComp = "DoubleBorder".asInstanceOf[ReactClass]
  FileListPanelView.horizontalLineComp = "HorizontalLine".asInstanceOf[ReactClass]
  FileListPanelView.fileListComp = mockUiComponent("FileList")
  FileListPanelView.textLineComp = "TextLine".asInstanceOf[ReactClass]
  FileListPanelView.sortIndicator = "SortIndicator".asInstanceOf[ReactClass]

  private val (width, height) = (25, 15)
  
  it should "render empty component" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = FileListPanelViewProps(dispatch, actions, state)

    //when
    val result = testRender(withContext(withThemeContext(<(FileListPanelView())(^.wrapped := props)())))

    //then
    assertFileListPanelView(result, props, state)
  }
  
  it should "render component with selected one file and with diskSpace" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState(index = 2, currDir = FileListDir("/", isRoot = true, items = js.Array(
      FileListItem.copy(FileListItem("dir 1", isDir = true))(size = 1),
      FileListItem.copy(FileListItem("dir 2", isDir = true))(size = 2),
      FileListItem.copy(FileListItem("file"))(size = 3)
    )), selectedNames = js.Set("dir 2"), diskSpace = 123.45)
    val props = FileListPanelViewProps(dispatch, actions, state)

    //when
    val result = testRender(withContext(withThemeContext(<(FileListPanelView())(^.wrapped := props)())))

    //then
    assertFileListPanelView(result, props, state, "file", "3", showDate = true,
      selected = Some("2 in 1 file"), dirSize = "3 (1)", diskSpace = Some(123.45))
  }
  
  it should "render component with selected more than one file" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState(index = 2, currDir = FileListDir("/", isRoot = true, items = js.Array(
      FileListItem.copy(FileListItem("dir 1", isDir = true))(size = 1),
      FileListItem.copy(FileListItem("dir 2", isDir = true))(size = 2),
      FileListItem.copy(FileListItem("file"))(size = 3)
    )), selectedNames = js.Set("dir 2", "file"))
    val props = FileListPanelViewProps(dispatch, actions, state)

    //when
    val result = testRender(withContext(withThemeContext(<(FileListPanelView())(^.wrapped := props)())))

    //then
    assertFileListPanelView(result, props, state, "file", "3", showDate = true,
      selected = Some("5 in 2 files"), dirSize = "3 (1)")
  }
  
  it should "render active component with root dir and focused file" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState(index = 1, currDir = FileListDir("/", isRoot = true, items = js.Array(
      FileListItem.copy(FileListItem("file 1"))(size = 1),
      FileListItem.copy(FileListItem("file 2"))(size = 2, permissions = "drwxr-xr-x"),
      FileListItem.copy(FileListItem("file 3"))(size = 3)
    )))
    val props = FileListPanelViewProps(dispatch, actions, state)

    //when
    val result = testRender(withContext(withThemeContext(<(FileListPanelView())(^.wrapped := props)()), isActive = true))

    //then
    assertFileListPanelView(result, props, state, "file 2", "2", permissions = "drwxr-xr-x", showDate = true, dirSize = "6 (3)", isActive = true)
  }
  
  it should "render component with root dir and focused dir" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState(index = 1, currDir = FileListDir("/", isRoot = true, items = js.Array(
      FileListItem.copy(FileListItem("file 1"))(size = 1),
      FileListItem.copy(FileListItem("dir 2", isDir = true))(size = 999999999, permissions = "drwxr-xr-x"),
      FileListItem.copy(FileListItem("file 3"))(size = 3)
    )))
    val props = FileListPanelViewProps(dispatch, actions, state)

    //when
    val result = testRender(withContext(withThemeContext(<(FileListPanelView())(^.wrapped := props)())))

    //then
    assertFileListPanelView(result, props, state, "dir 2", "999,999,999", permissions = "drwxr-xr-x", showDate = true, dirSize = "4 (2)")
  }
  
  it should "render component with root dir and focused file of big size" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState(index = 1, currDir = FileListDir("/", isRoot = true, items = js.Array(
      FileListItem.copy(FileListItem("file 1"))(size = 1),
      FileListItem.copy(FileListItem("file 2"))(size = 1123456789, permissions = "drwxr-xr-x"),
      FileListItem.copy(FileListItem("file 3"))(size = 3)
    )))
    val props = FileListPanelViewProps(dispatch, actions, state)

    //when
    val result = testRender(withContext(withThemeContext(<(FileListPanelView())(^.wrapped := props)())))

    //then
    assertFileListPanelView(result, props, state, "file 2", "~1 G", permissions = "drwxr-xr-x", showDate = true, dirSize = "1,123,456,793 (3)")
  }
  
  it should "render component with sub-dir and focused dir" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState(index = 1, currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem.up,
      FileListItem.copy(FileListItem("dir", isDir = true))(size = 1, permissions = "dr--r--r--"),
      FileListItem.copy(FileListItem("file"))(size = 2)
    )))
    val props = FileListPanelViewProps(dispatch, actions, state)

    //when
    val result = testRender(withContext(withThemeContext(<(FileListPanelView())(^.wrapped := props)())))

    //then
    assertFileListPanelView(result, props, state, "dir", "1", permissions = "dr--r--r--", showDate = true, dirSize = "2 (1)")
  }
  
  it should "render component with sub-dir and focused .." in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem.up,
      FileListItem.copy(FileListItem("dir", isDir = true))(size = 1),
      FileListItem.copy(FileListItem("file"))(size = 2)
    )))
    val props = FileListPanelViewProps(dispatch, actions, state)

    //when
    val result = testRender(withContext(withThemeContext(<(FileListPanelView())(^.wrapped := props)())))

    //then
    assertFileListPanelView(result, props, state, "..", dirSize = "2 (1)")
  }

  private def withContext(element: ReactElement, isActive: Boolean = false): ReactElement = {
    WithStackSpec.withContext(
      element = element,
      stack = new PanelStack(isActive, js.Array[PanelStackItem[_]](), _ => ()),
      width = width,
      height = height
    )
  }

  private def assertFileListPanelView(result: TestInstance,
                                      props: FileListPanelViewProps,
                                      state: FileListState,
                                      expectedFile: String = "",
                                      expectedFileSize: String = "",
                                      permissions: String = "",
                                      showDate: Boolean = false,
                                      selected: Option[String] = None,
                                      dirSize: String = "0 (0)",
                                      diskSpace: Option[Double] = None,
                                      isActive: Boolean = false): Unit = {
    
    val theme = FileListTheme.defaultTheme.fileList
    
    assertNativeComponent(result, <.box(^.rbStyle := theme.regularItem)(
      <(doubleBorderComp)(^.assertPlain[DoubleBorderProps](inside(_) {
        case DoubleBorderProps(resWidth, resHeight, style, resLeft, resTop, title, footer) =>
          resWidth shouldBe width
          resHeight shouldBe height
          style shouldBe theme.regularItem
          resLeft shouldBe js.undefined
          resTop shouldBe js.undefined
          title shouldBe js.undefined
          footer shouldBe js.undefined
      }))(),
      <(horizontalLineComp)(^.assertPlain[HorizontalLineProps](inside(_) {
        case HorizontalLineProps(resLeft, resTop, len, lineCh, style, startCh, endCh) =>
          resLeft shouldBe 0
          resTop shouldBe (height - 4)
          len shouldBe width
          lineCh shouldBe SingleChars.horizontal
          style shouldBe theme.regularItem
          startCh shouldBe DoubleChars.leftSingle
          endCh shouldBe DoubleChars.rightSingle
      }))(),
      <(fileListComp())(^.assertPlain[FileListProps](inside(_) {
        case FileListProps(dispatch, actions, resState, resWidth, resHeight, columns, _) =>
          dispatch shouldBe props.dispatch
          actions shouldBe props.actions
          resState shouldBe state
          resWidth shouldBe (width - 2)
          resHeight shouldBe (height - 5)
          columns shouldBe 2
      }))(),
      <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
        case TextLineProps(align, left, top, resWidth, text, style, focused, padding) =>
          align shouldBe TextAlign.center
          left shouldBe 1
          top shouldBe 0
          resWidth shouldBe (width - 2)
          text shouldBe state.currDir.path
          style shouldBe theme.regularItem
          focused shouldBe isActive
          padding shouldBe js.undefined
      }))(),
      <(sortIndicator)(^.assertPlain[SortIndicatorProps](inside(_) {
        case SortIndicatorProps(FileListSort(mode, asc)) =>
          mode shouldBe props.state.sort.mode
          asc shouldBe props.state.sort.asc
      }))(),
      
      selected.map { selectedText =>
        <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
          case TextLineProps(align, left, top, resWidth, text, style, focused, padding) =>
            align shouldBe TextAlign.center
            left shouldBe 1
            top shouldBe (height - 4)
            resWidth shouldBe (width - 2)
            text shouldBe selectedText
            style shouldBe theme.selectedItem
            focused shouldBe js.undefined
            padding shouldBe js.undefined
        }))()
      },
      
      <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
        case TextLineProps(align, left, top, resWidth, text, style, focused, padding) =>
          align shouldBe TextAlign.left
          left shouldBe 1
          top shouldBe (height - 3)
          resWidth shouldBe (width - 2 - 12)
          text shouldBe expectedFile
          style shouldBe theme.regularItem
          focused shouldBe js.undefined
          padding shouldBe 0
      }))(),
      <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
        case TextLineProps(align, left, top, resWidth, text, style, focused, padding) =>
          align shouldBe TextAlign.right
          left shouldBe (1 + width - 2 - 12)
          top shouldBe (height - 3)
          resWidth shouldBe 12
          text shouldBe expectedFileSize
          style shouldBe theme.regularItem
          focused shouldBe js.undefined
          padding shouldBe 0
      }))(),
      
      <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
        case TextLineProps(align, left, top, resWidth, text, style, focused, padding) =>
          align shouldBe TextAlign.left
          left shouldBe 1
          top shouldBe (height - 2)
          resWidth shouldBe 10
          text shouldBe permissions
          style shouldBe theme.regularItem
          focused shouldBe js.undefined
          padding shouldBe 0
      }))(),
      <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
        case TextLineProps(align, left, top, resWidth, text, style, focused, padding) =>
          align shouldBe TextAlign.right
          left shouldBe (1 + width - 2 - 25)
          top shouldBe (height - 2)
          resWidth shouldBe 25
          if (showDate) text should not be empty
          else text should be (empty)
          style shouldBe theme.regularItem
          focused shouldBe js.undefined
          padding shouldBe 0
      }))(),
      
      <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
        case TextLineProps(align, left, top, resWidth, text, style, focused, padding) =>
          align shouldBe TextAlign.center
          left shouldBe 1
          top shouldBe (height - 1)
          resWidth shouldBe (if (diskSpace.isEmpty) width - 2 else (width - 2) / 2)
          text shouldBe dirSize
          style shouldBe theme.regularItem
          focused shouldBe js.undefined
          padding shouldBe js.undefined
      }))(),
      diskSpace.map { bytes =>
        <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
          case TextLineProps(align, left, top, resWidth, text, style, focused, padding) =>
            align shouldBe TextAlign.center
            left shouldBe ((width - 2) / 2 + 1)
            top shouldBe (height - 1)
            resWidth shouldBe (width - 2) / 2
            text shouldBe f"$bytes%,.0f"
            style shouldBe theme.regularItem
            focused shouldBe js.undefined
            padding shouldBe js.undefined
        }))()
      }
    ))
  }
}
