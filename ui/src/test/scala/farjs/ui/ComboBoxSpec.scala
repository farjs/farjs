package farjs.ui

import farjs.ui.ComboBox._
import scommons.react.test._

import scala.scalajs.js

class ComboBoxSpec extends TestSpec with TestRendererUtils {

  ComboBox.textInputComp = mockUiComponent("TextInput")
  ComboBox.comboBoxPopup = mockUiComponent("ComboBoxPopup")

  it should "call onChange and hide popup when onClick" in {
    //given
    val onChange = mockFunction[String, Unit]
    val props = getComboBoxProps(onChange = onChange)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-down") shouldBe true
    val comboBox = findComponentProps(renderer.root, comboBoxPopup)

    //then
    onChange.expects("item 2")

    //when
    comboBox.onClick(1)

    //then
    findProps(renderer.root, comboBoxPopup) should be (empty)
  }

  it should "call onChange and hide popup when onKeypress(return)" in {
    //given
    val onChange = mockFunction[String, Unit]
    val props = getComboBoxProps(onChange = onChange)
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-down") shouldBe true
    findProps(renderer.root, comboBoxPopup) should not be empty
    val textInput = findComponentProps(renderer.root, textInputComp)

    //then
    onChange.expects("item 1")

    //when
    textInput.onKeypress("return") shouldBe true

    //then
    findProps(renderer.root, comboBoxPopup) should be (empty)
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

  it should "show popup when onKeypress(C-down)" in {
    //given
    val props = getComboBoxProps()
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    val textInput = findComponentProps(renderer.root, textInputComp)
    findProps(renderer.root, comboBoxPopup) should be (empty)

    //when
    textInput.onKeypress("C-down") shouldBe true

    //then
    inside(findComponentProps(renderer.root, comboBoxPopup)) {
      case ComboBoxPopupProps(selected, items, top, left, width, _) =>
        selected shouldBe 0
        items shouldBe List("item 1", "item 2")
        top shouldBe props.top + 1
        left shouldBe props.left
        width shouldBe props.width
    }
  }

  it should "select items in popup when onKeypress(down|up)" in {
    //given
    val props = getComboBoxProps()
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    findComponentProps(renderer.root, textInputComp).onKeypress("C-down") shouldBe true
    findComponentProps(renderer.root, comboBoxPopup).selected shouldBe 0

    //when & then
    findComponentProps(renderer.root, textInputComp).onKeypress("up") shouldBe true
    findComponentProps(renderer.root, comboBoxPopup).selected shouldBe 0

    //when & then
    findComponentProps(renderer.root, textInputComp).onKeypress("down") shouldBe true
    findComponentProps(renderer.root, comboBoxPopup).selected shouldBe 1

    //when & then
    findComponentProps(renderer.root, textInputComp).onKeypress("down") shouldBe true
    findComponentProps(renderer.root, comboBoxPopup).selected shouldBe 1

    //when & then
    findComponentProps(renderer.root, textInputComp).onKeypress("up") shouldBe true
    findComponentProps(renderer.root, comboBoxPopup).selected shouldBe 0
  }

  it should "return false if popup not shown when onKeypress(escape|up|down|return)" in {
    //given
    val props = getComboBoxProps()
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    val textInput = findComponentProps(renderer.root, textInputComp)

    //when & then
    textInput.onKeypress("escape") shouldBe false
    textInput.onKeypress("up") shouldBe false
    textInput.onKeypress("down") shouldBe false
    textInput.onKeypress("return") shouldBe false
  }

  it should "return false when onKeypress(unknown)" in {
    //given
    val props = getComboBoxProps()
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    val textInput = findComponentProps(renderer.root, textInputComp)

    //when & then
    textInput.onKeypress("unknown") shouldBe false
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
    val result = testRender(<(ComboBox())(^.plain := props)())

    //then
    assertTestComponent(result, textInputComp) {
      case TextInputProps(left, top, width, value, state, _, onChange, onEnter, _) =>
        left shouldBe props.left
        top shouldBe props.top
        width shouldBe props.width
        value shouldBe props.value
        state shouldBe TextInputState()
        onChange shouldBe props.onChange
        onEnter shouldBe props.onEnter
    }
  }

  private def getComboBoxProps(value: String = "initial name",
                               onChange: String => Unit = _ => (),
                               onEnter: js.Function0[Unit] = () => ()
                              ): ComboBoxProps = ComboBoxProps(
    left = 1,
    top = 2,
    width = 10,
    value = value,
    onChange = onChange,
    onEnter = onEnter
  )
}
