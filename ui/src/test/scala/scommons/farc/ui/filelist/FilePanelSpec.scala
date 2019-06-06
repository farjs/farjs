package scommons.farc.ui.filelist

import scommons.farc.ui._
import scommons.farc.ui.border._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.util.ShallowRendererUtils

class FilePanelSpec extends TestSpec with ShallowRendererUtils {

  it should "set state when onStateChanged" in {
    //given
    val props = FilePanelProps(size = (25, 15))
    val renderer = createRenderer()
    renderer.render(<(FilePanel())(^.wrapped := props)())
    val listProps = findComponentProps(renderer.getRenderOutput(), FileList)
    listProps.state shouldBe FileListState()
    val newState = FileListState(1, 2, Set(3))

    //when
    listProps.onStateChanged(newState)

    //then
    findComponentProps(renderer.getRenderOutput(), FileList).state shouldBe newState
  }
  
  it should "render component" in {
    //given
    val props = FilePanelProps(size = (25, 15))

    //when
    val result = shallowRender(<(FilePanel())(^.wrapped := props)())

    //then
    val styles = FileListView.styles
    assertNativeComponent(result, <.box(^.rbStyle := styles.normalItem)(), {
      case List(border, line, list, currFolder,  currFile, fileSize, folderSize, freeSpace) =>
        val (width, height) = props.size
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
          case FileListProps(resSize, columns, items, state, _) =>
            resSize shouldBe (width - 2) -> (height - 5)
            columns shouldBe 3
            items should not be empty
            state shouldBe FileListState()
        }
        
        assertComponent(currFolder, TextLine) {
          case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
            align shouldBe TextLine.Center
            pos shouldBe 1 -> 0
            resWidth shouldBe (width - 2)
            text shouldBe "/current/folder"
            style shouldBe styles.normalItem
            focused shouldBe true
            padding shouldBe 1
        }
        
        assertComponent(currFile, TextLine) {
          case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
            align shouldBe TextLine.Left
            pos shouldBe 1 -> (height - 3)
            resWidth shouldBe (width - 2 - 12)
            text shouldBe "file 1"
            style shouldBe styles.normalItem
            focused shouldBe false
            padding shouldBe 0
        }
        
        assertComponent(fileSize, TextLine) {
          case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
            align shouldBe TextLine.Right
            pos shouldBe (1 + width - 2 - 12) -> (height - 3)
            resWidth shouldBe 12
            text shouldBe "123456"
            style shouldBe styles.normalItem
            focused shouldBe false
            padding shouldBe 0
        }
        
        assertComponent(folderSize, TextLine) {
          case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
            align shouldBe TextLine.Center
            pos shouldBe 1 -> (height - 1)
            resWidth shouldBe ((width - 2) / 2)
            text shouldBe "123 4567 890 (3)"
            style shouldBe styles.normalItem
            focused shouldBe false
            padding shouldBe 1
        }
        
        assertComponent(freeSpace, TextLine) {
          case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
            align shouldBe TextLine.Center
            pos shouldBe ((width - 2) / 2) -> (height - 1)
            resWidth shouldBe ((width - 2) / 2)
            text shouldBe "123 4567 890"
            style shouldBe styles.normalItem
            focused shouldBe false
            padding shouldBe 1
        }
    })
  }
}
