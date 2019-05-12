package scommons.farc.ui.filelist

import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.util.ShallowRendererUtils

class FileListColumnSpec extends TestSpec with ShallowRendererUtils {

  it should "render component" in {
    //given
    val props = FileListColumnProps(
      size = (5, 2),
      left = 3,
      boxStyle = new BlessedStyle {},
      itemStyle = new BlessedStyle {},
      items = List(
        10 -> "item 1",
        11 -> "item 2",
        12 -> "item 3"
      ),
      focusedPos = 1
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
    )(), { case List(comp1, comp2, comp3) =>
      comp1.key shouldBe "0"
      assertComponent(comp1, FileListItem) { case FileListItemProps(width, top, style, text, focused) =>
        width shouldBe props.size._1
        top shouldBe 0
        style shouldBe props.itemStyle
        text shouldBe "item 1"
        focused shouldBe false
      }
      comp2.key shouldBe "1"
      assertComponent(comp2, FileListItem) { case FileListItemProps(width, top, style, text, focused) =>
        width shouldBe props.size._1
        top shouldBe 1
        style shouldBe props.itemStyle
        text shouldBe "item 2"
        focused shouldBe true
      }
      comp3.key shouldBe "2"
      assertComponent(comp3, FileListItem) { case FileListItemProps(width, top, style, text, focused) =>
        width shouldBe props.size._1
        top shouldBe 2
        style shouldBe props.itemStyle
        text shouldBe "item 3"
        focused shouldBe false
      }
    })
  }
}
