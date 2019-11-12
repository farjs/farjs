package farclone.ui.filelist

import org.scalatest.Assertion
import farclone.api.filelist._
import farclone.ui._
import farclone.ui.border._
import scommons.react._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

class FileListPanelSpec extends TestSpec with ShallowRendererUtils {

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState())

    //when
    val result = shallowRender(<(FileListPanel())(^.wrapped := props)())

    //then
    assertFileListPanel(result, props, FileListState())
  }
  
  it should "render component with selected files" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val state = FileListState(index = 2, currDir = FileListDir("/", isRoot = true, items = List(
      FileListItem("dir 1", isDir = true, size = 1),
      FileListItem("dir 2", isDir = true, size = 2),
      FileListItem("file", size = 3)
    )), selectedNames = Set("dir 2", "file"))
    val props = FileListPanelProps(dispatch, actions, state)

    //when
    val result = shallowRender(<(FileListPanel())(^.wrapped := props)())

    //then
    assertFileListPanel(result, props, state, "file", "3", showDate = true,
      selected = Some("5 in 2 file(s)"), dirSize = "3 (1)")
  }
  
  it should "render active component with root dir and focused file" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val state = FileListState(index = 1, currDir = FileListDir("/", isRoot = true, items = List(
      FileListItem("file 1", size = 1),
      FileListItem("file 2", size = 2, permissions = "drwxr-xr-x"),
      FileListItem("file 3", size = 3)
    )), isActive = true)
    val props = FileListPanelProps(dispatch, actions, state)

    //when
    val result = shallowRender(<(FileListPanel())(^.wrapped := props)())

    //then
    assertFileListPanel(result, props, state, "file 2", "2", permissions = "drwxr-xr-x", showDate = true, dirSize = "6 (3)")
  }
  
  it should "render component with root dir and focused file" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val state = FileListState(index = 1, currDir = FileListDir("/", isRoot = true, items = List(
      FileListItem("file 1", size = 1),
      FileListItem("file 2", size = 2, permissions = "drwxr-xr-x"),
      FileListItem("file 3", size = 3)
    )))
    val props = FileListPanelProps(dispatch, actions, state)

    //when
    val result = shallowRender(<(FileListPanel())(^.wrapped := props)())

    //then
    assertFileListPanel(result, props, state, "file 2", "2", permissions = "drwxr-xr-x", showDate = true, dirSize = "6 (3)")
  }
  
  it should "render component with sub-dir and focused dir" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val state = FileListState(index = 1, currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem.up,
      FileListItem("dir", isDir = true, size = 1, permissions = "dr--r--r--"),
      FileListItem("file", size = 2)
    )))
    val props = FileListPanelProps(dispatch, actions, state)

    //when
    val result = shallowRender(<(FileListPanel())(^.wrapped := props)())

    //then
    assertFileListPanel(result, props, state, "dir", "1", permissions = "dr--r--r--", showDate = true, dirSize = "2 (1)")
  }
  
  it should "render component with sub-dir and focused .." in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val state = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem.up,
      FileListItem("dir", isDir = true, size = 1),
      FileListItem("file", size = 2)
    )))
    val props = FileListPanelProps(dispatch, actions, state)

    //when
    val result = shallowRender(<(FileListPanel())(^.wrapped := props)())

    //then
    assertFileListPanel(result, props, state, "..", dirSize = "2 (1)")
  }
  
  private def assertFileListPanel(result: ShallowInstance,
                                  props: FileListPanelProps,
                                  state: FileListState,
                                  expectedFile: String = "",
                                  expectedFileSize: String = "",
                                  permissions: String = "",
                                  showDate: Boolean = false,
                                  selected: Option[String] = None,
                                  dirSize: String = "0 (0)"): Unit = {
    
    val (width, height) = (25, 15)
    val styles = FileListView.styles
    
    def assertComponents(border: ShallowInstance,
                         line: ShallowInstance,
                         list: ShallowInstance,
                         currFolder: ShallowInstance,
                         selection: Option[ShallowInstance],
                         currFile: ShallowInstance,
                         fileSize: ShallowInstance,
                         filePerm: ShallowInstance,
                         fileDate: ShallowInstance,
                         folderSize: ShallowInstance): Assertion = {

      assertComponent(border, DoubleBorder) {
        case DoubleBorderProps(resSize, style, pos, title) =>
          resSize shouldBe width -> height
          style shouldBe styles.normalItem
          pos shouldBe 0 -> 0
          title shouldBe None
      }
      assertComponent(line, HorizontalLine) {
        case HorizontalLineProps(pos, len, lineCh, style, startCh, endCh) =>
          pos shouldBe 0 -> (height - 4)
          len shouldBe width
          lineCh shouldBe SingleBorder.horizontalCh
          style shouldBe styles.normalItem
          startCh shouldBe Some(DoubleBorder.leftSingleCh)
          endCh shouldBe Some(DoubleBorder.rightSingleCh)
      }
      assertComponent(list, FileList) {
        case FileListProps(dispatch, actions, resState, resSize, columns) =>
          dispatch shouldBe props.dispatch
          actions shouldBe props.actions
          resState shouldBe state
          resSize shouldBe (width - 2) -> (height - 5)
          columns shouldBe 2
      }
      assertComponent(currFolder, TextLine) {
        case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
          align shouldBe TextLine.Center
          pos shouldBe 1 -> 0
          resWidth shouldBe (width - 2)
          text shouldBe state.currDir.path
          style shouldBe styles.normalItem
          focused shouldBe props.state.isActive
          padding shouldBe 1
      }
      
      selection.size shouldBe selected.size
      selection.foreach { selectedText =>
        assertComponent(selectedText, TextLine) {
          case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
            align shouldBe TextLine.Center
            pos shouldBe 1 -> (height - 4)
            resWidth shouldBe (width - 2)
            text shouldBe selected.get
            style shouldBe styles.selectedItem
            focused shouldBe false
            padding shouldBe 1
        }
      }
      
      assertComponent(currFile, TextLine) {
        case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
          align shouldBe TextLine.Left
          pos shouldBe 1 -> (height - 3)
          resWidth shouldBe (width - 2 - 12)
          text shouldBe expectedFile
          style shouldBe styles.normalItem
          focused shouldBe false
          padding shouldBe 0
      }
      assertComponent(fileSize, TextLine) {
        case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
          align shouldBe TextLine.Right
          pos shouldBe (1 + width - 2 - 12) -> (height - 3)
          resWidth shouldBe 12
          text shouldBe expectedFileSize
          style shouldBe styles.normalItem
          focused shouldBe false
          padding shouldBe 0
      }
      
      assertComponent(filePerm, TextLine) {
        case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
          align shouldBe TextLine.Left
          pos shouldBe 1 -> (height - 2)
          resWidth shouldBe 10
          text shouldBe permissions
          style shouldBe styles.normalItem
          focused shouldBe false
          padding shouldBe 0
      }
      assertComponent(fileDate, TextLine) {
        case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
          align shouldBe TextLine.Right
          pos shouldBe (1 + width - 2 - 25) -> (height - 2)
          resWidth shouldBe 25
          if (showDate) text should not be empty
          else text should be (empty)
          style shouldBe styles.normalItem
          focused shouldBe false
          padding shouldBe 0
      }
      
      assertComponent(folderSize, TextLine) {
        case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
          align shouldBe TextLine.Center
          pos shouldBe 1 -> (height - 1)
          resWidth shouldBe (width - 2)
          text shouldBe dirSize
          style shouldBe styles.normalItem
          focused shouldBe false
          padding shouldBe 1
      }
    }
    
    def renderContent(content: ReactElement): ShallowInstance = {
      val wrapper = new ClassComponent[Unit] {
        protected def create(): ReactClass = createClass[Unit](_ => content)
      }
      
      shallowRender(<(wrapper()).empty)
    }

    assertComponent(result, WithSize) { case WithSizeProps(render) =>
      val result = renderContent(render(width, height))
      
      assertNativeComponent(result, <.box(^.rbStyle := styles.normalItem)(), {
        case List(border, line, list, currFolder, currFile, fileSize, filePerm, fileDate, folderSize) =>
          assertComponents(border, line, list, currFolder, None, currFile, fileSize, filePerm, fileDate, folderSize)
        case List(border, line, list, currFolder, selection, currFile, fileSize, filePerm, fileDate, folderSize) =>
          assertComponents(border, line, list, currFolder, Some(selection), currFile, fileSize, filePerm, fileDate, folderSize)
      })
    }
  }
}
