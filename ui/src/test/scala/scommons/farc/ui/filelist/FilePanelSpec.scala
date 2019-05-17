package scommons.farc.ui.filelist

import scommons.farc.ui._
import scommons.farc.ui.border._
import scommons.farc.ui.filelist.FilePanel._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.util.ShallowRendererUtils

class FilePanelSpec extends TestSpec with ShallowRendererUtils {

  it should "render component" in {
    //given
    val props = FilePanelProps(size = (25, 15))
    val comp = <(FilePanel())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertNativeComponent(result, <.box(^.rbStyle := textStyle)(), {
      case List(border, line, list, currFolder,  currFile, fileSize, folderSize, freeSpace) =>
        val (width, height) = props.size
        assertComponent(border, DoubleBorder) { case DoubleBorderProps(resSize, style) =>
          resSize shouldBe width -> height
          style shouldBe borderStyle
        }
        assertComponent(line, HorizontalLine) {
          case HorizontalLineProps(pos, len, lineCh, style, startCh, endCh) =>
            pos shouldBe 0 -> (height - 4)
            len shouldBe width
            lineCh shouldBe SingleBorder.horizontalCh
            style shouldBe borderStyle
            startCh shouldBe Some(DoubleBorder.leftSingleCh)
            endCh shouldBe Some(DoubleBorder.rightSingleCh)
        }
        
        assertComponent(list, FileList) { case FileListProps(resSize, columns, items) =>
          resSize shouldBe (width - 2) -> (height - 5)
          columns shouldBe 3
          items should not be empty
        }
        currFolder.key shouldBe "currFolder"
        assertComponent(currFolder, TextLine) {
          case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
            align shouldBe TextLine.Center
            pos shouldBe 1 -> 0
            resWidth shouldBe (width - 2)
            text shouldBe "/current/folder"
            style shouldBe textStyle
            focused shouldBe true
            padding shouldBe 1
        }
        currFile.key shouldBe "currFile"
        assertComponent(currFile, TextLine) {
          case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
            align shouldBe TextLine.Left
            pos shouldBe 1 -> (height - 3)
            resWidth shouldBe (width - 2 - 12)
            text shouldBe "current.file"
            style shouldBe textStyle
            focused shouldBe false
            padding shouldBe 0
        }
        fileSize.key shouldBe "fileSize"
        assertComponent(fileSize, TextLine) {
          case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
            align shouldBe TextLine.Right
            pos shouldBe (1 + width - 2 - 12) -> (height - 3)
            resWidth shouldBe 12
            text shouldBe "123456"
            style shouldBe textStyle
            focused shouldBe false
            padding shouldBe 0
        }
        folderSize.key shouldBe "folderSize"
        assertComponent(folderSize, TextLine) {
          case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
            align shouldBe TextLine.Center
            pos shouldBe 1 -> (height - 1)
            resWidth shouldBe ((width - 2) / 2)
            text shouldBe "123 4567 890 (3)"
            style shouldBe textStyle
            focused shouldBe false
            padding shouldBe 1
        }
        freeSpace.key shouldBe "freeSpace"
        assertComponent(freeSpace, TextLine) {
          case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
            align shouldBe TextLine.Center
            pos shouldBe ((width - 2) / 2) -> (height - 1)
            resWidth shouldBe ((width - 2) / 2)
            text shouldBe "123 4567 890"
            style shouldBe textStyle
            focused shouldBe false
            padding shouldBe 1
        }
    })
  }
}
