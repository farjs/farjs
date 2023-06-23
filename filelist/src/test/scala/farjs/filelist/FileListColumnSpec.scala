package farjs.filelist

import farjs.filelist.FileListColumn._
import farjs.filelist.api.FileListItem
import farjs.filelist.theme.FileListTheme
import farjs.filelist.theme.FileListThemeSpec.withThemeContext
import farjs.ui._
import farjs.ui.border._
import org.scalatest.Assertion
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class FileListColumnSpec extends TestSpec with TestRendererUtils {

  FileListColumn.textLineComp = mockUiComponent("TextLine")

  it should "not re-render component if the same props" in {
    //given
    val props = FileListColumnProps(
      size = (14, 3),
      left = 2,
      borderCh = SingleChars.vertical,
      items = List(
        FileListItem("item 1"),
        FileListItem("item 2"),
        FileListItem("item 3")
      ),
      focusedIndex = 1,
      selectedNames = Set("item 2", "item 3")
    )
    val renderer = createTestRenderer(withThemeContext(<(FileListColumn())(^.wrapped := props)(
      <.text(^.content := "initial")()
    )))
    val testEl = renderer.root.children.head.children.head.children(2)
    testEl.`type` shouldBe "text"
    testEl.props.content shouldBe "initial"

    //when
    renderer.update(withThemeContext(
      <(FileListColumn())(^.wrapped := props.copy(selectedNames = Set("item 3", "item 2")))(
        <.text(^.content := "update")()
      )
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
      borderCh = SingleChars.vertical,
      items = List(
        FileListItem("item 1"),
        FileListItem("item 2"),
        FileListItem("item 3")
      ),
      focusedIndex = 1,
      selectedNames = Set("item 2", "item 3")
    )
    val renderer = createTestRenderer(withThemeContext(<(FileListColumn())(^.wrapped := props)(
      <.text(^.content := "initial")()
    )))
    val testEl = renderer.root.children.head.children.head.children(2)
    testEl.`type` shouldBe "text"
    testEl.props.content shouldBe "initial"

    //when
    renderer.update(withThemeContext(
      <(FileListColumn())(^.wrapped := props.copy(selectedNames = Set("item 3")))(
        <.text(^.content := "update")()
      )
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
    val currTheme = FileListTheme.xterm256Theme
    val props = FileListColumnProps(
      size = (14, 3),
      left = 2,
      borderCh = SingleChars.vertical,
      items = List(
        FileListItem.up,
        FileListItem("dir\t1 {bold}", isDir = true),
        FileListItem(".dir 2 looooooong", isDir = true),
        FileListItem("file 3"),
        FileListItem(".dir \r4", isDir = true),
        FileListItem(".file \n5"),
        FileListItem(" fileй 6"),
        FileListItem("file.zip")
      ),
      focusedIndex = 2,
      selectedNames = Set(".dir 2 looooooong", "file 3")
    )

    //when
    val result = testRender(withThemeContext(<(FileListColumn())(^.wrapped := props)(), currTheme))

    //then
    assertFileListColumn(result, props, Some(
      List(
        "{bold}{#5ce-fg}{#008-bg}..            {/}{bold}{#5ce-fg}{#008-bg}│{/}",
        "{bold}{#fff-fg}{#008-bg}dir 1 {open}bold{close}  {/}{bold}{#5ce-fg}{#008-bg}│{/}",
        "{bold}{yellow-fg}{#088-bg}.dir 2 loooooo{/}{bold}{#5ce-fg}{#008-bg}{close}{/}",
        "{bold}{yellow-fg}{#008-bg}file 3        {/}{bold}{#5ce-fg}{#008-bg}│{/}",
        "{bold}{#055-fg}{#008-bg}.dir 4        {/}{bold}{#5ce-fg}{#008-bg}│{/}",
        "{bold}{#055-fg}{#008-bg}.file 5       {/}{bold}{#5ce-fg}{#008-bg}│{/}",
        "{bold}{#5ce-fg}{#008-bg} fileй 6      {/}{bold}{#5ce-fg}{#008-bg}│{/}",
        "{bold}{#a05-fg}{#008-bg}file.zip      {/}{bold}{#5ce-fg}{#008-bg}│{/}"
      ).mkString("\n")
    ), currTheme)
  }
  
  it should "render empty component" in {
    //given
    val props = FileListColumnProps(
      size = (6, 3),
      left = 2,
      borderCh = SingleChars.vertical,
      items = Nil,
      focusedIndex = -1,
      selectedNames = Set.empty
    )

    //when
    val result = testRender(withThemeContext(<(FileListColumn())(^.wrapped := props)()))

    //then
    assertFileListColumn(result, props, None)
  }
  
  private def assertFileListColumn(result: TestInstance,
                                   props: FileListColumnProps,
                                   expectedContent: Option[String],
                                   currTheme: FileListTheme = FileListTheme.defaultTheme): Unit = {
    
    val theme = currTheme.fileList
    
    def assertElements(header: TestInstance, itemsText: Option[TestInstance]): Assertion = {
      assertTestComponent(header, textLineComp, plain = true) {
        case TextLineProps(align, left, top, width, text, style, focused, padding) =>
          align shouldBe TextAlign.center
          left shouldBe 0
          top shouldBe 0
          width shouldBe props.size._1
          text shouldBe "Name"
          style shouldBe theme.header
          focused shouldBe js.undefined
          padding shouldBe 0
      }

      expectedContent.foreach { content =>
        val textEl = inside(itemsText) {
          case Some(textEl) => textEl
        }
        
        assertNativeComponent(textEl, <.text(
          ^.rbWidth := props.size._1 + 1,
          ^.rbTop := 1,
          ^.rbTags := true,
          ^.rbWrap := false,
          ^.content := content
        )())
      }
      expectedContent.size shouldBe itemsText.size
    }
    
    assertNativeComponent(result.children.head, <.box(
      ^.rbWidth := props.size._1,
      ^.rbHeight := props.size._2,
      ^.rbLeft := props.left,
      ^.rbStyle := theme.regularItem
    )(), inside(_) {
      case List(header, itemsText) => assertElements(header, Some(itemsText))
      case List(header) => assertElements(header, None)
    })
  }
}
