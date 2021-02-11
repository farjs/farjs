package farjs.ui

import scommons.react.blessed._
import scommons.react.test._

class CheckBoxSpec extends TestSpec with TestRendererUtils {

  it should "call onChange when press button" in {
    //given
    val onChange = mockFunction[Unit]
    val props = getCheckBoxProps(onChange = onChange)
    val comp = testRender(<(CheckBox())(^.wrapped := props)())
    val button = findComponents(comp, "button").head

    //then
    onChange.expects()

    //when
    button.props.onPress()
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
    },
    onChange = onChange
  )

  private def assertCheckBox(result: TestInstance, props: CheckBoxProps): Unit = {
    val (left, top) = props.pos
    
    val List(button, text) = result.children.toList
    assertNativeComponent(button,
      <.button(
        ^.rbMouse := true,
        ^.rbWidth := 3,
        ^.rbHeight := 1,
        ^.rbLeft := left,
        ^.rbTop := top,
        ^.rbStyle := props.style,
        ^.content := {
          if (props.value) "[x]"
          else "[ ]"
        }
      )()
    )
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
