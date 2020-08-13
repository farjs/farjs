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
    
    (inputMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (inputMock.width _).expects().twice().returning(10)
    (inputMock.aleft _).expects().returning(1)
    (inputMock.atop _).expects().returning(3)
    (programMock.omove _).expects(10, 3)

    val root = createTestRenderer(<(TextBox())(^.wrapped := props)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    }).root

    //then
    (inputMock.screen _).expects().twice().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (inputMock.width _).expects().returning(10)
    (inputMock.aleft _).expects().twice().returning(1)
    (inputMock.atop _).expects().returning(3)
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

    (inputMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (inputMock.width _).expects().twice().returning(10)
    (inputMock.aleft _).expects().returning(1)
    (inputMock.atop _).expects().returning(2)
    (programMock.omove _).expects(5, 2)
    
    val root = createTestRenderer(<(TextBox())(^.wrapped := props)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    }).root

    //then
    (inputMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (inputMock.aleft _).expects().returning(1)
    (inputMock.atop _).expects().returning(2)
    (programMock.omove _).expects(5, 2)
    (screenMock.focused _).expects().returning(inputMock.asInstanceOf[BlessedElement])

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

    (inputMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (inputMock.width _).expects().twice().returning(10)
    (inputMock.aleft _).expects().returning(1)
    (inputMock.atop _).expects().returning(2)
    (programMock.omove _).expects(5, 2)
    
    val root = createTestRenderer(<(TextBox())(^.wrapped := props)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    }).root

    //then
    (inputMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (screenMock.cursor _).expects().returning(cursorMock.asInstanceOf[BlessedCursor])
    (cursorMock.shape _).expects().returning("underline")
    (cursorMock.blink _).expects().returning(false)
    (screenMock.cursorShape _).expects("underline", true)
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

    (inputMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (inputMock.width _).expects().twice().returning(10)
    (inputMock.aleft _).expects().returning(1)
    (inputMock.atop _).expects().returning(2)
    (programMock.omove _).expects(10, 2)

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
    val width = 10
    val aleft = 1
    val atop = 2
    
    def maxOffset = value.length - width + 1
    def maxCursorX = width - 1

    var offset = maxOffset
    var cursorX = maxCursorX

    (inputMock.screen _).expects().anyNumberOfTimes().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().anyNumberOfTimes().returning(programMock.asInstanceOf[BlessedProgram])
    (inputMock.width _).expects().anyNumberOfTimes().returning(width)
    (inputMock.aleft _).expects().anyNumberOfTimes().returning(aleft)
    (inputMock.atop _).expects().anyNumberOfTimes().returning(atop)
    (programMock.omove _).expects(aleft + cursorX, atop)

    val renderer = createTestRenderer(<(TextBox())(^.wrapped := props)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    })
    
    def check(defaultPrevented: Boolean, fullKey: String, posX: Int, newVal: String, idx: Int, ch: String = null): Unit = {
      //given
      val key = js.Dynamic.literal("full" -> fullKey).asInstanceOf[KeyboardKey]
      
      offset = idx
      if (cursorX != posX) {
        cursorX = posX
        (programMock.omove _).expects(aleft + cursorX, atop)
      }

      if (value != newVal) {
        onChange.expects(newVal)
      }
      val inputEl = renderer.root.children(0)
      
      //when
      inputEl.props.onKeypress(ch, key)
      
      //then
      key.defaultPrevented.getOrElse(false) shouldBe defaultPrevented

      if (value != newVal) {
        value = newVal

        TestRenderer.act { () =>
          renderer.update(<(TextBox())(^.wrapped := props.copy(value = newVal))())
        }
      }
      
      inputEl.props.content shouldBe newVal.substring(offset)
    }

    //when & then
    check(defaultPrevented = false, "escape", cursorX, value, offset)
    check(defaultPrevented = false, "return", cursorX, value, offset)
    check(defaultPrevented = false, "enter", cursorX, value, offset)
    check(defaultPrevented = false, "tab", cursorX, value, offset)
    
    //when & then
    check(defaultPrevented = true, "right", cursorX - 1, value, offset + 1)
    check(defaultPrevented = true, "end", cursorX + 1, value, maxOffset)
    check(defaultPrevented = true, "left", cursorX - 1, value, offset)
    check(defaultPrevented = true, "left", cursorX - 1, value, offset)
    check(defaultPrevented = true, "home", 0, value, 0)
    check(defaultPrevented = true, "left", 0, value, offset)
    check(defaultPrevented = true, "right", 1, value, offset)
    check(defaultPrevented = true, "right", 2, value, offset)
    check(defaultPrevented = true, "end", maxCursorX, value, maxOffset)
    
    //when & then
    check(defaultPrevented = true, "delete", cursorX, value, offset)
    check(defaultPrevented = true, "backspace", cursorX - 1, "initial nam", offset)
    check(defaultPrevented = true, "", cursorX + 1, "initial nam1", offset, "1")
    check(defaultPrevented = true, "", cursorX, "initial nam12", offset + 1, "2")
    check(defaultPrevented = true, "left", cursorX - 1, value, offset)
    check(defaultPrevented = true, "left", cursorX - 1, value, offset)
    check(defaultPrevented = true, "left", cursorX - 1, value, offset)
    check(defaultPrevented = true, "", cursorX + 1, "initial na3m12", offset, "3")
    check(defaultPrevented = true, "", cursorX + 1, "initial na34m12", offset, "4")
    check(defaultPrevented = true, "backspace", cursorX - 1, "initial na3m12", offset)
    check(defaultPrevented = true, "backspace", cursorX - 1, "initial nam12", offset)
    check(defaultPrevented = true, "delete", cursorX, "initial na12", offset)
    check(defaultPrevented = true, "delete", cursorX, "initial na2", offset)
    check(defaultPrevented = true, "home", 0, value, 0)
    check(defaultPrevented = true, "backspace", cursorX, value, offset)
    check(defaultPrevented = true, "delete", cursorX, "nitial na2", offset)
    check(defaultPrevented = true, "delete", cursorX, "itial na2", offset)
    
    //when & then
    check(defaultPrevented = false, "up", cursorX, value, offset)
    check(defaultPrevented = false, "down", cursorX, value, offset)
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

    def width: Int
    
    def aleft: Int
    def atop: Int

    def screen: BlessedScreen
    def focus(): Unit
  }
}
