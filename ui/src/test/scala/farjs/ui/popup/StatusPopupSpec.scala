package farjs.ui.popup

import farjs.ui._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup.StatusPopup._
import farjs.ui.theme.Theme
import org.scalatest._
import scommons.nodejs.test.TestSpec
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

class StatusPopupSpec extends TestSpec with BaseTestSpec with TestRendererUtils {

  StatusPopup.popupComp = () => "Popup".asInstanceOf[ReactClass]
  StatusPopup.modalContentComp = () => "ModalContent".asInstanceOf[ReactClass]
  StatusPopup.textLineComp = () => "TextLine".asInstanceOf[ReactClass]

  private val theme = Theme.current.popup.regular
  private val style = new BlessedStyle {
    override val bold = theme.bold
    override val bg = theme.bg
    override val fg = theme.fg
  }
  
  it should "render component" in {
    //given
    val props = StatusPopupProps(
      text = "Toooooooooooooooooooooooooooooo looooooooooooooooooooooooong test message",
      title = "Test Title",
      closable = true,
      onClose = () => ()
    )

    //when
    val result = testRender(<(StatusPopup())(^.wrapped := props)())

    //then
    assertTaskStatusPopup(result, props)
  }

  private def assertTaskStatusPopup(result: TestInstance,
                                    props: StatusPopupProps): Unit = {
    
    val width = 35
    val textWidth = width - (paddingHorizontal + 2) * 2
    val textLines = UI.splitText(props.text, textWidth)
    val height = (paddingVertical + 1) * 2 + textLines.size

    assertTestComponent(result, popupComp)({ case PopupProps(onClose, resClosable, focusable, _) =>
      onClose should be theSameInstanceAs props.onClose
      resClosable shouldBe props.closable
      focusable shouldBe true
    }, { case List(content) =>
      assertTestComponent(content, modalContentComp)({
        case ModalContentProps(title, size, resStyle, padding) =>
          title shouldBe props.title
          size shouldBe width -> height
          assertObject(resStyle, style)
          padding shouldBe ModalContent.padding
      }, { case List(btn) =>
        assertNativeComponent(btn,
          <.button(
            ^.rbWidth := textWidth,
            ^.rbHeight := textLines.size,
            ^.rbLeft := 2,
            ^.rbTop := 1,
            ^.rbStyle := style
          )(), { msgs =>
            msgs.size shouldBe textLines.size
            msgs.zip(textLines).zipWithIndex.foreach { case ((msg, textLine), index) =>
              assertTestComponent(msg, textLineComp) {
                case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
                  align shouldBe TextLine.Center
                  pos shouldBe 0 -> index
                  resWidth shouldBe textWidth
                  text shouldBe textLine
                  assertObject(resStyle, style)
                  focused shouldBe false
                  padding shouldBe 0
              }
            }
            Succeeded
          }
        )
      })
    })
  }
}
