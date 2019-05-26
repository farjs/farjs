package scommons.farc.ui.filelist

import scommons.farc.ui._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.util.ShallowRendererUtils

class FileListColumnSpec extends TestSpec with ShallowRendererUtils {

  it should "render component" in {
    //given
    val props = FileListColumnProps(
      size = (5, 3),
      left = 3,
      boxStyle = new BlessedStyle {},
      itemStyle = new BlessedStyle {},
      items = List(
        10 -> "item 1",
        11 -> "item 2",
        12 -> "item 3"
      ),
      focusedPos = 1,
      selectedIds = Set(11, 12)
    )
    val comp = <(FileListColumn())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertNativeComponent(result, <.box(
      ^.rbWidth := props.size._1,
      ^.rbHeight := props.size._2,
      ^.rbLeft := props.left,
      ^.rbStyle := props.boxStyle
    )(), { case List(header, item1, item2, item3) =>
      header.key shouldBe null
      assertComponent(header, TextLine) {
        case TextLineProps(align, pos, width, text, style, focused, padding) =>
          align shouldBe TextLine.Center
          pos shouldBe 0 -> 0
          width shouldBe props.size._1
          text shouldBe "Name"
          style shouldBe FileListColumn.headerStyle
          focused shouldBe false
          padding shouldBe 0
      }
      
      item1.key shouldBe "0"
      assertComponent(item1, FileListItem) {
        case FileListItemProps(width, top, style, text, focused) =>
          width shouldBe props.size._1
          top shouldBe 1
          style shouldBe props.itemStyle
          text shouldBe "item 1"
          focused shouldBe false
      }
      item2.key shouldBe "1"
      assertComponent(item2, FileListItem) {
        case FileListItemProps(width, top, style, text, focused) =>
          width shouldBe props.size._1
          top shouldBe 2
          text shouldBe "item 2"
          style shouldBe FileListColumn.selectedItem
          focused shouldBe true
      }
      item3.key shouldBe "2"
      assertComponent(item3, FileListItem) {
        case FileListItemProps(width, top, style, text, focused) =>
          width shouldBe props.size._1
          top shouldBe 3
          style shouldBe FileListColumn.selectedItem
          text shouldBe "item 3"
          focused shouldBe false
      }
    })
  }
}
