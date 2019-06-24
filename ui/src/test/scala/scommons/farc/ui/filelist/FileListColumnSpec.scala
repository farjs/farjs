package scommons.farc.ui.filelist

import org.scalatest.Assertion
import scommons.farc.api.filelist.FileListItem
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
        FileListItem("item 1"),
        FileListItem("item 2"),
        FileListItem("item 3")
      ),
      focusedIndex = 1,
      selectedNames = Set("item 2", "item 3")
    )
    val renderer = createTestRenderer(<(FileListColumn())(^.wrapped := props)(
      <.text(^.content := "initial")()
    ))
    val testEl = renderer.root.children.head.children.head.children(2)
    testEl.`type` shouldBe "text"
    testEl.props.content shouldBe "initial"

    //when
    renderer.update(<(FileListColumn())(^.wrapped := props.copy(selectedNames = Set("item 3", "item 2")))(
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
        FileListItem("item 1"),
        FileListItem("item 2"),
        FileListItem("item 3")
      ),
      focusedIndex = 1,
      selectedNames = Set("item 2", "item 3")
    )
    val renderer = createTestRenderer(<(FileListColumn())(^.wrapped := props)(
      <.text(^.content := "initial")()
    ))
    val testEl = renderer.root.children.head.children.head.children(2)
    testEl.`type` shouldBe "text"
    testEl.props.content shouldBe "initial"

    //when
    renderer.update(<(FileListColumn())(^.wrapped := props.copy(selectedNames = Set("item 3")))(
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
        FileListItem.up,
        FileListItem("dir 1 {bold}", isDir = true),
        FileListItem(".dir 2 looooooong", isDir = true),
        FileListItem("file 3"),
        FileListItem(".dir 4", isDir = true),
        FileListItem(".file 5\n"),
        FileListItem("file 6")
      ),
      focusedIndex = 2,
      selectedNames = Set(".dir 2 looooooong", "file 3")
    )
    val comp = <(FileListColumn())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileListColumn(result, props, Some(
      """{white-fg}{blue-bg}..            {/}{white-fg}{blue-bg}│{/}
        |{bold}{white-fg}{blue-bg}dir 1 {open}bold{close}  {/}{white-fg}{blue-bg}│{/}
        |{bold}{yellow-fg}{cyan-bg}.dir 2 loooooo{/}{red-fg}{blue-bg}{close}{/}
        |{bold}{yellow-fg}{blue-bg}file 3        {/}{white-fg}{blue-bg}│{/}
        |{bold}{black-fg}{blue-bg}.dir 4        {/}{white-fg}{blue-bg}│{/}
        |{bold}{black-fg}{blue-bg}.file 5       {/}{white-fg}{blue-bg}│{/}
        |{white-fg}{blue-bg}file 6        {/}{white-fg}{blue-bg}│{/}""".stripMargin
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
      selectedNames = Set.empty
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
