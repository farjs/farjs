package farjs.ui

import farjs.ui.TextBox._
import scommons.react.test._

import scala.scalajs.js

class TextBoxSpec extends TestSpec with TestRendererUtils {

  TextBox.textInputComp = mockUiComponent("TextInput")

  it should "update state when stateUpdater" in {
    //given
    val props = getTextBoxProps()
    val renderer = createTestRenderer(<(TextBox())(^.plain := props)())
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
    val props = getTextBoxProps()

    //when
    val result = testRender(<(TextBox())(^.plain := props)())

    //then
    assertTestComponent(result, textInputComp) {
      case TextInputProps(left, top, width, value, state, _, onChange, onEnter) =>
        left shouldBe props.left
        top shouldBe props.top
        width shouldBe props.width
        value shouldBe props.value
        state shouldBe TextInputState()
        onChange shouldBe props.onChange
        onEnter shouldBe props.onEnter
    }
  }

  private def getTextBoxProps(value: String = "initial name",
                              onChange: String => Unit = _ => (),
                              onEnter: js.Function0[Unit] = () => ()
                             ): TextBoxProps = TextBoxProps(
    left = 1,
    top = 2,
    width = 10,
    value = value,
    onChange = onChange,
    onEnter = onEnter
  )
}
