package farjs.ui

import scommons.react.blessed._
import scommons.react.test._

class ButtonSpec extends TestSpec with TestRendererUtils {

  it should "call onPress when press button" in {
    //given
    val onPress = mockFunction[Unit]
    val props = getButtonProps(onPress = onPress)
    val comp = testRender(<(Button())(^.wrapped := props)())
    val button = findComponents(comp, <.button.name).head

    //then
    onPress.expects()

    //when
    button.props.onPress()
  }

  it should "change focused state when onFocus/onBlur" in {
    //given
    val onPress = mockFunction[Unit]
    val props = getButtonProps(onPress = onPress)
    val renderer = createTestRenderer(<(Button())(^.wrapped := props)())
    val button = findComponents(renderer.root, <.button.name).head
    assertButton(renderer.root, props, focused = false)

    //then
    onPress.expects().never()

    //when & then
    button.props.onFocus()
    assertButton(renderer.root, props, focused = true)

    //when & then
    button.props.onBlur()
    assertButton(renderer.root, props, focused = false)
  }

  it should "render component" in {
    //given
    val props = getButtonProps()

    //when
    val result = createTestRenderer(<(Button())(^.wrapped := props)()).root

    //then
    assertButton(result, props, focused = false)
  }
  
  private def getButtonProps(onPress: () => Unit = () => ()): ButtonProps = ButtonProps(
    left = 1,
    top = 2,
    label = "test button",
    style = new BlessedStyle {
      override val fg = "white"
      override val bg = "blue"
      override val focus = new BlessedStyle {
        override val fg = "cyan"
        override val bg = "black"
      }
    },
    onPress = onPress
  )

  private def assertButton(result: TestInstance, props: ButtonProps, focused: Boolean): Unit = {
    inside(result.children.toList) { case List(button) =>
      assertNativeComponent(button,
        <.button(
          ^.rbMouse := true,
          ^.rbTags := true,
          ^.rbWrap := false,
          ^.rbWidth := props.label.length,
          ^.rbHeight := 1,
          ^.rbLeft := props.left,
          ^.rbTop := props.top,
          ^.content := {
            val style =
              if (focused) props.style.focus.orNull
              else props.style

            TextBox.renderText(style, props.label)
          }
        )()
      )
    }
  }
}
