package farclone.ui.popup

import org.scalatest.Assertion
import farclone.ui._
import farclone.ui.border._
import farclone.ui.popup.MessageBox.splitText
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

class MessageBoxSpec extends TestSpec with ShallowRendererUtils {

  it should "call onClose when onPress Ok button" in {
    //given
    val onClose = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", onClose = onClose)
    val comp = shallowRender(<(MessageBox())(^.wrapped := props)())
    val okButton = findComponents(comp, "button").head

    //then
    onClose.expects()
    
    //when
    okButton.props.onPress()
  }
  
  it should "render component" in {
    //given
    val props = MessageBoxProps(
      title = "test title",
      message = "Toooooooooooooooooooooooooooooo looooooooooooooooooooooooong test message"
    )

    //when
    val result = shallowRender(<(MessageBox())(^.wrapped := props)())

    //then
    assertMessageBox(result, props)
  }
  
  it should "split text when splitText" in {
    //when & then
    splitText("", 2) shouldBe List("")
    splitText("test", 2) shouldBe List("test")
    splitText("test1, test2", 11) shouldBe List("test1,", "test2")
    splitText("test1, test2", 12) shouldBe List("test1, test2")
    splitText("test1, test2, test3", 12) shouldBe List("test1,", "test2, test3")
    splitText("test1, test2, test3", 13) shouldBe List("test1, test2,", "test3")
  }
  
  private def assertMessageBox(result: ShallowInstance, props: MessageBoxProps): Unit = {
    val (width, height) = (60, 7)
    
    def assertComponents(border: ShallowInstance,
                         msg1: ShallowInstance,
                         msg2: ShallowInstance,
                         btn: ShallowInstance): Assertion = {

      assertComponent(border, DoubleBorder) {
        case DoubleBorderProps(resSize, style, pos, title) =>
          resSize shouldBe (width - 6) -> (height - 2)
          style shouldBe props.style
          pos shouldBe 3 -> 1
          title shouldBe Some(props.title)
      }
      
      msg1.key shouldBe "0"
      assertComponent(msg1, TextLine) {
        case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
          align shouldBe TextLine.Center
          pos shouldBe 4 -> 2
          resWidth shouldBe (width - 8)
          text shouldBe "Toooooooooooooooooooooooooooooo"
          style shouldBe props.style
          focused shouldBe false
          padding shouldBe 1
      }
      msg2.key shouldBe "1"
      assertComponent(msg2, TextLine) {
        case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
          align shouldBe TextLine.Center
          pos shouldBe 4 -> 3
          resWidth shouldBe (width - 8)
          text shouldBe "looooooooooooooooooooooooong test message"
          style shouldBe props.style
          focused shouldBe false
          padding shouldBe 1
      }
      
      assertNativeComponent(btn,
        <.button(
          ^.rbMouse := true,
          ^.rbWidth := 4,
          ^.rbHeight := 1,
          ^.rbTop := height - 3,
          ^.rbLeft := "center",
          ^.rbStyle := props.style,
          ^.content := " OK "
        )()
      )
    }
    
    assertComponent(result, Popup)({ case PopupProps(onClose, closable, focusable, _) =>
      onClose shouldBe props.onClose
      closable shouldBe true
      focusable shouldBe true
    }, { case List(box) =>
      assertNativeComponent(box,
        <.box(
          ^.rbClickable := true,
          ^.rbAutoFocus := false,
          ^.rbWidth := width,
          ^.rbHeight := height,
          ^.rbTop := "center",
          ^.rbLeft := "center",
          ^.rbShadow := true,
          ^.rbStyle := props.style
        )(), {
          case List(border, msg1, msg2, btn) => assertComponents(border, msg1, msg2, btn)
        }
      )
    })
  }
}
