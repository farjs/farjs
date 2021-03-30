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

  it should "change focused state when onFocus/onBlur" in {
    //given
    val onChange = mockFunction[Unit]
    val props = getCheckBoxProps(onChange = onChange)
    val renderer = createTestRenderer(<(CheckBox())(^.wrapped := props)())
    val button = findComponents(renderer.root, "button").head
    assertCheckBox(renderer.root, props, focused = false)

    //then
    onChange.expects().never()

    //when & then
    button.props.onFocus()
    assertCheckBox(renderer.root, props, focused = true)

    //when & then
    button.props.onBlur()
    assertCheckBox(renderer.root, props, focused = false)
  }

  it should "render checked component" in {
    //given
    val props = getCheckBoxProps(value = true)

    //when
    val result = createTestRenderer(<(CheckBox())(^.wrapped := props)()).root

    //then
    assertCheckBox(result, props, focused = false)
  }
  
  it should "render un-checked component" in {
    //given
    val props = getCheckBoxProps()

    //when
    val result = createTestRenderer(<(CheckBox())(^.wrapped := props)()).root

    //then
    assertCheckBox(result, props, focused = false)
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

  private def assertCheckBox(result: TestInstance, props: CheckBoxProps, focused: Boolean): Unit = {
    val (left, top) = props.pos
    
    val List(button, text) = result.children.toList
    assertNativeComponent(button,
      <.button(
        ^.rbMouse := true,
        ^.rbTags := true,
        ^.rbWidth := 4,
        ^.rbHeight := 1,
        ^.rbLeft := left,
        ^.rbTop := top,
        ^.content := {
          val style =
            if (focused) props.style.focus.orNull
            else props.style

          TextBox.renderText(style, if (props.value) "[x]" else "[ ]")
        }
      )()
    )
    assertNativeComponent(text,
      <.text(
        ^.rbHeight := 1,
        ^.rbLeft := left + 3,
        ^.rbTop := top,
        ^.rbStyle := props.style,
        ^.content := s" ${props.label}"
      )()
    )
  }
}
