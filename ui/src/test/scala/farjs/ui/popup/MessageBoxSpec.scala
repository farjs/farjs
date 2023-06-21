package farjs.ui.popup

import farjs.ui._
import farjs.ui.popup.MessageBox._
import farjs.ui.popup.ModalContent._
import farjs.ui.theme.DefaultTheme
import org.scalatest.{Assertion, Succeeded}
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js

class MessageBoxSpec extends TestSpec with TestRendererUtils {

  MessageBox.popupComp = mockUiComponent("Popup")
  MessageBox.modalContentComp = mockUiComponent("ModalContent")
  MessageBox.textLineComp = mockUiComponent("TextLine")
  MessageBox.buttonsPanelComp = "ButtonsPanel".asInstanceOf[ReactClass]

  "OK popup" should "call OK action when onClose popup" in {
    //given
    val onAction = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", js.Array(
      MessageBoxAction.OK(onAction)
    ), DefaultTheme.popup.regular)
    val comp = testRender(<(MessageBox())(^.plain := props)())
    val popup = findComponentProps(comp, popupComp)

    //then
    onAction.expects()
    
    //when
    popup.onClose()
  }
  
  it should "call OK action when onPress OK button" in {
    //given
    val onAction = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", js.Array(
      MessageBoxAction.OK(onAction)
    ), DefaultTheme.popup.regular)
    val comp = testRender(<(MessageBox())(^.plain := props)())
    val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    val action = buttonsProps.actions.head

    //then
    onAction.expects()
    
    //when
    action.onAction()
  }
  
  it should "render component" in {
    //given
    val props = MessageBoxProps(
      title = "test title",
      message = "Toooooooooooooooooooooooooooooo looooooooooooooooooooooooong test message",
      actions = js.Array(MessageBoxAction.OK(() => ())),
      style = DefaultTheme.popup.regular
    )

    //when
    val result = testRender(<(MessageBox())(^.plain := props)())

    //then
    assertMessageBox(result, props, List("OK"))
  }
  
  "YES/NO popup" should "call NO action when onClose popup" in {
    //given
    val onYesAction = mockFunction[Unit]
    val onNoAction = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", js.Array(
      MessageBoxAction.YES(onYesAction),
      MessageBoxAction.NO(onNoAction)
    ), DefaultTheme.popup.regular)
    val comp = testRender(<(MessageBox())(^.plain := props)())
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
    val props = MessageBoxProps("test title", "test message", js.Array(
      MessageBoxAction.YES(onYesAction),
      MessageBoxAction.NO(onNoAction)
    ), DefaultTheme.popup.regular)
    val comp = testRender(<(MessageBox())(^.plain := props)())
    val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    val action = buttonsProps.actions.head

    //then
    onYesAction.expects()
    onNoAction.expects().never()
    
    //when
    action.onAction()
  }
  
  it should "call NO action when onPress NO button" in {
    //given
    val onYesAction = mockFunction[Unit]
    val onNoAction = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", js.Array(
      MessageBoxAction.YES(onYesAction),
      MessageBoxAction.NO(onNoAction)
    ), DefaultTheme.popup.regular)
    val comp = testRender(<(MessageBox())(^.plain := props)())
    val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    val action = buttonsProps.actions(1)

    //then
    onNoAction.expects()
    onYesAction.expects().never()
    
    //when
    action.onAction()
  }
  
  it should "render component" in {
    //given
    val props = MessageBoxProps("test title", "test message", js.Array(
      MessageBoxAction.YES(() => ()),
      MessageBoxAction.NO(() => ())
    ), DefaultTheme.popup.regular)

    //when
    val result = testRender(<(MessageBox())(^.plain := props)())

    //then
    assertMessageBox(result, props, List("YES", "NO"))
  }

  "YES/NO non-closable popup" should "do nothing when onClose popup" in {
    //given
    val onYesAction = mockFunction[Unit]
    val onNoAction = mockFunction[Unit]
    val props = MessageBoxProps("test title", "test message", js.Array(
      MessageBoxAction.YES(onYesAction),
      MessageBoxAction.NO_NON_CLOSABLE(onNoAction)
    ), DefaultTheme.popup.regular)
    val comp = testRender(<(MessageBox())(^.plain := props)())
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
    val props = MessageBoxProps("test title", "test message", js.Array(
      MessageBoxAction.YES(() => ()),
      MessageBoxAction.NO_NON_CLOSABLE(() => ())
    ), DefaultTheme.popup.regular)

    //when
    val result = testRender(<(MessageBox())(^.plain := props)())

    //then
    assertMessageBox(result, props, List("YES", "NO"), closable = false)
  }

  private def assertMessageBox(result: TestInstance,
                               props: MessageBoxProps,
                               actions: List[String],
                               closable: Boolean = true): Assertion = {
    val width = 60
    val textWidth = width - (paddingHorizontal + 2) * 2
    val textLines = UI.splitText(props.message, textWidth)
    val height = (paddingVertical + 1) * 2 + textLines.size + 1
    
    def assertComponents(msgs: List[TestInstance],
                         actionsBox: TestInstance): Assertion = {

      msgs.size shouldBe textLines.size
      msgs.zip(textLines).zipWithIndex.foreach { case ((msg, textLine), index) =>
        assertTestComponent(msg, textLineComp, plain = true) {
          case TextLineProps(align, left, top, resWidth, text, style, focused, padding) =>
            align shouldBe TextAlign.center
            left shouldBe 2
            top shouldBe (1 + index)
            resWidth shouldBe (width - 10)
            text shouldBe textLine
            style shouldBe props.style
            focused shouldBe js.undefined
            padding shouldBe 0
        }
      }
      
      assertNativeComponent(actionsBox,
        <(buttonsPanelComp)(^.assertPlain[ButtonsPanelProps](inside(_) {
          case ButtonsPanelProps(top, resActions, resStyle, padding, margin) =>
            top shouldBe (1 + textLines.size)
            resActions.map(_.label).toList shouldBe actions
            resStyle shouldBe props.style
            padding shouldBe 1
            margin shouldBe js.undefined
        }))()
      )
    }
    
    assertTestComponent(result, popupComp)({ case PopupProps(_, resClosable, focusable, _, _) =>
      resClosable shouldBe closable
      focusable shouldBe true
    }, inside(_) { case List(content) =>
      assertTestComponent(content, modalContentComp)({
        case ModalContentProps(title, size, style, padding, left, footer) =>
          title shouldBe props.title
          size shouldBe width -> height
          style shouldBe props.style
          padding shouldBe ModalContent.padding
          left shouldBe "center"
          footer shouldBe None
      }, inside(_) {
          case List(msg, actionsBox) if textLines.size == 1 =>
            assertComponents(List(msg), actionsBox)
          case List(msg1, msg2, actionsBox) if textLines.size == 2 =>
            assertComponents(List(msg1, msg2), actionsBox)
        }
      )
    })
  }
}
