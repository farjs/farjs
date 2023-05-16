package farjs.ui

import farjs.ui.ComboBox._
import farjs.ui.popup.PopupOverlay
import farjs.ui.theme.DefaultTheme
import org.scalatest.Succeeded
import scommons.nodejs._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class ComboBoxSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  ComboBox.textInputComp = mockUiComponent("TextInput")
  ComboBox.comboBoxPopup = mockUiComponent("ComboBoxPopup")

  it should "call onChange, hide popup and emit keypress event when popup.onClick" in {
    //given
    val onChange = mockFunction[String, Unit]
    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)
    val props = getComboBoxProps(onChange = onChange)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-down") shouldBe true
    val comboBox = findComponentProps(renderer.root, comboBoxPopup)

    //then
    onChange.expects("item 2")
    onKey.expects("end", false, false, false)

    //when
    comboBox.onClick(1)

    //then
    findProps(renderer.root, comboBoxPopup) should be (empty)

    //cleanup
    process.stdin.removeListener("keypress", listener)
    Succeeded
  }

  it should "call onChange, hide popup and emit keypress event when onKeypress(return)" in {
    //given
    val onChange = mockFunction[String, Unit]
    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)
    val props = getComboBoxProps(onChange = onChange)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-down") shouldBe true
    findProps(renderer.root, comboBoxPopup) should not be empty
    val textInput = findComponentProps(renderer.root, textInputComp)

    //then
    onChange.expects("item")
    onKey.expects("end", false, false, false)

    //when
    textInput.onKeypress("return") shouldBe true

    //then
    findProps(renderer.root, comboBoxPopup) should be (empty)

    //cleanup
    process.stdin.removeListener("keypress", listener)
    Succeeded
  }

  it should "do nothing if no items when onKeypress(return)" in {
    //given
    val onChange = mockFunction[String, Unit]
    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)
    val props = getComboBoxProps(items = Nil, onChange = onChange)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-down") shouldBe true
    findProps(renderer.root, comboBoxPopup) should not be empty
    val textInput = findComponentProps(renderer.root, textInputComp)

    //then
    onChange.expects(*).never()
    onKey.expects(*, *, *, *).never()

    //when
    textInput.onKeypress("return") shouldBe true

    //then
    findProps(renderer.root, comboBoxPopup) should not be empty

    //cleanup
    process.stdin.removeListener("keypress", listener)
    Succeeded
  }

  it should "do autocomplete if selected when single char" in {
    //given
    val onChange = mockFunction[String, Unit]
    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    var onKeyCalled = false
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKeyCalled = true
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)
    val props = getComboBoxProps(items = List("abc", "ac"), value = "ad", onChange = onChange)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    val textInput = findComponentProps(renderer.root, textInputComp)
    textInput.stateUpdater(_.copy(selStart = 1))

    //then
    onChange.expects("abc")
    onKey.expects("end", false, false, true)

    //when
    findComponentProps(renderer.root, textInputComp).onKeypress("b") shouldBe false

    //then
    eventually {
      onKeyCalled shouldBe true
    }.map { _ =>
      //cleanup
      process.stdin.removeListener("keypress", listener)
      Succeeded
    }
  }

  it should "do autocomplete if not selected when single char" in {
    //given
    val onChange = mockFunction[String, Unit]
    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    var onKeyCalled = false
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKeyCalled = true
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)
    val props = getComboBoxProps(items = List("abc", "ac"), value = "", onChange = onChange)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    val textInput = findComponentProps(renderer.root, textInputComp)

    //then
    onChange.expects("abc")
    onKey.expects("end", false, false, true)

    //when
    textInput.onKeypress("a") shouldBe false

    //then
    eventually {
      onKeyCalled shouldBe true
    }.map { _ =>
      //cleanup
      process.stdin.removeListener("keypress", listener)
      Succeeded
    }
  }

  it should "do autocomplete when upper-case char" in {
    //given
    val onChange = mockFunction[String, Unit]
    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    var onKeyCalled = false
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKeyCalled = true
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)
    val props = getComboBoxProps(items = List("aBc", "ac"), value = "a", onChange = onChange)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    val textInput = findComponentProps(renderer.root, textInputComp)

    //then
    onChange.expects("aBc")
    onKey.expects("end", false, false, true)

    //when
    textInput.onKeypress("S-b") shouldBe false

    //then
    eventually {
      onKeyCalled shouldBe true
    }.map { _ =>
      //cleanup
      process.stdin.removeListener("keypress", listener)
      Succeeded
    }
  }

  it should "do autocomplete when space char" in {
    //given
    val onChange = mockFunction[String, Unit]
    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    var onKeyCalled = false
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKeyCalled = true
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)
    val props = getComboBoxProps(items = List("a c", " c"), value = "a", onChange = onChange)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    val textInput = findComponentProps(renderer.root, textInputComp)

    //then
    onChange.expects("a c")
    onKey.expects("end", false, false, true)

    //when
    textInput.onKeypress("space") shouldBe false

    //then
    eventually {
      onKeyCalled shouldBe true
    }.map { _ =>
      //cleanup
      process.stdin.removeListener("keypress", listener)
      Succeeded
    }
  }

  it should "clear timeout when autocomplete" in {
    //given
    val onChange = mockFunction[String, Unit]
    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    var onKeyCalled = false
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKeyCalled = true
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)
    val props = getComboBoxProps(items = List("abc", "ac"), value = "", onChange = onChange)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())

    //then
    onChange.expects("abc")
    onKey.expects("end", false, false, true)

    //when
    findComponentProps(renderer.root, textInputComp).onKeypress("a") shouldBe false
    TestRenderer.act { () =>
      renderer.update(<(ComboBox())(^.plain := ComboBoxProps.copy(props)(value = "a"))())
    }
    findComponentProps(renderer.root, textInputComp).onKeypress("b") shouldBe false

    //then
    eventually {
      onKeyCalled shouldBe true
    }.map { _ =>
      //cleanup
      process.stdin.removeListener("keypress", listener)
      Succeeded
    }
  }

  it should "hide popup when onKeypress(escape)" in {
    //given
    val props = getComboBoxProps()
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-down") shouldBe true
    findProps(renderer.root, comboBoxPopup) should not be empty
    val textInput = findComponentProps(renderer.root, textInputComp)

    //when
    textInput.onKeypress("escape") shouldBe true

    //then
    findProps(renderer.root, comboBoxPopup) should be (empty)
  }

  it should "hide popup if shown when onKeypress(C-up)" in {
    //given
    val props = getComboBoxProps()
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-up") shouldBe true
    findProps(renderer.root, comboBoxPopup) should not be empty
    val textInput = findComponentProps(renderer.root, textInputComp)

    //when
    textInput.onKeypress("C-up") shouldBe true

    //then
    findProps(renderer.root, comboBoxPopup) should be (empty)
  }

  it should "hide popup and show cursor when form.onClick" in {
    //given
    val props = getComboBoxProps()
    val hideCursorMock = mockFunction[Unit]
    val showCursorMock = mockFunction[Unit]
    val programMock = literal(hideCursor = hideCursorMock, showCursor = showCursorMock)
    val screenMock = literal(program = programMock)
    val formMock = literal(screen = screenMock)
    hideCursorMock.expects()
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)(), { el =>
      if (el.`type` == <.form.name.asInstanceOf[js.Any]) formMock
      else null
    })
    findComponentProps(renderer.root, textInputComp).onKeypress("C-up") shouldBe true
    findProps(renderer.root, comboBoxPopup) should not be empty
    val form = inside(findComponents(renderer.root, <.form.name)) {
      case List(form) => form
    }

    //then
    showCursorMock.expects()

    //when
    form.props.onClick()

    //then
    findProps(renderer.root, comboBoxPopup) should be (empty)
  }

  it should "hide popup and show cursor when arrow.onClick" in {
    //given
    val props = getComboBoxProps()
    val hideCursorMock = mockFunction[Unit]
    val showCursorMock = mockFunction[Unit]
    val programMock = literal(hideCursor = hideCursorMock, showCursor = showCursorMock)
    val screenMock = literal(program = programMock)
    val formMock = literal(screen = screenMock)
    hideCursorMock.expects()
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)(), { el =>
      if (el.`type` == <.form.name.asInstanceOf[js.Any]) formMock
      else null
    })
    findComponentProps(renderer.root, textInputComp).onKeypress("C-up") shouldBe true
    findProps(renderer.root, comboBoxPopup) should not be empty
    val arrow = inside(findComponents(renderer.root, <.text.name)) {
      case List(arrow) => arrow
    }

    //then
    showCursorMock.expects()

    //when
    arrow.props.onClick(null)

    //then
    findProps(renderer.root, comboBoxPopup) should be (empty)
  }

  it should "show popup and not focus input when arrow.onClick" in {
    //given
    val props = getComboBoxProps()
    val focusMock = mockFunction[Unit]
    val screenMock = literal()
    val textMock = literal(screen = screenMock, focus = focusMock)
    screenMock.focused = textMock
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).inputRef.current =
      textMock.asInstanceOf[BlessedElement]
    findProps(renderer.root, comboBoxPopup) should be (empty)
    val arrow = inside(findComponents(renderer.root, <.text.name)) {
      case List(arrow) => arrow
    }

    //then
    focusMock.expects().never()

    //when
    arrow.props.onClick(null)

    //then
    findProps(renderer.root, comboBoxPopup) should not be empty
  }

  it should "show popup and focus input when arrow.onClick" in {
    //given
    val props = getComboBoxProps()
    val focusMock = mockFunction[Unit]
    val screenMock = literal()
    val textMock = literal(screen = screenMock, focus = focusMock)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).inputRef.current =
      textMock.asInstanceOf[BlessedElement]
    findProps(renderer.root, comboBoxPopup) should be (empty)
    val arrow = inside(findComponents(renderer.root, <.text.name)) {
      case List(arrow) => arrow
    }

    //then
    focusMock.expects()

    //when
    arrow.props.onClick(null)

    //then
    findProps(renderer.root, comboBoxPopup) should not be empty
  }

  it should "show popup when onKeypress(C-down)" in {
    //given
    val props = getComboBoxProps()
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    val textInput = findComponentProps(renderer.root, textInputComp)
    findProps(renderer.root, comboBoxPopup) should be (empty)

    //when
    textInput.onKeypress("C-down") shouldBe true

    //then
    val theme = DefaultTheme.popup.menu
    val arrowStyle = DefaultTheme.popup.regular
    assertComponents(renderer.root.children, List(
      <(textInputComp())(^.assertWrapped(inside(_) {
        case TextInputProps(_, left, top, width, value, state, _, onChange, onEnter, _) =>
          left shouldBe props.left
          top shouldBe props.top
          width shouldBe props.width
          value shouldBe props.value
          state shouldBe TextInputState()
          onChange shouldBe props.onChange
          onEnter shouldBe props.onEnter
      }))(),

      <.text(
        ^.rbWidth := 1,
        ^.rbHeight := 1,
        ^.rbLeft := props.left + props.width,
        ^.rbTop := props.top,
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := arrowStyle,
        ^.content := arrowDownCh
      )(),
      
      <.form(
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := PopupOverlay.style
      )(
        <(comboBoxPopup())(^.assertWrapped(inside(_) {
          case ComboBoxPopupProps(left, top, width, items, viewport, _, style, _) =>
            left shouldBe props.left
            top shouldBe props.top + 1
            width shouldBe props.width
            items.toList shouldBe List("item", "item 2")
            viewport.focused shouldBe 0
            style shouldBe theme
        }))()
      )
    ))
  }

  it should "update viewport when setViewport in popup" in {
    //given
    val items = List("1", "2", "3")
    val props = getComboBoxProps(items = items)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-down") shouldBe true
    val popup = findComponentProps(renderer.root, comboBoxPopup)
    popup.items.toList shouldBe items
    popup.viewport.focused shouldBe 0
    val viewport = popup.viewport.copy(focused = 1)

    //when
    popup.setViewport(viewport)

    //then
    findComponentProps(renderer.root, comboBoxPopup).viewport should be theSameInstanceAs viewport
  }

  it should "return false if popup not shown when onKeypress" in {
    //given
    val props = getComboBoxProps()
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    val textInput = findComponentProps(renderer.root, textInputComp)

    //when & then
    textInput.onKeypress("escape") shouldBe false
    textInput.onKeypress("tab") shouldBe false
    textInput.onKeypress("up") shouldBe false
    textInput.onKeypress("down") shouldBe false
    textInput.onKeypress("return") shouldBe false
    textInput.onKeypress("unknown") shouldBe false
  }

  it should "return true if popup is shown when onKeypress(up/down/unknown)" in {
    //given
    val props = getComboBoxProps()
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-up") shouldBe true
    findProps(renderer.root, comboBoxPopup) should not be empty
    val textInput = findComponentProps(renderer.root, textInputComp)

    //when & then
    textInput.onKeypress("up") shouldBe true
    textInput.onKeypress("down") shouldBe true
    textInput.onKeypress("unknown") shouldBe true

    //then
    findProps(renderer.root, comboBoxPopup) should not be empty
  }

  it should "update input state when stateUpdater" in {
    //given
    val props = getComboBoxProps()
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    val textInput = findComponentProps(renderer.root, textInputComp)

    //when
    textInput.stateUpdater(_.copy(
      offset = 1,
      cursorX = 2,
      selStart = 3,
      selEnd = 4
    ))

    //then
    findComponentProps(renderer.root, textInputComp).state shouldBe TextInputState(
      offset = 1,
      cursorX = 2,
      selStart = 3,
      selEnd = 4
    )
  }

  it should "render initial component" in {
    //given
    val props = getComboBoxProps()

    //when
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())

    //then
    val arrowStyle = DefaultTheme.popup.regular
    assertComponents(renderer.root.children, List(
      <(textInputComp())(^.assertWrapped(inside(_) {
        case TextInputProps(_, left, top, width, value, state, _, onChange, onEnter, _) =>
          left shouldBe props.left
          top shouldBe props.top
          width shouldBe props.width
          value shouldBe props.value
          state shouldBe TextInputState()
          onChange shouldBe props.onChange
          onEnter shouldBe props.onEnter
      }))(),

      <.text(
        ^.rbWidth := 1,
        ^.rbHeight := 1,
        ^.rbLeft := props.left + props.width,
        ^.rbTop := props.top,
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := arrowStyle,
        ^.content := arrowDownCh
      )()
    ))
  }

  private def getComboBoxProps(items: List[String] = List("item", "item 2"),
                               value: String = "initial name",
                               onChange: String => Unit = _ => (),
                               onEnter: js.Function0[Unit] = () => ()
                              ): ComboBoxProps = ComboBoxProps(
    left = 1,
    top = 2,
    width = 10,
    items = js.Array(items: _*),
    value = value,
    onChange = onChange,
    onEnter = onEnter
  )
}
