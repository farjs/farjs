package farjs.ui

import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import org.scalactic.source.Position
import scommons.react.blessed._
import scommons.react.raw.React
import scommons.react.test._
import scommons.react.test.raw.TestRenderer

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class TextInputSpec extends TestSpec with TestRendererUtils {

  private val currTheme = DefaultTheme

  it should "move cursor and grab focus when onClick" in {
    //given
    var state = TextInputState()
    val stateUpdater: js.Function1[js.Function1[TextInputState, TextInputState], Unit] = {
      updater => state = updater(state)
    }
    val props = getTextInputProps(state, stateUpdater)
    val omoveMock = mockFunction[Int, Int, Unit]
    val programMock = literal("omove" -> omoveMock)
    val screenMock = literal("program" -> programMock)
    val focusMock = mockFunction[Unit]
    val inputMock = literal("screen" -> screenMock, "focus" -> focusMock)
    inputMock.width = 10
    inputMock.aleft = 1
    inputMock.atop = 3
    omoveMock.expects(10, 3)

    val renderer = createTestRenderer(withThemeContext(<(TextInput())(^.plain := props)()), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock
      else null
    })
    TestRenderer.act { () =>
      renderer.update(withThemeContext(<(TextInput())(^.plain := TextInputProps.copy(props)(state = state))()))
    }

    //then
    inputMock.width = 10
    inputMock.aleft = 1
    inputMock.atop = 3
    omoveMock.expects(2, 3)
    focusMock.expects()

    //when
    renderer.root.children(0).props.onClick(literal(x = 2, y = 3))
  }

  it should "keep cursor position when onResize" in {
    //given
    var state = TextInputState()
    val stateUpdater: js.Function1[js.Function1[TextInputState, TextInputState], Unit] = {
      updater => state = updater(state)
    }
    val props = getTextInputProps(state, stateUpdater, value = "test")
    val omoveMock = mockFunction[Int, Int, Unit]
    val programMock = literal("omove" -> omoveMock)
    val screenMock = literal("program" -> programMock)
    val focusMock = mockFunction[Unit]
    val inputMock = literal("screen" -> screenMock, "focus" -> focusMock)
    inputMock.width = 10
    inputMock.aleft = 1
    inputMock.atop = 2
    omoveMock.expects(5, 2)
    
    val renderer = createTestRenderer(withThemeContext(<(TextInput())(^.plain := props)()), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock
      else null
    })
    TestRenderer.act { () =>
      renderer.update(withThemeContext(<(TextInput())(^.plain := TextInputProps.copy(props)(state = state))()))
    }

    //then
    inputMock.aleft = 1
    inputMock.atop = 2
    omoveMock.expects(5, 2)
    screenMock.focused = inputMock

    //when
    renderer.root.children(0).props.onResize()
  }

  it should "show cursor when onFocus" in {
    //given
    var state = TextInputState()
    val stateUpdater: js.Function1[js.Function1[TextInputState, TextInputState], Unit] = {
      updater => state = updater(state)
    }
    val props = getTextInputProps(state, stateUpdater, value = "test")
    val omoveMock = mockFunction[Int, Int, Unit]
    val programMock = literal("omove" -> omoveMock)
    val screenMock = literal("program" -> programMock)
    val focusMock = mockFunction[Unit]
    val inputMock = literal("screen" -> screenMock, "focus" -> focusMock)
    inputMock.width = 10
    inputMock.aleft = 1
    inputMock.atop = 2
    omoveMock.expects(5, 2)
    
    val renderer = createTestRenderer(withThemeContext(<(TextInput())(^.plain := props)()), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock
      else null
    })
    TestRenderer.act { () =>
      renderer.update(withThemeContext(<(TextInput())(^.plain := TextInputProps.copy(props)(state = state))()))
    }

    //then
    val cursorMock = literal("shape" -> "underline", "blink" -> false)
    val cursorShapeMock = mockFunction[String, Boolean, Boolean]
    val showCursorMock = mockFunction[Unit]
    screenMock.cursor = cursorMock
    screenMock.cursorShape = cursorShapeMock
    programMock.showCursor = showCursorMock
    cursorShapeMock.expects("underline", true)
    showCursorMock.expects()

    //when
    renderer.root.children(0).props.onFocus()
  }

  it should "hide cursor when onBlur" in {
    //given
    var state = TextInputState()
    val stateUpdater: js.Function1[js.Function1[TextInputState, TextInputState], Unit] = {
      updater => state = updater(state)
    }
    val props = getTextInputProps(state, stateUpdater)
    val omoveMock = mockFunction[Int, Int, Unit]
    val programMock = literal("omove" -> omoveMock)
    val screenMock = literal("program" -> programMock)
    val inputMock = literal("screen" -> screenMock)
    inputMock.width = 10
    inputMock.aleft = 1
    inputMock.atop = 2
    omoveMock.expects(10, 2)

    val renderer = createTestRenderer(withThemeContext(<(TextInput())(^.plain := props)()), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock
      else null
    })
    TestRenderer.act { () =>
      renderer.update(withThemeContext(<(TextInput())(^.plain := TextInputProps.copy(props)(state = state))()))
    }

    //then
    val hideCursorMock = mockFunction[Unit]
    programMock.hideCursor = hideCursorMock
    hideCursorMock.expects()

    //when
    renderer.root.children(0).props.onBlur()
  }

  it should "prevent default if return true from props.onKeypress" in {
    //given
    val onKeypress = mockFunction[String, Boolean]
    val state = TextInputState()
    val stateUpdater: js.Function1[js.Function1[TextInputState, TextInputState], Unit] = {
      updater => updater(state)
    }
    val props = getTextInputProps(state, stateUpdater, onKeypress = onKeypress: js.Function1[String, Boolean])
    val omoveMock = mockFunction[Int, Int, Unit]
    val programMock = literal("omove" -> omoveMock)
    val screenMock = literal("program" -> programMock)
    val inputMock = literal("screen" -> screenMock)
    val width = props.width
    val cursorX = width - 1
    inputMock.width = width
    inputMock.aleft = props.left
    inputMock.atop = props.top
    omoveMock.expects(props.left + cursorX, props.top)

    val inputEl = testRender(withThemeContext(<(TextInput())(^.plain := props)()), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock
      else null
    })
    val key = literal("full" -> "C-down").asInstanceOf[KeyboardKey]

    //then
    onKeypress.expects("C-down").returning(true)

    //when
    inputEl.props.onKeypress(null, key)

    //then
    key.defaultPrevented.getOrElse(false) shouldBe true
  }
  
  it should "call onEnter and prevent default if return key when onKeypress" in {
    //given
    val onEnter = mockFunction[Unit]
    val state = TextInputState()
    val stateUpdater: js.Function1[js.Function1[TextInputState, TextInputState], Unit] = {
      updater => updater(state)
    }
    val props = getTextInputProps(state, stateUpdater, onEnter = onEnter)
    val omoveMock = mockFunction[Int, Int, Unit]
    val programMock = literal("omove" -> omoveMock)
    val screenMock = literal("program" -> programMock)
    val inputMock = literal("screen" -> screenMock)
    val width = props.width
    val cursorX = width - 1
    inputMock.width = width
    inputMock.aleft = props.left
    inputMock.atop = props.top
    omoveMock.expects(props.left + cursorX, props.top)

    val inputEl = testRender(withThemeContext(<(TextInput())(^.plain := props)()), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock
      else null
    })
    val key = literal("full" -> "return").asInstanceOf[KeyboardKey]

    //then
    onEnter.expects()

    //when
    inputEl.props.onKeypress(null, key)

    //then
    key.defaultPrevented.getOrElse(false) shouldBe true
  }
  
  it should "copy selection to clipboard if C-c key when onKeypress" in {
    //given
    val onEnter = mockFunction[Unit]
    var state = TextInputState()
    val stateUpdater: js.Function1[js.Function1[TextInputState, TextInputState], Unit] = {
      updater => state = updater(state)
    }
    val props = getTextInputProps(state, stateUpdater, onEnter = onEnter)
    val omoveMock = mockFunction[Int, Int, Unit]
    val programMock = literal("omove" -> omoveMock)
    val screenMock = literal("program" -> programMock)
    val inputMock = literal("screen" -> screenMock)
    val width = props.width
    val cursorX = width - 1
    inputMock.width = width
    inputMock.aleft = props.left
    inputMock.atop = props.top
    omoveMock.expects(props.left + cursorX, props.top)

    val renderer = createTestRenderer(withThemeContext(<(TextInput())(^.plain := props)()), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock
      else null
    })
    TestRenderer.act { () =>
      renderer.update(withThemeContext(<(TextInput())(^.plain := TextInputProps.copy(props)(state = state))()))
    }
    val inputEl = renderer.root.children(0)
    val key = literal("full" -> "C-c").asInstanceOf[KeyboardKey]

    //then
    val copyToClipboardMock = mockFunction[String, Boolean]
    screenMock.copyToClipboard = copyToClipboardMock
    copyToClipboardMock.expects(props.value)

    //when
    inputEl.props.onKeypress(null, key)

    //then
    key.defaultPrevented.getOrElse(false) shouldBe true
  }
  
  it should "cut selection to clipboard if C-x key when onKeypress" in {
    //given
    val onEnter = mockFunction[Unit]
    var state = TextInputState()
    val stateUpdater: js.Function1[js.Function1[TextInputState, TextInputState], Unit] = {
      updater => state = updater(state)
    }
    val props = getTextInputProps(state, stateUpdater, onEnter = onEnter)
    val omoveMock = mockFunction[Int, Int, Unit]
    val programMock = literal("omove" -> omoveMock)
    val screenMock = literal("program" -> programMock)
    val inputMock = literal("screen" -> screenMock)
    val width = props.width
    val cursorX = width - 1
    inputMock.width = width
    inputMock.aleft = props.left
    inputMock.atop = props.top
    omoveMock.expects(props.left + cursorX, props.top)

    val renderer = createTestRenderer(withThemeContext(<(TextInput())(^.plain := props)()), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock
      else null
    })
    TestRenderer.act { () =>
      renderer.update(withThemeContext(<(TextInput())(^.plain := TextInputProps.copy(props)(state = state))()))
    }
    val inputEl = renderer.root.children(0)
    val key = literal("full" -> "C-x").asInstanceOf[KeyboardKey]

    //then
    val copyToClipboardMock = mockFunction[String, Boolean]
    screenMock.copyToClipboard = copyToClipboardMock
    copyToClipboardMock.expects(props.value)
    omoveMock.expects(props.left, props.top)

    //when
    inputEl.props.onKeypress(null, key)

    //then
    key.defaultPrevented.getOrElse(false) shouldBe true
  }
  
  it should "process key and prevent default when onKeypress" in {
    //given
    val onChange = mockFunction[String, Unit]
    var value = "initial name"
    var state = TextInputState()
    var renderer: TestRenderer = null
    var props: TextInputProps = null
    val stateUpdater: js.Function1[js.Function1[TextInputState, TextInputState], Unit] = {
      updater =>
        state = updater(state)
        props = TextInputProps.copy(props)(state = state)
        TestRenderer.act { () =>
          if (renderer != null) {
            renderer.update(withThemeContext(<(TextInput())(^.plain := props)()))
          }
        }
    }
    props = getTextInputProps(state, stateUpdater, value, onChange)
    val omoveMock = mockFunction[Int, Int, Unit]
    val programMock = literal("omove" -> omoveMock)
    val screenMock = literal("program" -> programMock)
    val inputMock = literal("screen" -> screenMock)
    val width = 10
    val aleft = 1
    val atop = 2
    
    def maxOffset: Int = value.length - width + 1
    def maxCursorX: Int = width - 1

    var offset = maxOffset
    var cursorX = maxCursorX
    var selStart = 0
    var selEnd = value.length
    
    def currIdx: Int = offset + cursorX

    inputMock.width = width
    inputMock.aleft = aleft
    inputMock.atop = atop
    omoveMock.expects(aleft + cursorX, atop)

    renderer = createTestRenderer(withThemeContext(<(TextInput())(^.plain := props)()), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock
      else null
    })
    TestRenderer.act { () =>
      renderer.update(withThemeContext(<(TextInput())(^.plain := props)()))
    }
    
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
      val key = literal("full" -> fullKey).asInstanceOf[KeyboardKey]
      
      offset = idx
      selStart = startIdx
      selEnd = endIdx
      if (cursorX != posX) {
        cursorX = posX
        omoveMock.expects(aleft + cursorX, atop)
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
        props = TextInputProps.copy(props)(value = newVal)

        TestRenderer.act { () =>
          renderer.update(withThemeContext(<(TextInput())(^.plain := props)()))
        }
      }
      
      val theme = currTheme.textBox
      inputEl.props.content shouldBe {
        val currValue = UiString(newVal)
        if (selEnd - selStart > 0) {
          val part1 = UI.renderText2(theme.regular, currValue.slice(offset, selStart))
          val part2 = UI.renderText2(theme.selected, currValue.slice(math.max(selStart, offset), selEnd))
          val part3 = UI.renderText2(theme.regular, currValue.slice(selEnd, currValue.strWidth()))
          s"$part1$part2$part3"
        } else UI.renderText2(theme.regular, currValue.slice(offset, currValue.strWidth()))
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
    check(defaultPrevented = true, "S-end", offset, maxCursorX, value, currIdx, value.length)
    check(defaultPrevented = true, "delete", offset, 0, "")

    //when & then
    check(defaultPrevented = true, "", offset, cursorX + 1, "и", ch = "и")
    check(defaultPrevented = true, "", offset, cursorX, "й", ch = "й".substring(1))
    check(defaultPrevented = true, "", offset, cursorX + 2, "й杜", ch = "杜")
    check(defaultPrevented = true, "left", offset, cursorX - 2, value)
    check(defaultPrevented = true, "S-left", offset, cursorX - 1, value, startIdx = 0, endIdx = 1)
    check(defaultPrevented = true, "", offset, cursorX + 2, "杜杜", ch = "杜")
    check(defaultPrevented = true, "", offset, cursorX, "杜杜", ch = "\uD834") //high surrogate
    check(defaultPrevented = true, "", offset, cursorX + 1, "杜\uD834\uDF06杜", ch = "\uDF06") //low surrogate
    check(defaultPrevented = true, "left", offset, cursorX - 1, value)
    check(defaultPrevented = true, "backspace", offset, cursorX - 2, "\uD834\uDF06杜")
    check(defaultPrevented = true, "right", offset, cursorX + 1, value)
    check(defaultPrevented = true, "right", offset, cursorX + 2, value)
    check(defaultPrevented = true, "", offset, cursorX + 1, "\uD834\uDF06杜и", ch = "и")
    check(defaultPrevented = true, "", offset, cursorX, "\uD834\uDF06杜й", ch = "й".substring(1))
    check(defaultPrevented = true, "left", offset, cursorX - 1, value)
    check(defaultPrevented = true, "left", offset, cursorX - 2, value)
    check(defaultPrevented = true, "delete", offset, cursorX, "\uD834\uDF06й")
    check(defaultPrevented = true, "delete", offset, cursorX, "\uD834\uDF06")
    check(defaultPrevented = true, "backspace", offset, cursorX - 1, "")
    
    //when & then
    check(defaultPrevented = false, "up", offset, cursorX, value, selStart, selEnd)
    check(defaultPrevented = false, "down", offset, cursorX, value, selStart, selEnd)
  }

  it should "render initial component" in {
    //given
    var state = TextInputState()
    val stateUpdater: js.Function1[js.Function1[TextInputState, TextInputState], Unit] = {
      updater => state = updater(state)
    }
    val props = getTextInputProps(state, stateUpdater)
    val omoveMock = mockFunction[Int, Int, Unit]
    val programMock = literal("omove" -> omoveMock)
    val screenMock = literal("program" -> programMock)
    val inputMock = literal("screen" -> screenMock)
    inputMock.width = 10
    inputMock.aleft = 1
    inputMock.atop = 3
    omoveMock.expects(10, 3)

    //when
    val renderer = createTestRenderer(withThemeContext(<(TextInput())(^.plain := props)()), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock
      else null
    })
    TestRenderer.act { () =>
      renderer.update(withThemeContext(<(TextInput())(^.plain := TextInputProps.copy(props)(state = state))()))
    }

    //then
    assertTextInput(renderer.root.children(0), props)
  }

  private def getTextInputProps(state: TextInputState,
                                stateUpdater: js.Function1[js.Function1[TextInputState, TextInputState], Unit],
                                value: String = "initial name",
                                onChange: String => Unit = _ => (),
                                onEnter: js.Function0[Unit] = () => (),
                                onKeypress: js.UndefOr[js.Function1[String, Boolean]] = js.undefined
                               ): TextInputProps = TextInputProps(
    inputRef = React.createRef(),
    left = 1,
    top = 2,
    width = 10,
    value = value,
    state = state,
    stateUpdater = stateUpdater,
    onChange = onChange,
    onEnter = onEnter,
    onKeypress = onKeypress
  )

  private def assertTextInput(result: TestInstance, props: TextInputProps): Unit = {
    val theme = currTheme.textBox
    val selectedText = props.value.drop(props.value.length - props.width + 1)
    
    assertNativeComponent(result,
      <.input(
        ^.rbAutoFocus := false,
        ^.rbClickable := true,
        ^.rbKeyable := true,
        ^.rbWidth := props.width,
        ^.rbHeight := 1,
        ^.rbLeft := props.left,
        ^.rbTop := props.top,
        ^.rbStyle := theme.regular,
        ^.rbWrap := false,
        ^.rbTags := true,
        ^.content := UI.renderText2(theme.selected, selectedText)
      )()
    )
  }
}
