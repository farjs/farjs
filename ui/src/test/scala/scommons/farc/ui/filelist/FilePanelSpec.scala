package scommons.farc.ui.filelist

import org.scalatest.Assertion
import scommons.farc.api.filelist.{FileListApi, FileListItem}
import scommons.farc.ui._
import scommons.farc.ui.border._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

class FilePanelSpec extends TestSpec with ShallowRendererUtils {

  it should "set state when onStateChanged" in {
    //given
    val api = mock[FileListApi]
    val props = FilePanelProps(api, size = (25, 15))
    val renderer = createRenderer()
    renderer.render(<(FilePanel())(^.wrapped := props)())
    val listProps = findComponentProps(renderer.getRenderOutput(), FileList)
    listProps.state shouldBe FileListState()
    val newState = FileListState(offset = 1, index = 2)

    //when
    listProps.onStateChanged(newState)

    //then
    findComponentProps(renderer.getRenderOutput(), FileList).state shouldBe newState
  }
  
  it should "render empty component" in {
    //given
    val api = mock[FileListApi]
    val props = FilePanelProps(api, size = (25, 15))

    //when
    val result = shallowRender(<(FilePanel())(^.wrapped := props)())

    //then
    assertFilePanel(result, props, FileListState())
  }
  
  it should "render component with selected files" in {
    //given
    val api = mock[FileListApi]
    val props = FilePanelProps(api, size = (25, 15))
    val state = FileListState(index = 2, currDir = "/", items = List(
      FileListItem("dir 1", isDir = true, size = 1),
      FileListItem("dir 2", isDir = true, size = 2),
      FileListItem("file", size = 3)
    ), selectedNames = Set("dir 2", "file"))
    val renderer = createRenderer()

    //when
    renderer.render(<(FilePanel())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileList).onStateChanged(state)
    val result = renderer.getRenderOutput()

    //then
    assertFilePanel(result, props, state, "file", "3", showDate = true,
      selected = Some("5 in 2 file(s)"), dirSize = "3 (1)")
  }
  
  it should "render component with root dir and focused file" in {
    //given
    val api = mock[FileListApi]
    val props = FilePanelProps(api, size = (25, 15))
    val state = FileListState(index = 1, currDir = "/", items = List(
      FileListItem("file 1", size = 1),
      FileListItem("file 2", size = 2, permissions = "drwxr-xr-x"),
      FileListItem("file 3", size = 3)
    ))
    val renderer = createRenderer()

    //when
    renderer.render(<(FilePanel())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileList).onStateChanged(state)
    val result = renderer.getRenderOutput()

    //then
    assertFilePanel(result, props, state, "file 2", "2", permissions = "drwxr-xr-x", showDate = true, dirSize = "6 (3)")
  }
  
  it should "render component with sub-dir and focused dir" in {
    //given
    val api = mock[FileListApi]
    val props = FilePanelProps(api, size = (25, 15))
    val state = FileListState(index = 1, currDir = "/sub-dir", items = List(
      FileListItem.up,
      FileListItem("dir", isDir = true, size = 1, permissions = "dr--r--r--"),
      FileListItem("file", size = 2)
    ))
    val renderer = createRenderer()

    //when
    renderer.render(<(FilePanel())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileList).onStateChanged(state)
    val result = renderer.getRenderOutput()

    //then
    assertFilePanel(result, props, state, "dir", "1", permissions = "dr--r--r--", showDate = true, dirSize = "2 (1)")
  }
  
  it should "render component with sub-dir and focused .." in {
    //given
    val api = mock[FileListApi]
    val props = FilePanelProps(api, size = (25, 15))
    val state = FileListState(currDir = "/sub-dir", items = List(
      FileListItem.up,
      FileListItem("dir", isDir = true, size = 1),
      FileListItem("file", size = 2)
    ))
    val renderer = createRenderer()

    //when
    renderer.render(<(FilePanel())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileList).onStateChanged(state)
    val result = renderer.getRenderOutput()

    //then
    assertFilePanel(result, props, state, "..", dirSize = "2 (1)")
  }
  
  private def assertFilePanel(result: ShallowInstance,
                              props: FilePanelProps,
                              state: FileListState,
                              expectedFile: String = "",
                              expectedFileSize: String = "",
                              permissions: String = "",
                              showDate: Boolean = false,
                              selected: Option[String] = None,
                              dirSize: String = "0 (0)"): Unit = {
    
    val (width, height) = props.size
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

      assertComponent(border, DoubleBorder) { case DoubleBorderProps(resSize, style) =>
        resSize shouldBe width -> height
        style shouldBe styles.normalItem
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
        case FileListProps(resApi, resSize, columns, resState, _) =>
          resApi shouldBe props.api
          resSize shouldBe (width - 2) -> (height - 5)
          columns shouldBe 3
          resState shouldBe state
      }
      assertComponent(currFolder, TextLine) {
        case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
          align shouldBe TextLine.Center
          pos shouldBe 1 -> 0
          resWidth shouldBe (width - 2)
          text shouldBe state.currDir
          style shouldBe styles.normalItem
          focused shouldBe true
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
    
    assertNativeComponent(result, <.box(^.rbStyle := styles.normalItem)(), {
      case List(border, line, list, currFolder, currFile, fileSize, filePerm, fileDate, folderSize) =>
        assertComponents(border, line, list, currFolder, None, currFile, fileSize, filePerm, fileDate, folderSize)
      case List(border, line, list, currFolder, selection, currFile, fileSize, filePerm, fileDate, folderSize) =>
        assertComponents(border, line, list, currFolder, Some(selection), currFile, fileSize, filePerm, fileDate, folderSize)
    })
  }
}
