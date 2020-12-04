package farjs.ui

import farjs.ui.TextBox.styles
import farjs.ui.TextBoxSpec._
import org.scalactic.source.Position
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

  it should "call onEnter and prevent default if return key when onKeypress" in {
    //given
    val onEnter = mockFunction[Unit]
    val props = getTextBoxProps(onEnter = onEnter)
    val programMock = mock[BlessedProgramMock]
    val screenMock = mock[BlessedScreenMock]
    val inputMock = mock[BlessedElementMock]
    val width = props.width
    val (aleft, atop) = props.pos
    val cursorX = width - 1
    (inputMock.screen _).expects().anyNumberOfTimes().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().anyNumberOfTimes().returning(programMock.asInstanceOf[BlessedProgram])
    (inputMock.width _).expects().anyNumberOfTimes().returning(width)
    (inputMock.aleft _).expects().anyNumberOfTimes().returning(aleft)
    (inputMock.atop _).expects().anyNumberOfTimes().returning(atop)
    (programMock.omove _).expects(aleft + cursorX, atop)

    val inputEl = testRender(<(TextBox())(^.wrapped := props)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    })
    val key = js.Dynamic.literal("full" -> "return").asInstanceOf[KeyboardKey]

    //then
    onEnter.expects()

    //when
    inputEl.props.onKeypress(null, key)

    //then
    key.defaultPrevented.getOrElse(false) shouldBe true
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
    var selStart = 0
    var selEnd = value.length
    
    def currIdx = offset + cursorX

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
    
    def check(defaultPrevented: Boolean,
              fullKey: String,
              idx: Int,
              posX: Int,
              newVal: String,
              startIdx: Int = -1,
              endIdx: Int = -1,
              ch: String = null
             )(implicit pos: Position): Unit = {
      
      //given
      val key = js.Dynamic.literal("full" -> fullKey).asInstanceOf[KeyboardKey]
      
      offset = idx
      selStart = startIdx
      selEnd = endIdx
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
      
      inputEl.props.content shouldBe {
        if (selEnd - selStart > 0) {
          val part1 = TextBox.renderText(styles.normal, newVal.slice(offset, selStart))
          val part2 = TextBox.renderText(styles.selected, newVal.slice(math.max(selStart, offset), selEnd))
          val part3 = TextBox.renderText(styles.normal, newVal.substring(math.min(selEnd, newVal.length)))
          s"$part1$part2$part3"
        } else TextBox.renderText(styles.normal, newVal.substring(math.min(offset, newVal.length)))
      }
    }

    //when & then
    check(defaultPrevented = true, "enter", offset, cursorX, value, selStart, selEnd)
    check(defaultPrevented = false, "escape", offset, cursorX, value, selStart, selEnd)
    check(defaultPrevented = false, "tab", offset, cursorX, value, selStart, selEnd)
    
    //when & then
    check(defaultPrevented = true, "right", offset + 1, cursorX - 1, value)
    check(defaultPrevented = true, "S-home", 0, 0, value, 0, currIdx)
    check(defaultPrevented = true, "right", offset, cursorX + 1, value)
    check(defaultPrevented = true, "S-right", offset, cursorX + 1, value, currIdx, currIdx + 1)
    check(defaultPrevented = true, "S-left", offset, cursorX - 1, value, currIdx - 1, selEnd)
    check(defaultPrevented = true, "S-end", maxOffset, maxCursorX, value, selStart, value.length)
    check(defaultPrevented = true, "C-a", maxOffset, maxCursorX, value, 0, value.length)
    check(defaultPrevented = true, "end", maxOffset, maxCursorX, value)
    check(defaultPrevented = true, "left", offset, cursorX - 1, value)
    check(defaultPrevented = true, "left", offset, cursorX - 1, value)
    check(defaultPrevented = true, "home", 0, 0, value)
    check(defaultPrevented = true, "left", offset, 0, value)
    check(defaultPrevented = true, "right", offset, 1, value)
    check(defaultPrevented = true, "right", offset, 2, value)
    check(defaultPrevented = true, "end", maxOffset, maxCursorX, value)
    
    //when & then
    check(defaultPrevented = true, "delete", offset, cursorX, value)
    check(defaultPrevented = true, "S-left", offset, cursorX - 1, value, currIdx - 1, value.length)
    check(defaultPrevented = true, "delete", offset, cursorX, "initial nam")
    check(defaultPrevented = true, "S-left", offset, cursorX - 1, value, currIdx - 1, value.length)
    check(defaultPrevented = true, "backspace", offset, cursorX, "initial na")
    check(defaultPrevented = true, "left", offset, cursorX - 1, value)
    check(defaultPrevented = true, "S-end", maxOffset, maxCursorX, value, currIdx, value.length)
    check(defaultPrevented = true, "delete", offset, cursorX - 1, "initial n")
    check(defaultPrevented = true, "", offset, cursorX + 1, "initial n1", ch = "1")
    check(defaultPrevented = true, "", offset + 1, cursorX, "initial n12", ch = "2")
    check(defaultPrevented = true, "S-left", offset, cursorX - 1, value, currIdx - 1, value.length)
    check(defaultPrevented = true, "S-left", offset, cursorX - 1, value, currIdx - 1, value.length)
    check(defaultPrevented = true, "", offset, cursorX + 1, "initial na", ch = "a")
    check(defaultPrevented = true, "", offset, cursorX + 1, "initial nam", ch = "m")
    check(defaultPrevented = true, "", offset + 1, cursorX, "initial name", ch = "e")
    check(defaultPrevented = true, "backspace", offset, cursorX - 1, "initial nam")
    check(defaultPrevented = true, "", offset, cursorX + 1, "initial nam1", ch = "1")
    check(defaultPrevented = true, "", offset + 1, cursorX, "initial nam12", ch = "2")
    check(defaultPrevented = true, "left", offset, cursorX - 1, value)
    check(defaultPrevented = true, "left", offset, cursorX - 1, value)
    check(defaultPrevented = true, "left", offset, cursorX - 1, value)
    check(defaultPrevented = true, "", offset, cursorX + 1, "initial na3m12", ch = "3")
    check(defaultPrevented = true, "", offset, cursorX + 1, "initial na34m12", ch = "4")
    check(defaultPrevented = true, "backspace", offset, cursorX - 1, "initial na3m12")
    check(defaultPrevented = true, "backspace", offset, cursorX - 1, "initial nam12")
    check(defaultPrevented = true, "delete", offset, cursorX, "initial na12")
    check(defaultPrevented = true, "delete", offset, cursorX, "initial na2")
    check(defaultPrevented = true, "home", 0, 0, value)
    check(defaultPrevented = true, "backspace", offset, cursorX, value)
    check(defaultPrevented = true, "delete", offset, cursorX, "nitial na2")
    check(defaultPrevented = true, "delete", offset, cursorX, "itial na2")
    
    //when & then
    check(defaultPrevented = false, "up", offset, cursorX, value, selStart, selEnd)
    check(defaultPrevented = false, "down", offset, cursorX, value, selStart, selEnd)
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
                              onChange: String => Unit = _ => (),
                              onEnter: () => Unit = () => ()
                             ): TextBoxProps = TextBoxProps(
    pos = (1, 2),
    width = 10,
    value = value,
    onChange = onChange,
    onEnter = onEnter
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
        ^.rbStyle := styles.normal,
        ^.content := TextBox.renderText(styles.normal, props.value)
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
