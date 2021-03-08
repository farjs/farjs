package farjs.filelist

import farjs.filelist.FileListQuickSearch._
import farjs.filelist.FileListQuickSearchSpec._
import farjs.ui.border.DoubleBorderProps
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.raw.{BlessedCursor, BlessedProgram}
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

class FileListQuickSearchSpec extends TestSpec with TestRendererUtils {

  FileListQuickSearch.doubleBorderComp = () => "DoubleBorder".asInstanceOf[ReactClass]

  it should "call onClose when onClick" in {
    //given
    val onClose = mockFunction[Unit]
    val props = FileListQuickSearchProps("text", onClose)
    val programMock = mock[BlessedProgramMock]
    val screenMock = mock[BlessedScreenMock]
    val cursorMock = mock[BlessedCursorMock]
    val textMock = mock[BlessedElementMock]
    (textMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen]).twice()
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram]).twice()
    (screenMock.cursor _).expects().returning(cursorMock.asInstanceOf[BlessedCursor])
    (cursorMock.shape _).expects().returning("underline")
    (cursorMock.blink _).expects().returning(false)
    (screenMock.cursorShape _).expects("underline", true)
    (programMock.showCursor _).expects()
    (textMock.aleft _).expects().returning(1)
    (textMock.atop _).expects().returning(3)
    (programMock.omove _).expects(5, 3)
    
    val comp = testRender(<(FileListQuickSearch())(^.wrapped := props)(), { el =>
      if (el.`type` == "text".asInstanceOf[js.Any]) textMock.asInstanceOf[js.Any]
      else null
    })
    
    //then
    onClose.expects()

    //when
    comp.props.onClick()
  }
  
  it should "move cursor when onResize" in {
    //given
    val props = FileListQuickSearchProps("text", () => ())
    val programMock = mock[BlessedProgramMock]
    val screenMock = mock[BlessedScreenMock]
    val cursorMock = mock[BlessedCursorMock]
    val textMock = mock[BlessedElementMock]
    (textMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen]).twice()
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram]).twice()
    (screenMock.cursor _).expects().returning(cursorMock.asInstanceOf[BlessedCursor])
    (cursorMock.shape _).expects().returning("underline")
    (cursorMock.blink _).expects().returning(false)
    (screenMock.cursorShape _).expects("underline", true)
    (programMock.showCursor _).expects()
    (textMock.aleft _).expects().returning(1)
    (textMock.atop _).expects().returning(3)
    (programMock.omove _).expects(5, 3)
    
    val renderer = createTestRenderer(<(FileListQuickSearch())(^.wrapped := props)(), { el =>
      if (el.`type` == "text".asInstanceOf[js.Any]) textMock.asInstanceOf[js.Any]
      else null
    })
    val List(formComp) = findComponents(renderer.root, <.form.name)

    //then
    (textMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (textMock.aleft _).expects().returning(2)
    (textMock.atop _).expects().returning(3)
    (programMock.omove _).expects(6, 3)
    
    //when
    formComp.props.onResize()
  }
  
  it should "move cursor when update" in {
    //given
    val props = FileListQuickSearchProps("text", () => ())
    val programMock = mock[BlessedProgramMock]
    val screenMock = mock[BlessedScreenMock]
    val cursorMock = mock[BlessedCursorMock]
    val textMock = mock[BlessedElementMock]
    (textMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen]).twice()
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram]).twice()
    (screenMock.cursor _).expects().returning(cursorMock.asInstanceOf[BlessedCursor])
    (cursorMock.shape _).expects().returning("underline")
    (cursorMock.blink _).expects().returning(false)
    (screenMock.cursorShape _).expects("underline", true)
    (programMock.showCursor _).expects()
    (textMock.aleft _).expects().returning(1)
    (textMock.atop _).expects().returning(3)
    (programMock.omove _).expects(5, 3)
    
    val renderer = createTestRenderer(<(FileListQuickSearch())(^.wrapped := props)(), { el =>
      if (el.`type` == "text".asInstanceOf[js.Any]) textMock.asInstanceOf[js.Any]
      else null
    })

    //then
    (textMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (textMock.aleft _).expects().returning(1)
    (textMock.atop _).expects().returning(3)
    (programMock.omove _).expects(6, 3)
    
    //when
    TestRenderer.act { () =>
      renderer.update(<(FileListQuickSearch())(^.wrapped := props.copy(text = "text2"))())
    }
  }
  
  it should "hide cursor when unmount" in {
    //given
    val props = FileListQuickSearchProps("text", () => ())
    val programMock = mock[BlessedProgramMock]
    val screenMock = mock[BlessedScreenMock]
    val cursorMock = mock[BlessedCursorMock]
    val textMock = mock[BlessedElementMock]
    (textMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen]).twice()
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram]).twice()
    (screenMock.cursor _).expects().returning(cursorMock.asInstanceOf[BlessedCursor])
    (cursorMock.shape _).expects().returning("underline")
    (cursorMock.blink _).expects().returning(false)
    (screenMock.cursorShape _).expects("underline", true)
    (programMock.showCursor _).expects()
    (textMock.aleft _).expects().returning(1)
    (textMock.atop _).expects().returning(3)
    (programMock.omove _).expects(5, 3)
    
    val renderer = createTestRenderer(<(FileListQuickSearch())(^.wrapped := props)(), { el =>
      if (el.`type` == "text".asInstanceOf[js.Any]) textMock.asInstanceOf[js.Any]
      else null
    })

    //then
    (programMock.hideCursor _).expects()
    
    //when
    TestRenderer.act { () =>
      renderer.unmount()
    }
  }
  
  it should "render component" in {
    //given
    val props = FileListQuickSearchProps("some quick search text", () => ())
    val programMock = mock[BlessedProgramMock]
    val screenMock = mock[BlessedScreenMock]
    val cursorMock = mock[BlessedCursorMock]
    val textMock = mock[BlessedElementMock]
    (textMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen]).twice()
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram]).twice()
    (screenMock.cursor _).expects().returning(cursorMock.asInstanceOf[BlessedCursor])
    (cursorMock.shape _).expects().returning("underline")
    (cursorMock.blink _).expects().returning(false)
    (screenMock.cursorShape _).expects("underline", true)
    (programMock.showCursor _).expects()
    (textMock.aleft _).expects().returning(1)
    (textMock.atop _).expects().returning(3)
    (programMock.omove _).expects(23, 3)

    //when
    val result = testRender(<(FileListQuickSearch())(^.wrapped := props)(), { el =>
      if (el.`type` == "text".asInstanceOf[js.Any]) textMock.asInstanceOf[js.Any]
      else null
    })

    //then
    assertFileListQuickSearch(result, props)
  }
  
  private def assertFileListQuickSearch(result: TestInstance,
                                        props: FileListQuickSearchProps): Unit = {
    val width = 25
    val height = 3
    val boxStyle = Theme.current.popup.regular
    val textStyle = Theme.current.textBox.regular
    val textWidth = width - 2
    
    assertNativeComponent(result, <.form(
      ^.rbClickable := true,
      ^.rbMouse := true,
      ^.rbAutoFocus := false,
      ^.rbStyle := overlayStyle
    )(), { case List(box) =>
      assertNativeComponent(box, <.box(
        ^.rbClickable := true,
        ^.rbAutoFocus := false,
        ^.rbWidth := width,
        ^.rbHeight := height,
        ^.rbTop := "100%-3",
        ^.rbLeft := 10,
        ^.rbStyle := boxStyle
      )(), { case List(border, text) =>
        assertTestComponent(border, doubleBorderComp) {
          case DoubleBorderProps(resSize, style, pos, title) =>
            resSize shouldBe (width -> height)
            style shouldBe boxStyle
            pos shouldBe (0 -> 0)
            title shouldBe Some("Search")
        }

        assertNativeComponent(text,
          <.text(
            ^.rbWidth := textWidth,
            ^.rbHeight := 1,
            ^.rbTop := 1,
            ^.rbLeft := 1,
            ^.rbStyle := textStyle,
            ^.content := props.text.take(textWidth)
          )()
        )
      })
    })
  }
}

object FileListQuickSearchSpec {

  @JSExportAll
  trait BlessedProgramMock {

    def showCursor(): Unit
    def hideCursor(): Unit

    def omove(x: Int, y: Int): Unit
  }

  @JSExportAll
  trait BlessedScreenMock {

    def program: BlessedProgram
    def cursor: BlessedCursor

    def cursorShape(shape: String, blink: Boolean): Boolean
  }

  @JSExportAll
  trait BlessedCursorMock {

    def shape: String
    def blink: Boolean
  }

  @JSExportAll
  trait BlessedElementMock {

    def aleft: Int
    def atop: Int

    def screen: BlessedScreen
  }
}
