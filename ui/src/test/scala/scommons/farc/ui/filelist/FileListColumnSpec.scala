package scommons.farc.ui.filelist

import org.scalatest.Assertion
import scommons.farc.ui._
import scommons.farc.ui.border.SingleBorder
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

class FileListColumnSpec extends TestSpec
  with ShallowRendererUtils
  with TestRendererUtils {

  it should "not re-render component if the same props" in {
    //given
    val props = FileListColumnProps(
      size = (14, 3),
      left = 2,
      borderCh = SingleBorder.verticalCh,
      items = List(
        10 -> "item 1",
        11 -> "item 2",
        12 -> "item 3"
      ),
      focusedIndex = 1,
      selectedIds = Set(11, 12)
    )
    val renderer = createTestRenderer(<(FileListColumn())(^.wrapped := props)(
      <.text(^.content := "initial")()
    ))
    val testEl = renderer.root.children.head.children.head.children(2)
    testEl.`type` shouldBe "text"
    testEl.props.content shouldBe "initial"

    //when
    renderer.update(<(FileListColumn())(^.wrapped := props.copy(selectedIds = Set(12, 11)))(
      <.text(^.content := "update")()
    ))

    //then
    val sameEl = renderer.root.children.head.children.head.children(2)
    sameEl.`type` shouldBe "text"
    sameEl.props.content shouldBe "initial"
    
    //cleanup
    renderer.unmount()
  }
  
  it should "re-render component if different props" in {
    //given
    val props = FileListColumnProps(
      size = (14, 3),
      left = 2,
      borderCh = SingleBorder.verticalCh,
      items = List(
        10 -> "item 1",
        11 -> "item 2",
        12 -> "item 3"
      ),
      focusedIndex = 1,
      selectedIds = Set(11, 12)
    )
    val renderer = createTestRenderer(<(FileListColumn())(^.wrapped := props)(
      <.text(^.content := "initial")()
    ))
    val testEl = renderer.root.children.head.children.head.children(2)
    testEl.`type` shouldBe "text"
    testEl.props.content shouldBe "initial"

    //when
    renderer.update(<(FileListColumn())(^.wrapped := props.copy(selectedIds = Set(12)))(
      <.text(^.content := "update")()
    ))

    //then
    val updatedEl = renderer.root.children.head.children.head.children(2)
    updatedEl.`type` shouldBe "text"
    updatedEl.props.content shouldBe "update"

    //cleanup
    renderer.unmount()
  }
  
  it should "render non-empty component" in {
    //given
    val props = FileListColumnProps(
      size = (14, 3),
      left = 2,
      borderCh = SingleBorder.verticalCh,
      items = List(
        10 -> "item 1 {bold}",
        11 -> "item 2 looooooong",
        12 -> "item 3"
      ),
      focusedIndex = 1,
      selectedIds = Set(11, 12)
    )
    val comp = <(FileListColumn())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileListColumn(result, props, Some(
      """{white-fg}{blue-bg}item 1 {open}bold{close} {/}{white-fg}{blue-bg}│{/}
        |{bold}{yellow-fg}{cyan-bg}item 2 loooooo{/}{red-fg}{blue-bg}{close}{/}
        |{bold}{yellow-fg}{blue-bg}item 3        {/}{white-fg}{blue-bg}│{/}""".stripMargin
    ))
  }
  
  it should "render empty component" in {
    //given
    val props = FileListColumnProps(
      size = (6, 3),
      left = 2,
      borderCh = SingleBorder.verticalCh,
      items = Nil,
      focusedIndex = -1,
      selectedIds = Set.empty
    )
    val comp = <(FileListColumn())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileListColumn(result, props, None)
  }
  
  private def assertFileListColumn(result: ShallowInstance,
                                   props: FileListColumnProps,
                                   expectedContent: Option[String]): Unit = {
    
    def assertElements(header: ShallowInstance, itemsText: Option[ShallowInstance]): Assertion = {
      assertComponent(header, TextLine) {
        case TextLineProps(align, pos, width, text, style, focused, padding) =>
          align shouldBe TextLine.Center
          pos shouldBe 0 -> 0
          width shouldBe props.size._1
          text shouldBe "Name"
          style shouldBe FileListView.styles.headerStyle
          focused shouldBe false
          padding shouldBe 0
      }

      expectedContent.foreach { content =>
        val Some(textEl) = itemsText
        
        assertNativeComponent(textEl, <.text(
          ^.rbWidth := props.size._1 + 1,
          ^.rbTop := 1,
          ^.rbTags := true,
          ^.content := content
        )())
      }
      expectedContent.size shouldBe itemsText.size
    }
    
    assertNativeComponent(result, <.box(
      ^.rbWidth := props.size._1,
      ^.rbHeight := props.size._2,
      ^.rbLeft := props.left,
      ^.rbStyle := FileListView.styles.normalItem
    )(), { children: List[ShallowInstance] =>
      children match {
        case List(header, itemsText, _) => assertElements(header, Some(itemsText))
        case List(header, _) => assertElements(header, None)
      }
    })
  }
}
