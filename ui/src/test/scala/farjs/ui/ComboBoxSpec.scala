package farjs.ui

import farjs.ui.ComboBox._
import farjs.ui.ComboBoxPopup.maxItems
import farjs.ui.popup.PopupOverlay
import farjs.ui.theme.DefaultTheme
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.blessed._
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class ComboBoxSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  ComboBox.textInputComp = mockUiComponent("TextInput")
  ComboBox.comboBoxPopup = mockUiComponent("ComboBoxPopup")
  ComboBox.scrollBarComp = mockUiComponent("ScrollBar")

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
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
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

    //cleanup
    Future.unit.map { _ =>
      process.stdin.removeListener("keypress", listener)
      Succeeded
    }
  }

  it should "do autocomplete if not selected when single char" in {
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
    val props = getComboBoxProps(items = List("abc", "ac"), value = "", onChange = onChange)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    val textInput = findComponentProps(renderer.root, textInputComp)

    //then
    onChange.expects("abc")
    onKey.expects("end", false, false, true)

    //when
    textInput.onKeypress("a") shouldBe false

    //cleanup
    Future.unit.map { _ =>
      process.stdin.removeListener("keypress", listener)
      Succeeded
    }
  }

  it should "do autocomplete when upper-case char" in {
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
    val props = getComboBoxProps(items = List("aBc", "ac"), value = "a", onChange = onChange)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    val textInput = findComponentProps(renderer.root, textInputComp)

    //then
    onChange.expects("aBc")
    onKey.expects("end", false, false, true)

    //when
    textInput.onKeypress("S-b") shouldBe false

    //cleanup
    Future.unit.map { _ =>
      process.stdin.removeListener("keypress", listener)
      Succeeded
    }
  }

  it should "do autocomplete when space char" in {
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
    val props = getComboBoxProps(items = List("a c", " c"), value = "a", onChange = onChange)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    val textInput = findComponentProps(renderer.root, textInputComp)

    //then
    onChange.expects("a c")
    onKey.expects("end", false, false, true)

    //when
    textInput.onKeypress("space") shouldBe false

    //cleanup
    Future.unit.map { _ =>
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
          case ComboBoxPopupProps(selected, items, left, top, width, _, _) =>
            selected shouldBe 0
            items shouldBe List("item", "item 2")
            left shouldBe props.left
            top shouldBe props.top + 1
            width shouldBe props.width
        }))()
      )
    ))
  }

  it should "show popup with ScrollBar when onKeypress(C-down)" in {
    //given
    val items = List("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
    val page1 = List("1", "2", "3", "4", "5", "6", "7", "8")
    val props = getComboBoxProps(items = items)
    props.items.length should be > maxItems
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
          case ComboBoxPopupProps(selected, items, left, top, width, _, _) =>
            selected shouldBe 0
            items shouldBe page1
            left shouldBe props.left
            top shouldBe props.top + 1
            width shouldBe props.width
        }))(),

        <(scrollBarComp())(^.assertPlain[ScrollBarProps](inside(_) {
          case ScrollBarProps(left, top, length, style, value, extent, min, max, _) =>
            left shouldBe (props.left + props.width - 1)
            top shouldBe (props.top + 2)
            length shouldBe maxItems
            style shouldBe theme
            value shouldBe 0
            extent shouldBe maxItems
            min shouldBe 0
            max shouldBe (items.size - maxItems)
        }))()
      )
    ))
  }

  it should "scroll when onChange in ScrollBar" in {
    //given
    val items = List("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
    val page1 = List("1", "2", "3", "4", "5", "6", "7", "8")
    val page2 = List("2", "3", "4", "5", "6", "7", "8", "9")
    val props = getComboBoxProps(items = items)
    props.items.length should be > maxItems
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-down") shouldBe true
    inside(findComponentProps(renderer.root, comboBoxPopup)) { case popupProps =>
      popupProps.items shouldBe page1
      popupProps.selected shouldBe 0
    }
    val scrollBarProps = findComponentProps(renderer.root, scrollBarComp, plain = true)

    //when
    scrollBarProps.onChange(1)

    //then
    inside(findComponentProps(renderer.root, comboBoxPopup)) { case popupProps =>
      popupProps.items shouldBe page2
      popupProps.selected shouldBe 0
    }
    inside(findComponentProps(renderer.root, scrollBarComp, plain = true)) { case scrollBarProps =>
      scrollBarProps.value shouldBe 1
    }
  }

  it should "select items when onWheel(true/false)" in {
    //given
    val items = List("1", "2", "3")
    val props = getComboBoxProps(items = items)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-down") shouldBe true
    findComponentProps(renderer.root, comboBoxPopup).items shouldBe items
    findComponentProps(renderer.root, comboBoxPopup).selected shouldBe 0

    def check(up: Boolean, items: List[String], selected: Int): Assertion = {
      findComponentProps(renderer.root, comboBoxPopup).onWheel(up)
      inside(findComponentProps(renderer.root, comboBoxPopup)) { case popupProps =>
        popupProps.items shouldBe items
        popupProps.selected shouldBe selected
      }
    }

    //when & then
    check(up = true, items, 0)
    check(up = true, items, 0)
    check(up = false, items, 1)
    check(up = false, items, 2)
    check(up = false, items, 2)
    check(up = true, items, 1)
    check(up = true, items, 0)
    check(up = true, items, 0)
  }

  it should "not select if empty items when onKeypress(page-/down/up|end|home)" in {
    //given
    val props = getComboBoxProps(items = Nil)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-down") shouldBe true
    findComponentProps(renderer.root, comboBoxPopup).items shouldBe Nil
    findComponentProps(renderer.root, comboBoxPopup).selected shouldBe 0

    def check(keyFull: String, items: List[String], selected: Int): Assertion = {
      findComponentProps(renderer.root, textInputComp).onKeypress(keyFull) shouldBe true
      inside(findComponentProps(renderer.root, comboBoxPopup)) { case popupProps =>
        popupProps.items shouldBe items
        popupProps.selected shouldBe selected
      }
    }

    //when & then
    check("up", Nil, 0)
    check("down", Nil, 0)
    check("pageup", Nil, 0)
    check("pagedown", Nil, 0)
    check("home", Nil, 0)
    check("end", Nil, 0)
  }

  it should "not select if single item when onKeypress(page-/down/up|end|home)" in {
    //given
    val items = List("one")
    val props = getComboBoxProps(items = items)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-down") shouldBe true
    findComponentProps(renderer.root, comboBoxPopup).items shouldBe items
    findComponentProps(renderer.root, comboBoxPopup).selected shouldBe 0

    def check(keyFull: String, items: List[String], selected: Int): Assertion = {
      findComponentProps(renderer.root, textInputComp).onKeypress(keyFull) shouldBe true
      inside(findComponentProps(renderer.root, comboBoxPopup)) { case popupProps =>
        popupProps.items shouldBe items
        popupProps.selected shouldBe selected
      }
    }

    //when & then
    check("up", items, 0)
    check("down", items, 0)
    check("pageup", items, 0)
    check("pagedown", items, 0)
    check("home", items, 0)
    check("end", items, 0)
  }

  it should "select if items < maxItems when onKeypress(page-/down/up|end|home)" in {
    //given
    val items = List("1", "2", "3", "4", "5")
    val props = getComboBoxProps(items = items)
    props.items.length should be < maxItems
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-down") shouldBe true
    findComponentProps(renderer.root, comboBoxPopup).items shouldBe items
    findComponentProps(renderer.root, comboBoxPopup).selected shouldBe 0

    def check(keyFull: String, items: List[String], selected: Int): Assertion = {
      findComponentProps(renderer.root, textInputComp).onKeypress(keyFull) shouldBe true
      inside(findComponentProps(renderer.root, comboBoxPopup)) { case popupProps =>
        popupProps.items shouldBe items
        popupProps.selected shouldBe selected
      }
    }

    //when & then
    check("up", items, 0)
    check("pageup", items, 0)
    check("home", items, 0)
    check("down", items, 1)
    check("down", items, 2)
    check("pagedown", items, 4)
    check("pageup", items, 0)
    check("end", items, 4)
    check("down", items, 4)
    check("pagedown", items, 4)
    check("up", items, 3)
    check("up", items, 2)
    check("home", items, 0)
  }

  it should "select if items > maxItems when onKeypress(page-/down/up|end|home)" in {
    //given
    val items = List("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
    val page1 = List("1", "2", "3", "4", "5", "6", "7", "8")
    val page2 = List("3", "4", "5", "6", "7", "8", "9", "10")
    val props = getComboBoxProps(items = items)
    props.items.length should be > maxItems
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-down") shouldBe true
    inside(findComponentProps(renderer.root, comboBoxPopup)) { case popupProps =>
      popupProps.items shouldBe page1
      popupProps.selected shouldBe 0
    }
    
    def check(keyFull: String, items: List[String], selected: Int): Assertion = {
      findComponentProps(renderer.root, textInputComp).onKeypress(keyFull) shouldBe true
      inside(findComponentProps(renderer.root, comboBoxPopup)) { case popupProps =>
        popupProps.items shouldBe items
        popupProps.selected shouldBe selected
      }
    }

    //when & then
    check("up", page1, 0)
    check("pageup", page1, 0)
    check("home", page1, 0)
    check("down", page1, 1)
    check("down", page1, 2)
    check("pagedown", page2, 2)
    check("pagedown", page2, 7)
    check("pageup", page1, 7)
    check("pageup", page1, 0)
    check("down", page1, 1)
    check("down", page1, 2)
    check("down", page1, 3)
    check("down", page1, 4)
    check("down", page1, 5)
    check("down", page1, 6)
    check("down", page1, 7)
    check("down", List("2", "3", "4", "5", "6", "7", "8", "9"), 7)
    check("down", page2, 7)
    check("down", page2, 7)
    check("end", page2, 7)
    check("pagedown", page2, 7)
    check("up", page2, 6)
    check("up", page2, 5)
    check("up", page2, 4)
    check("up", page2, 3)
    check("up", page2, 2)
    check("up", page2, 1)
    check("up", page2, 0)
    check("up", List("2", "3", "4", "5", "6", "7", "8", "9"), 0)
    check("up", page1, 0)
    check("up", page1, 0)
    check("end", page2, 7)
    check("home", page1, 0)
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
    textInput.onKeypress("pagedown") shouldBe false
    textInput.onKeypress("pageup") shouldBe false
    textInput.onKeypress("end") shouldBe false
    textInput.onKeypress("home") shouldBe false
    textInput.onKeypress("return") shouldBe false
    textInput.onKeypress("unknown") shouldBe false
  }

  it should "return true if popup is shown when onKeypress(unknown)" in {
    //given
    val props = getComboBoxProps()
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-up") shouldBe true
    findProps(renderer.root, comboBoxPopup) should not be empty
    val textInput = findComponentProps(renderer.root, textInputComp)

    //when & then
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
