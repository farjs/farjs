package farjs.ui

import farjs.ui.CheckBox._
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

class CheckBoxSpec extends TestSpec with TestRendererUtils {

  CheckBox.buttonComp = () => "Button".asInstanceOf[ReactClass]

  it should "call onChange when press button" in {
    //given
    val onChange = mockFunction[Unit]
    val props = getCheckBoxProps(onChange = onChange)
    val comp = testRender(<(CheckBox())(^.wrapped := props)())
    val button = findComponentProps(comp, buttonComp)

    //then
    onChange.expects()

    //when
    button.onPress()
  }

  it should "render checked component" in {
    //given
    val props = getCheckBoxProps(value = true)

    //when
    val result = createTestRenderer(<(CheckBox())(^.wrapped := props)()).root

    //then
    assertCheckBox(result, props)
  }
  
  it should "render un-checked component" in {
    //given
    val props = getCheckBoxProps()

    //when
    val result = createTestRenderer(<(CheckBox())(^.wrapped := props)()).root

    //then
    assertCheckBox(result, props)
  }

  private def getCheckBoxProps(value: Boolean = false,
                               onChange: () => Unit = () => ()
                              ): CheckBoxProps = CheckBoxProps(
    pos = (1, 2),
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
    val (left, top) = props.pos
    
    inside(result.children.toList) { case List(button, text) =>
      assertTestComponent(button, buttonComp) {
        case ButtonProps(resPos, label, resStyle, _) =>
          resPos shouldBe props.pos
          label shouldBe (if (props.value) "[x]" else "[ ]")
          resStyle shouldBe props.style
      }

      assertNativeComponent(text,
        <.text(
          ^.rbHeight := 1,
          ^.rbLeft := left + 4,
          ^.rbTop := top,
          ^.rbStyle := props.style,
          ^.content := props.label
        )()
      )
    }
  }
}
