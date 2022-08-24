package farjs.ui

import farjs.ui.ComboBox._
import scommons.react.test._

import scala.scalajs.js

class ComboBoxSpec extends TestSpec with TestRendererUtils {

  ComboBox.textInputComp = mockUiComponent("TextInput")

  it should "return false if unknown key when onKeypress" in {
    //given
    val props = getComboBoxProps()
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    val textInput = findComponentProps(renderer.root, textInputComp)

    //when & then
    textInput.onKeypress("unknown") shouldBe false
  }

  it should "return true if C-down key when onKeypress" in {
    //given
    val props = getComboBoxProps()
    val renderer = createTestRenderer(<(ComboBox())(^.plain := props)())
    val textInput = findComponentProps(renderer.root, textInputComp)

    //when & then
    textInput.onKeypress("C-down") shouldBe true
  }

  it should "update state when stateUpdater" in {
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

  it should "render component" in {
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
