package farjs.ui.popup

import farjs.ui._
import farjs.ui.border._
import farjs.ui.theme.Theme
import org.scalatest.{Assertion, Succeeded}
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

class MessageBoxSpec extends TestSpec with ShallowRendererUtils {

  "OK popup" should "call OK action when onClose popup" in {
    //given
    val onAction = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", List(
      MessageBoxAction.OK(onAction)
    ), Theme.current.popup.regular)
    val comp = shallowRender(<(MessageBox())(^.wrapped := props)())
    val popup = findComponentProps(comp, Popup)

    //then
    onAction.expects()
    
    //when
    popup.onClose()
  }
  
  it should "call OK action when onPress OK button" in {
    //given
    val onAction = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", List(
      MessageBoxAction.OK(onAction)
    ), Theme.current.popup.regular)
    val comp = shallowRender(<(MessageBox())(^.wrapped := props)())
    val okButton = findComponents(comp, "button").head

    //then
    onAction.expects()
    
    //when
    okButton.props.onPress()
  }
  
  it should "render component" in {
    //given
    val props = MessageBoxProps(
      title = "test title",
      message = "Toooooooooooooooooooooooooooooo looooooooooooooooooooooooong test message",
      actions = List(MessageBoxAction.OK(() => ())),
      style = Theme.current.popup.regular
    )

    //when
    val result = shallowRender(<(MessageBox())(^.wrapped := props)())

    //then
    assertMessageBox(result, props, List("OK" -> 0))
  }
  
  "YES/NO popup" should "call NO action when onClose popup" in {
    //given
    val onYesAction = mockFunction[Unit]
    val onNoAction = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", List(
      MessageBoxAction.YES(onYesAction),
      MessageBoxAction.NO(onNoAction)
    ), Theme.current.popup.regular)
    val comp = shallowRender(<(MessageBox())(^.wrapped := props)())
    val popup = findComponentProps(comp, Popup)

    //then
    onYesAction.expects().never()
    onNoAction.expects()
    
    //when
    popup.onClose()
  }
  
  it should "call YES action when onPress YES button" in {
    //given
    val onYesAction = mockFunction[Unit]
    val onNoAction = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", List(
      MessageBoxAction.YES(onYesAction),
      MessageBoxAction.NO(onNoAction)
    ), Theme.current.popup.regular)
    val comp = shallowRender(<(MessageBox())(^.wrapped := props)())
    val yesButton = findComponents(comp, "button").head

    //then
    onYesAction.expects()
    onNoAction.expects().never()
    
    //when
    yesButton.props.onPress()
  }
  
  it should "call NO action when onPress NO button" in {
    //given
    val onYesAction = mockFunction[Unit]
    val onNoAction = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", List(
      MessageBoxAction.YES(onYesAction),
      MessageBoxAction.NO(onNoAction)
    ), Theme.current.popup.regular)
    val comp = shallowRender(<(MessageBox())(^.wrapped := props)())
    val noButton = findComponents(comp, "button")(1)

    //then
    onYesAction.expects().never()
    onNoAction.expects()
    
    //when
    noButton.props.onPress()
  }
  
  it should "render component" in {
    //given
    val props = MessageBoxProps("test title", "test message", List(
      MessageBoxAction.YES(() => ()),
      MessageBoxAction.NO(() => ())
    ), Theme.current.popup.regular)

    //when
    val result = shallowRender(<(MessageBox())(^.wrapped := props)())

    //then
    assertMessageBox(result, props, List("YES" -> 0, "NO" -> 5))
  }

  "YES/NO non-closable popup" should "do nothing when onClose popup" in {
    //given
    val onYesAction = mockFunction[Unit]
    val onNoAction = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", List(
      MessageBoxAction.YES(onYesAction),
      MessageBoxAction.NO(onNoAction).copy(triggeredOnClose = false)
    ), Theme.current.popup.regular)
    val comp = shallowRender(<(MessageBox())(^.wrapped := props)())
    val popup = findComponentProps(comp, Popup)

    //then
    onYesAction.expects().never()
    onNoAction.expects().never()

    //when
    popup.onClose()
  }

  it should "render component" in {
    //given
    val props = MessageBoxProps("test title", "test message", List(
      MessageBoxAction.YES(() => ()),
      MessageBoxAction.NO(() => ()).copy(triggeredOnClose = false)
    ), Theme.current.popup.regular)

    //when
    val result = shallowRender(<(MessageBox())(^.wrapped := props)())

    //then
    assertMessageBox(result, props, List("YES" -> 0, "NO" -> 5), closable = false)
  }

  private def assertMessageBox(result: ShallowInstance,
                               props: MessageBoxProps,
                               actions: List[(String, Int)],
                               closable: Boolean = true): Unit = {
    val width = 60
    val textWidth = width - 8
    val textLines = UI.splitText(props.message, textWidth - 2) //exclude padding
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
      
      val buttonsWidth = actions.map(_._1.length + 2).sum
      assertNativeComponent(actionsBox,
        <.box(
          ^.rbWidth := buttonsWidth,
          ^.rbHeight := 1,
          ^.rbTop := height - 3,
          ^.rbLeft := "center",
          ^.rbStyle := props.style
        )(), { buttons: List[ShallowInstance] =>
          buttons.size shouldBe actions.size
          buttons.zip(actions).foreach { case (btn, (label, pos)) =>
            assertNativeComponent(btn,
              <.button(
                ^.key := s"$pos",
                ^.rbMouse := true,
                ^.rbHeight := 1,
                ^.rbLeft := pos,
                ^.rbStyle := props.style,
                ^.content := s" $label "
              )()
            )
          }
          Succeeded
        }
      )
    }
    
    assertComponent(result, Popup)({ case PopupProps(_, resClosable, focusable, _) =>
      resClosable shouldBe closable
      focusable shouldBe true
    }, inside(_) { case List(box) =>
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
        )(), inside(_) {
          case List(border, msg, actionsBox) if textLines.size == 1 =>
            assertComponents(border, List(msg), actionsBox)
          case List(border, msg1, msg2, actionsBox) if textLines.size == 2 =>
            assertComponents(border, List(msg1, msg2), actionsBox)
        }
      )
    })
  }
}
