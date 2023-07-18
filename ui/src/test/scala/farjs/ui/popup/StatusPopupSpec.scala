package farjs.ui.popup

import farjs.ui._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup.StatusPopup._
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import org.scalatest._
import scommons.nodejs.test.TestSpec
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class StatusPopupSpec extends TestSpec with BaseTestSpec with TestRendererUtils {

  StatusPopup.popupComp = "Popup".asInstanceOf[ReactClass]
  StatusPopup.modalContentComp = mockUiComponent("ModalContent")
  StatusPopup.textLineComp = "TextLine".asInstanceOf[ReactClass]

  private val theme = DefaultTheme.popup.regular
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
    val result = testRender(withThemeContext(<(StatusPopup())(^.wrapped := props)()))

    //then
    assertTaskStatusPopup(result, props)
  }

  private def assertTaskStatusPopup(result: TestInstance,
                                    props: StatusPopupProps): Unit = {
    
    val width = 35
    val textWidth = width - (paddingHorizontal + 2) * 2
    val textLines = UI.splitText(props.text, textWidth)
    val height = (paddingVertical + 1) * 2 + textLines.size

    assertNativeComponent(result, <(popupComp)(^.assertPlain[PopupProps](inside(_) {
      case PopupProps(onClose, focusable, _, _) =>
        onClose.isDefined shouldBe props.closable
        focusable shouldBe js.undefined
    }))(), inside(_) { case List(content) =>
      assertTestComponent(content, modalContentComp, plain = true)({
        case ModalContentProps(title, resWidth, resHeight, resStyle, padding, left, footer) =>
          title shouldBe props.title
          resWidth shouldBe width
          resHeight shouldBe height
          assertObject(resStyle, style)
          padding shouldBe js.undefined
          left shouldBe js.undefined
          footer shouldBe js.undefined
      }, inside(_) { case List(btn) =>
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
              assertNativeComponent(msg, <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
                case TextLineProps(align, left, top, resWidth, text, resStyle, focused, padding) =>
                  align shouldBe TextAlign.center
                  left shouldBe 0
                  top shouldBe index
                  resWidth shouldBe textWidth
                  text shouldBe textLine
                  assertObject(resStyle, style)
                  focused shouldBe js.undefined
                  padding shouldBe 0
              }))())
            }
            Succeeded
          }
        )
      })
    })
  }
}
