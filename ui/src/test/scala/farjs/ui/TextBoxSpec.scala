package farjs.ui

import farjs.ui.TextBox._
import org.scalactic.source.Position
import org.scalatest.Assertion
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
    textInput.stateUpdater(TextInputState.copy(_)(
      offset = 1,
      cursorX = 2,
      selStart = 3,
      selEnd = 4
    ))

    //then
    assertTextInputState(findComponentProps(renderer.root, textInputComp).state, TextInputState(
      offset = 1,
      cursorX = 2,
      selStart = 3,
      selEnd = 4
    ))
  }

  it should "render component" in {
    //given
    val props = getTextBoxProps()

    //when
    val result = testRender(<(TextBox())(^.plain := props)())

    //then
    assertTestComponent(result, textInputComp) {
      case TextInputProps(_, left, top, width, value, state, _, onChange, onEnter, _) =>
        left shouldBe props.left
        top shouldBe props.top
        width shouldBe props.width
        value shouldBe props.value
        assertTextInputState(state, TextInputState())
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

  private def assertTextInputState(result: TextInputState,
                                   expected: TextInputState
                                  )(implicit position: Position): Assertion = {
    inside(result) {
      case TextInputState(offset, cursorX, selStart, selEnd) =>
        offset shouldBe expected.offset
        cursorX shouldBe expected.cursorX
        selStart shouldBe expected.selStart
        selEnd shouldBe expected.selEnd
    }
  }
}
