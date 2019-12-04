package farclone.ui

import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

class TextBoxSpec extends TestSpec with ShallowRendererUtils {

  it should "render component" in {
    //given
    val props = getTextBoxProps()

    //when
    val result = shallowRender(<(TextBox())(^.wrapped := props)())

    //then
    assertTextBox(result, props)
  }

  private def getTextBoxProps(value: String = "initial name",
                              onChange: String => Unit = _ => ()
                             ): TextBoxProps = TextBoxProps(
    pos = (1, 2),
    width = 10,
    value = value,
    style = new BlessedStyle {
      override val bg = "cyan"
      override val fg = "black"
    },
    onChange = onChange
  )

  private def assertTextBox(result: ShallowInstance, props: TextBoxProps): Unit = {
    val (left, top) = props.pos
    
    assertNativeComponent(result,
      <.input(
        ^.rbMouse := true,
        ^.rbKeyable := true,
        ^.rbWidth := props.width,
        ^.rbHeight := 1,
        ^.rbLeft := left,
        ^.rbTop := top,
        ^.rbStyle := props.style,
        ^.content := props.value
      )()
    )
  }
}
