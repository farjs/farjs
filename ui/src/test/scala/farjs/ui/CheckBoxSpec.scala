package farjs.ui

import farjs.ui.CheckBox._
import scommons.react.blessed._
import scommons.react.test._

class CheckBoxSpec extends TestSpec with TestRendererUtils {

  CheckBox.buttonComp = mockUiComponent("Button")

  it should "call onChange when press button" in {
    //given
    val onChange = mockFunction[Unit]
    val props = getCheckBoxProps(onChange = onChange)
    val comp = testRender(<(CheckBox())(^.plain := props)())
    val button = findComponentProps(comp, buttonComp, plain = true)

    //then
    onChange.expects()

    //when
    button.onPress()
  }

  it should "render checked component" in {
    //given
    val props = getCheckBoxProps(value = true)

    //when
    val result = createTestRenderer(<(CheckBox())(^.plain := props)()).root

    //then
    assertCheckBox(result, props)
  }
  
  it should "render un-checked component" in {
    //given
    val props = getCheckBoxProps()

    //when
    val result = createTestRenderer(<(CheckBox())(^.plain := props)()).root

    //then
    assertCheckBox(result, props)
  }

  private def getCheckBoxProps(value: Boolean = false,
                               onChange: () => Unit = () => ()
                              ): CheckBoxProps = CheckBoxProps(
    left = 1,
    top = 2,
    value = value,
    label = "test item",
    style = new BlessedStyle {
      override val fg = "white"
      override val bg = "blue"
      override val focus = new BlessedStyle {
        override val fg = "cyan"
        override val bg = "black"
      }
    },
    onChange = onChange
  )

  private def assertCheckBox(result: TestInstance, props: CheckBoxProps): Unit = {
    inside(result.children.toList) { case List(button, text) =>
      assertTestComponent(button, buttonComp, plain = true)(inside(_) {
        case ButtonProps(resLeft, resTop, label, resStyle, _) =>
          resLeft shouldBe props.left
          resTop shouldBe props.top
          label shouldBe (if (props.value) "[x]" else "[ ]")
          resStyle shouldBe props.style
      })

      assertNativeComponent(text,
        <.text(
          ^.rbHeight := 1,
          ^.rbLeft := props.left + 4,
          ^.rbTop := props.top,
          ^.rbStyle := props.style,
          ^.content := props.label
        )()
      )
    }
  }
}
