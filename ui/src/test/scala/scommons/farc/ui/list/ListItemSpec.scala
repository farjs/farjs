package scommons.farc.ui.list

import scommons.farc.ui.list.ListItemSpec._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

class ListItemSpec extends TestSpec
  with ShallowRendererUtils
  with TestRendererUtils {

  it should "call onFocus callback when onFocus" in {
    //given
    val onFocus = mockFunction[Unit]
    val props = ListItemProps(
      pos = 1,
      style = new BlessedStyle {},
      text = "test item",
      focused = true,
      onFocus = onFocus,
      onKeyPress = (_, _) => ()
    )
    val comp = shallowRender(<(ListItem())(^.wrapped := props)())
    
    //then
    onFocus.expects()
    
    //when
    comp.props.onFocus()
  }
  
  it should "call onKeyPress callback when onKeypress" in {
    //given
    val onKeyPress = mockFunction[BlessedElement, KeyboardKey, Unit]
    val keyMock = mock[KeyboardKeyMock]
    val props = ListItemProps(
      pos = 1,
      style = new BlessedStyle {},
      text = "test item",
      focused = true,
      onFocus = () => (),
      onKeyPress = onKeyPress
    )
    val buttonMock = mock[BlessedElementMock]
    val buttonElement = buttonMock.asInstanceOf[BlessedElement]
    val comp = testRender(<(ListItem())(^.wrapped := props)(), { el =>
      if (el.`type` == "button".asInstanceOf[js.Any]) {
        buttonElement
      }
      else null
    })
    val key = keyMock.asInstanceOf[KeyboardKey]
    
    //then
    onKeyPress.expects(buttonElement, key)
    
    //when
    comp.props.onKeypress(null, key)
  }
  
  it should "render component" in {
    //given
    val props = ListItemProps(
      pos = 1,
      style = new BlessedStyle {},
      text = "test item",
      focused = true,
      onFocus = () => (),
      onKeyPress = (_, _) => ()
    )
    val comp = <(ListItem())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertNativeComponent(result,
      <.button(
        ^.rbTop := props.pos,
        ^.rbHeight := 1,
        ^.rbStyle := props.style,
        ^.rbMouse := true,
        ^.content := props.text
      )()
    )
  }
}

object ListItemSpec {

  @JSExportAll
  trait KeyboardKeyMock {

    def full: String
  }

  @JSExportAll
  trait BlessedElementMock {

    def width: Int
    def height: Int
  }
}
