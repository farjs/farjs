package farjs.ui

import farjs.ui.TextBoxSpec._
import scommons.react.blessed._
import scommons.react.blessed.raw._
import scommons.react.test.TestSpec
import scommons.react.test.raw.{ShallowInstance, TestRenderer}
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

class TextBoxSpec extends TestSpec
  with ShallowRendererUtils
  with TestRendererUtils {

  it should "move cursor and grab focus when onClick" in {
    //given
    val props = getTextBoxProps()
    val programMock = mock[BlessedProgramMock]
    val screenMock = mock[BlessedScreenMock]
    val inputMock = mock[BlessedElementMock]
    val root = createTestRenderer(<(TextBox())(^.wrapped := props)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    }).root

    //then
    (inputMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (inputMock.aleft _).expects().returning(1)
    (programMock.omove _).expects(2, 3)
    (screenMock.focused _).expects().returning(null)
    (inputMock.focus _).expects()

    //when
    root.children(0).props.onClick(js.Dynamic.literal(x = 2, y = 3))
  }

  it should "keep cursor position when onResize" in {
    //given
    val props = getTextBoxProps(value = "test")
    val programMock = mock[BlessedProgramMock]
    val screenMock = mock[BlessedScreenMock]
    val inputMock = mock[BlessedElementMock]
    val root = createTestRenderer(<(TextBox())(^.wrapped := props)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    }).root

    //then
    (inputMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (screenMock.focused _).expects().returning(inputMock.asInstanceOf[BlessedElement])
    (inputMock.aleft _).expects().returning(1)
    (inputMock.atop _).expects().returning(2)
    (programMock.omove _).expects(5, 2)

    //when
    root.children(0).props.onResize()
  }

  it should "show cursor when onFocus" in {
    //given
    val props = getTextBoxProps(value = "test")
    val programMock = mock[BlessedProgramMock]
    val screenMock = mock[BlessedScreenMock]
    val cursorMock = mock[BlessedCursorMock]
    val inputMock = mock[BlessedElementMock]
    val root = createTestRenderer(<(TextBox())(^.wrapped := props)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    }).root

    //then
    (inputMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.cursor _).expects().returning(cursorMock.asInstanceOf[BlessedCursor])
    (cursorMock.shape _).expects().returning("underline")
    (cursorMock.blink _).expects().returning(false)
    (screenMock.cursorShape _).expects("underline", true)
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (inputMock.aleft _).expects().returning(1)
    (inputMock.atop _).expects().returning(2)
    (programMock.omove _).expects(5, 2)
    (programMock.showCursor _).expects()

    //when
    root.children(0).props.onFocus()
  }

  it should "hide cursor when onBlur" in {
    //given
    val props = getTextBoxProps()
    val programMock = mock[BlessedProgramMock]
    val screenMock = mock[BlessedScreenMock]
    val inputMock = mock[BlessedElementMock]
    val root = createTestRenderer(<(TextBox())(^.wrapped := props)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    }).root

    //then
    (inputMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (programMock.hideCursor _).expects()

    //when
    root.children(0).props.onBlur()
  }

  it should "process key and prevent default when onKeypress" in {
    //given
    val onChange = mockFunction[String, Unit]
    var value = "initial name"
    val props = getTextBoxProps(value, onChange)
    val programMock = mock[BlessedProgramMock]
    val screenMock = mock[BlessedScreenMock]
    val inputMock = mock[BlessedElementMock]
    val renderer = createTestRenderer(<(TextBox())(^.wrapped := props)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    })
    val aleft = 1
    val atop = 2
    var cursorX = value.length
    
    def check(defaultPrevented: Boolean, fullKey: String, posX: Int, newVal: String, ch: String = null): Unit = {
      //given
      val key = js.Dynamic.literal("full" -> fullKey).asInstanceOf[KeyboardKey]
      
      (inputMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
      (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
      (inputMock.aleft _).expects().returning(aleft)
      (inputMock.atop _).expects().returning(atop)
      
      if (cursorX != posX) {
        cursorX = posX
        (programMock.omove _).expects(aleft + cursorX, atop)
      }

      if (value != newVal) {
        onChange.expects(newVal)
      }
      
      //when
      renderer.root.children(0).props.onKeypress(ch, key)
      
      //then
      key.defaultPrevented.getOrElse(false) shouldBe defaultPrevented

      if (value != newVal) {
        value = newVal

        TestRenderer.act { () =>
          renderer.update(<(TextBox())(^.wrapped := props.copy(value = newVal))())
        }
      }
    }

    //when & then
    check(defaultPrevented = false, "escape", cursorX, value)
    check(defaultPrevented = false, "return", cursorX, value)
    check(defaultPrevented = false, "enter", cursorX, value)
    check(defaultPrevented = false, "tab", cursorX, value)
    
    //when & then
    check(defaultPrevented = true, "right", cursorX, value)
    check(defaultPrevented = true, "left", cursorX - 1, value)
    check(defaultPrevented = true, "left", cursorX - 1, value)
    check(defaultPrevented = true, "home", 0, value)
    check(defaultPrevented = true, "left", 0, value)
    check(defaultPrevented = true, "right", 1, value)
    check(defaultPrevented = true, "right", 2, value)
    check(defaultPrevented = true, "end", value.length, value)
    
    //when & then
    check(defaultPrevented = true, "delete", cursorX, value)
    check(defaultPrevented = true, "backspace", cursorX - 1, "initial nam")
    check(defaultPrevented = true, "", cursorX + 1, "initial nam1", "1")
    check(defaultPrevented = true, "", cursorX + 1, "initial nam12", "2")
    check(defaultPrevented = true, "left", cursorX - 1, value)
    check(defaultPrevented = true, "left", cursorX - 1, value)
    check(defaultPrevented = true, "left", cursorX - 1, value)
    check(defaultPrevented = true, "", cursorX + 1, "initial na3m12", "3")
    check(defaultPrevented = true, "", cursorX + 1, "initial na34m12", "4")
    check(defaultPrevented = true, "backspace", cursorX - 1, "initial na3m12")
    check(defaultPrevented = true, "backspace", cursorX - 1, "initial nam12")
    check(defaultPrevented = true, "delete", cursorX, "initial na12")
    check(defaultPrevented = true, "delete", cursorX, "initial na2")
    check(defaultPrevented = true, "home", 0, value)
    check(defaultPrevented = true, "backspace", 0, value)
    check(defaultPrevented = true, "delete", 0, "nitial na2")
    check(defaultPrevented = true, "delete", 0, "itial na2")
    
    //when & then
    check(defaultPrevented = false, "up", cursorX, value)
    check(defaultPrevented = false, "down", cursorX, value)
  }

  it should "render component" in {
    //given
    val props = getTextBoxProps()

    //when
    val result = shallowRender(<(TextBox())(^.wrapped := props)())

    //then
    assertTextBox(result, props)
  }

  private def getTextBoxProps(value: String = "initial name",
                              onChange: String => Unit = _ => ()
                             ): TextBoxProps = TextBoxProps(
    pos = (1, 2),
    width = 10,
    value = value,
    style = new BlessedStyle {
      override val bg = "cyan"
      override val fg = "black"
    },
    onChange = onChange
  )

  private def assertTextBox(result: ShallowInstance, props: TextBoxProps): Unit = {
    val (left, top) = props.pos
    
    assertNativeComponent(result,
      <.input(
        ^.rbAutoFocus := false,
        ^.rbClickable := true,
        ^.rbKeyable := true,
        ^.rbWidth := props.width,
        ^.rbHeight := 1,
        ^.rbLeft := left,
        ^.rbTop := top,
        ^.rbStyle := props.style,
        ^.content := props.value
      )()
    )
  }
}

object TextBoxSpec {

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

    def focused: BlessedElement

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
    def focus(): Unit
  }
}
