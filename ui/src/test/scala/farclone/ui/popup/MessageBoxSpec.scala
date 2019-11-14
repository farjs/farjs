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

  it should "call onAction when onPress button" in {
    //given
    val onClose = mockFunction[Unit]
    val props = MessageBoxProps(
      title = "test title",
      message = "test message",
      actions = List(MessageBoxAction.OK(onClose))
    )
    val comp = shallowRender(<(MessageBox())(^.wrapped := props)())
    val okButton = findComponents(comp, "button").head

    //then
    onClose.expects()
    
    //when
    okButton.props.onPress()
  }
  
  it should "render OK popup" in {
    //given
    val props = MessageBoxProps(
      title = "test title",
      message = "Toooooooooooooooooooooooooooooo looooooooooooooooooooooooong test message",
      actions = List(MessageBoxAction.OK(() => ()))
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
    val width = 60
    val textWidth = width - 8
    val textLines = splitText(props.message, textWidth - 2) //exclude padding
    val height = 5 + textLines.size
    
    def assertComponents(border: ShallowInstance,
                         msgs: List[ShallowInstance],
                         actionsBox: ShallowInstance): Assertion = {

      assertComponent(border, DoubleBorder) {
        case DoubleBorderProps(resSize, style, pos, title) =>
          resSize shouldBe (width - 6) -> (height - 2)
          style shouldBe props.style
          pos shouldBe 3 -> 1
          title shouldBe Some(props.title)
      }
      
      msgs.size shouldBe textLines.size
      msgs.zip(textLines).zipWithIndex.foreach { case ((msg, textLine), index) =>
        msg.key shouldBe s"$index"
        assertComponent(msg, TextLine) {
          case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
            align shouldBe TextLine.Center
            pos shouldBe 4 -> (2 + index)
            resWidth shouldBe (width - 8)
            text shouldBe textLine
            style shouldBe props.style
            focused shouldBe false
            padding shouldBe 1
        }
      }
      
      assertNativeComponent(actionsBox,
        <.box(
          ^.rbWidth := 4,
          ^.rbHeight := 1,
          ^.rbTop := height - 3,
          ^.rbLeft := "center",
          ^.rbStyle := props.style
        )(), { case List(okBtn) =>
          assertNativeComponent(okBtn,
            <.button(
              ^.key := "0",
              ^.rbMouse := true,
              ^.rbWidth := 4,
              ^.rbHeight := 1,
              ^.rbLeft := 0,
              ^.rbStyle := props.style,
              ^.content := " OK "
            )()
          )
        }
      )
    }
    
    assertComponent(result, Popup)({ case PopupProps(_, closable, focusable, _) =>
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
          inside(_) {
            case List(border, msg, actionsBox) if textLines.size == 1 =>
              assertComponents(border, List(msg), actionsBox)
            case List(border, msg1, msg2, actionsBox) if textLines.size == 2 =>
              assertComponents(border, List(msg1, msg2), actionsBox)
          }
        }
      )
    })
  }
}
