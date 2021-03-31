package farjs.ui.popup

import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.MessageBox._
import farjs.ui.theme.Theme
import org.scalatest.{Assertion, Succeeded}
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

class MessageBoxSpec extends TestSpec with TestRendererUtils {

  MessageBox.popupComp = () => "Popup".asInstanceOf[ReactClass]
  MessageBox.doubleBorderComp = () => "DoubleBorder".asInstanceOf[ReactClass]
  MessageBox.textLineComp = () => "TextLine".asInstanceOf[ReactClass]
  MessageBox.buttonsPanelComp = () => "ButtonsPanel".asInstanceOf[ReactClass]

  "OK popup" should "call OK action when onClose popup" in {
    //given
    val onAction = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", List(
      MessageBoxAction.OK(onAction)
    ), Theme.current.popup.regular)
    val comp = testRender(<(MessageBox())(^.wrapped := props)())
    val popup = findComponentProps(comp, popupComp)

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
    val comp = testRender(<(MessageBox())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions.head

    //then
    onAction.expects()
    
    //when
    onPress()
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
    val result = testRender(<(MessageBox())(^.wrapped := props)())

    //then
    assertMessageBox(result, props, List("OK"))
  }
  
  "YES/NO popup" should "call NO action when onClose popup" in {
    //given
    val onYesAction = mockFunction[Unit]
    val onNoAction = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", List(
      MessageBoxAction.YES(onYesAction),
      MessageBoxAction.NO(onNoAction)
    ), Theme.current.popup.regular)
    val comp = testRender(<(MessageBox())(^.wrapped := props)())
    val popup = findComponentProps(comp, popupComp)

    //then
    onYesAction.expects().never()
    onNoAction.expects()
    
    //when
    popup.onClose()

    Succeeded
  }
  
  it should "call YES action when onPress YES button" in {
    //given
    val onYesAction = mockFunction[Unit]
    val onNoAction = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", List(
      MessageBoxAction.YES(onYesAction),
      MessageBoxAction.NO(onNoAction)
    ), Theme.current.popup.regular)
    val comp = testRender(<(MessageBox())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions.head

    //then
    onYesAction.expects()
    onNoAction.expects().never()
    
    //when
    onPress()
  }
  
  it should "call NO action when onPress NO button" in {
    //given
    val onYesAction = mockFunction[Unit]
    val onNoAction = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", List(
      MessageBoxAction.YES(onYesAction),
      MessageBoxAction.NO(onNoAction)
    ), Theme.current.popup.regular)
    val comp = testRender(<(MessageBox())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions(1)

    //then
    onNoAction.expects()
    onYesAction.expects().never()
    
    //when
    onPress()
  }
  
  it should "render component" in {
    //given
    val props = MessageBoxProps("test title", "test message", List(
      MessageBoxAction.YES(() => ()),
      MessageBoxAction.NO(() => ())
    ), Theme.current.popup.regular)

    //when
    val result = testRender(<(MessageBox())(^.wrapped := props)())

    //then
    assertMessageBox(result, props, List("YES", "NO"))
  }

  "YES/NO non-closable popup" should "do nothing when onClose popup" in {
    //given
    val onYesAction = mockFunction[Unit]
    val onNoAction = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", List(
      MessageBoxAction.YES(onYesAction),
      MessageBoxAction.NO(onNoAction).copy(triggeredOnClose = false)
    ), Theme.current.popup.regular)
    val comp = testRender(<(MessageBox())(^.wrapped := props)())
    val popup = findComponentProps(comp, popupComp)

    //then
    onYesAction.expects().never()
    onNoAction.expects().never()

    //when
    popup.onClose()

    Succeeded
  }

  it should "render component" in {
    //given
    val props = MessageBoxProps("test title", "test message", List(
      MessageBoxAction.YES(() => ()),
      MessageBoxAction.NO(() => ()).copy(triggeredOnClose = false)
    ), Theme.current.popup.regular)

    //when
    val result = testRender(<(MessageBox())(^.wrapped := props)())

    //then
    assertMessageBox(result, props, List("YES", "NO"), closable = false)
  }

  private def assertMessageBox(result: TestInstance,
                               props: MessageBoxProps,
                               actions: List[String],
                               closable: Boolean = true): Assertion = {
    val width = 60
    val textWidth = width - 8
    val textLines = UI.splitText(props.message, textWidth - 2) //exclude padding
    val height = 5 + textLines.size
    
    def assertComponents(border: TestInstance,
                         msgs: List[TestInstance],
                         actionsBox: TestInstance): Assertion = {

      assertTestComponent(border, doubleBorderComp) {
        case DoubleBorderProps(resSize, style, pos, title) =>
          resSize shouldBe (width - 6) -> (height - 2)
          style shouldBe props.style
          pos shouldBe 3 -> 1
          title shouldBe Some(props.title)
      }
      
      msgs.size shouldBe textLines.size
      msgs.zip(textLines).zipWithIndex.foreach { case ((msg, textLine), index) =>
        assertTestComponent(msg, textLineComp) {
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
      
      assertTestComponent(actionsBox, buttonsPanelComp) {
        case ButtonsPanelProps(top, resActions, resStyle, padding, margin) =>
          top shouldBe (height - 3)
          resActions.map(_._1) shouldBe actions
          resStyle shouldBe props.style
          padding shouldBe 1
          margin shouldBe 0
      }
    }
    
    assertTestComponent(result, popupComp)({ case PopupProps(_, resClosable, focusable, _) =>
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
