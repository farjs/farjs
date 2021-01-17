package farjs.app.task

import farjs.app.task.TaskStatusPopup._
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.PopupProps
import org.scalatest._
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

class TaskStatusPopupSpec extends TestSpec with TestRendererUtils {

  TaskStatusPopup.popupComp = () => "Popup".asInstanceOf[ReactClass]
  TaskStatusPopup.doubleBorderComp = () => "DoubleBorder".asInstanceOf[ReactClass]
  TaskStatusPopup.textLineComp = () => "TextLine".asInstanceOf[ReactClass]
  
  it should "render component" in {
    //given
    val props = TaskStatusPopupProps(
      "Toooooooooooooooooooooooooooooo looooooooooooooooooooooooong test message"
    )

    //when
    val result = testRender(<(TaskStatusPopup())(^.wrapped := props)())

    //then
    assertTaskStatusPopup(result, props)
  }

  private def assertTaskStatusPopup(result: TestInstance,
                                    props: TaskStatusPopupProps): Unit = {
    
    val width = 35
    val textWidth = width - 8
    val textLines = UI.splitText(props.text, textWidth - 2) //exclude padding
    val height = 4 + textLines.size

    assertTestComponent(result, popupComp)({ case PopupProps(onClose, resClosable, focusable, _) =>
      onClose()
      resClosable shouldBe false
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
          ^.rbStyle := styles.text
        )(), { case List(border, btn) =>
          assertTestComponent(border, doubleBorderComp) {
            case DoubleBorderProps(resSize, style, pos, title) =>
              resSize shouldBe (width - 6) -> (height - 2)
              style shouldBe styles.text
              pos shouldBe 3 -> 1
              title shouldBe Some("Status")
          }
          
          assertNativeComponent(btn,
            <.button(
              ^.rbWidth := textWidth,
              ^.rbHeight := textLines.size,
              ^.rbLeft := 4,
              ^.rbTop := 2,
              ^.rbStyle := styles.text
            )(), { msgs =>
              msgs.size shouldBe textLines.size
              msgs.zip(textLines).zipWithIndex.foreach { case ((msg, textLine), index) =>
                assertTestComponent(msg, textLineComp) {
                  case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
                    align shouldBe TextLine.Center
                    pos shouldBe 0 -> index
                    resWidth shouldBe textWidth
                    text shouldBe textLine
                    style shouldBe styles.text
                    focused shouldBe false
                    padding shouldBe 1
                }
              }
              Succeeded
            }
          )
        }
      )
    })
  }
}
