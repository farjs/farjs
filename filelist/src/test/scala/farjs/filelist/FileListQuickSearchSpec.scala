package farjs.filelist

import farjs.filelist.FileListQuickSearch._
import farjs.ui.border.DoubleBorderProps
import farjs.ui.popup.PopupOverlay
import farjs.ui.theme.Theme
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class FileListQuickSearchSpec extends TestSpec with TestRendererUtils {

  FileListQuickSearch.doubleBorderComp = mockUiComponent("DoubleBorder")

  it should "call onClose when onClick" in {
    //given
    val onClose = mockFunction[Unit]
    val props = FileListQuickSearchProps("text", onClose)
    val omoveMock = mockFunction[Int, Int, Unit]
    val cursorShapeMock = mockFunction[String, Boolean, Boolean]
    val showCursorMock = mockFunction[Unit]
    val programMock = literal("omove" -> omoveMock, "showCursor" -> showCursorMock)
    val cursorMock = literal("shape" -> "underline", "blink" -> false)
    val screenMock = literal("program" -> programMock, "cursor" -> cursorMock,
      "cursorShape" -> cursorShapeMock)
    val textMock = literal("screen" -> screenMock)
    cursorShapeMock.expects("underline", true)
    showCursorMock.expects()
    textMock.aleft = 1
    textMock.atop = 3
    omoveMock.expects(5, 3)
    
    val comp = testRender(<(FileListQuickSearch())(^.wrapped := props)(), { el =>
      if (el.`type` == "text".asInstanceOf[js.Any]) textMock
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
    val omoveMock = mockFunction[Int, Int, Unit]
    val cursorShapeMock = mockFunction[String, Boolean, Boolean]
    val showCursorMock = mockFunction[Unit]
    val programMock = literal("omove" -> omoveMock, "showCursor" -> showCursorMock)
    val cursorMock = literal("shape" -> "underline", "blink" -> false)
    val screenMock = literal("program" -> programMock, "cursor" -> cursorMock,
      "cursorShape" -> cursorShapeMock)
    val textMock = literal("screen" -> screenMock)
    cursorShapeMock.expects("underline", true)
    showCursorMock.expects()
    textMock.aleft = 1
    textMock.atop = 3
    omoveMock.expects(5, 3)
    
    val renderer = createTestRenderer(<(FileListQuickSearch())(^.wrapped := props)(), { el =>
      if (el.`type` == "text".asInstanceOf[js.Any]) textMock
      else null
    })
    val formComp = inside(findComponents(renderer.root, <.form.name)) {
      case List(formComp) => formComp
    }

    //then
    textMock.aleft = 2
    textMock.atop = 3
    omoveMock.expects(6, 3)
    
    //when
    formComp.props.onResize()
  }
  
  it should "move cursor when update" in {
    //given
    val props = FileListQuickSearchProps("text", () => ())
    val omoveMock = mockFunction[Int, Int, Unit]
    val cursorShapeMock = mockFunction[String, Boolean, Boolean]
    val showCursorMock = mockFunction[Unit]
    val programMock = literal("omove" -> omoveMock, "showCursor" -> showCursorMock)
    val cursorMock = literal("shape" -> "underline", "blink" -> false)
    val screenMock = literal("program" -> programMock, "cursor" -> cursorMock,
      "cursorShape" -> cursorShapeMock)
    val textMock = literal("screen" -> screenMock)
    cursorShapeMock.expects("underline", true)
    showCursorMock.expects()
    textMock.aleft = 1
    textMock.atop = 3
    omoveMock.expects(5, 3)
    
    val renderer = createTestRenderer(<(FileListQuickSearch())(^.wrapped := props)(), { el =>
      if (el.`type` == "text".asInstanceOf[js.Any]) textMock
      else null
    })

    //then
    textMock.aleft = 1
    textMock.atop = 3
    omoveMock.expects(6, 3)
    
    //when
    TestRenderer.act { () =>
      renderer.update(<(FileListQuickSearch())(^.wrapped := props.copy(text = "text2"))())
    }
  }
  
  it should "hide cursor when unmount" in {
    //given
    val props = FileListQuickSearchProps("text", () => ())
    val omoveMock = mockFunction[Int, Int, Unit]
    val cursorShapeMock = mockFunction[String, Boolean, Boolean]
    val showCursorMock = mockFunction[Unit]
    val programMock = literal("omove" -> omoveMock, "showCursor" -> showCursorMock)
    val cursorMock = literal("shape" -> "underline", "blink" -> false)
    val screenMock = literal("program" -> programMock, "cursor" -> cursorMock,
      "cursorShape" -> cursorShapeMock)
    val textMock = literal("screen" -> screenMock)
    cursorShapeMock.expects("underline", true)
    showCursorMock.expects()
    textMock.aleft = 1
    textMock.atop = 3
    omoveMock.expects(5, 3)
    
    val renderer = createTestRenderer(<(FileListQuickSearch())(^.wrapped := props)(), { el =>
      if (el.`type` == "text".asInstanceOf[js.Any]) textMock
      else null
    })

    //then
    val hideCursorMock = mockFunction[Unit]
    programMock.hideCursor = hideCursorMock
    hideCursorMock.expects()
    
    //when
    TestRenderer.act { () =>
      renderer.unmount()
    }
  }
  
  it should "render component" in {
    //given
    val props = FileListQuickSearchProps("some quick search text", () => ())
    val omoveMock = mockFunction[Int, Int, Unit]
    val cursorShapeMock = mockFunction[String, Boolean, Boolean]
    val showCursorMock = mockFunction[Unit]
    val programMock = literal("omove" -> omoveMock, "showCursor" -> showCursorMock)
    val cursorMock = literal("shape" -> "underline", "blink" -> false)
    val screenMock = literal("program" -> programMock, "cursor" -> cursorMock,
      "cursorShape" -> cursorShapeMock)
    val textMock = literal("screen" -> screenMock)
    cursorShapeMock.expects("underline", true)
    showCursorMock.expects()
    textMock.aleft = 1
    textMock.atop = 3
    omoveMock.expects(23, 3)

    //when
    val result = testRender(<(FileListQuickSearch())(^.wrapped := props)(), { el =>
      if (el.`type` == "text".asInstanceOf[js.Any]) textMock
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
      ^.rbStyle := PopupOverlay.style
    )(), inside(_) { case List(box) =>
      assertNativeComponent(box, <.box(
        ^.rbClickable := true,
        ^.rbAutoFocus := false,
        ^.rbWidth := width,
        ^.rbHeight := height,
        ^.rbTop := "100%-3",
        ^.rbLeft := 10,
        ^.rbStyle := boxStyle
      )(), inside(_) { case List(border, text) =>
        assertTestComponent(border, doubleBorderComp, plain = true) {
          case DoubleBorderProps(resWidth, resHeight, style, resLeft, resTop, title) =>
            resWidth shouldBe width
            resHeight shouldBe height
            style shouldBe boxStyle
            resLeft shouldBe js.undefined
            resTop shouldBe js.undefined
            title shouldBe "Search"
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
