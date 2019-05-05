package scommons.farc.ui.list

import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.util.ShallowRendererUtils

class ListItemSpec extends TestSpec with ShallowRendererUtils {

  it should "render not focused component" in {
    //given
    val props = ListItemProps(
      top = 1,
      style = new BlessedStyle {},
      text = "test item",
      focused = false
    )
    val comp = <(ListItem())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertNativeComponent(result,
      <.box(
        ^.rbTop := props.top,
        ^.rbHeight := 1,
        ^.rbStyle := props.style,
        ^.content := props.text
      )()
    )
  }
  
  it should "render focused component" in {
    //given
    val props = ListItemProps(
      top = 1,
      style = new BlessedStyle {
        override val focus = new BlessedStyle {}
      },
      text = "test item",
      focused = true
    )
    val comp = <(ListItem())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertNativeComponent(result,
      <.box(
        ^.rbTop := props.top,
        ^.rbHeight := 1,
        ^.rbStyle := props.style.focus,
        ^.content := props.text
      )()
    )
  }
}
