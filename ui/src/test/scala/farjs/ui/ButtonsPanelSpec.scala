package farjs.ui

import farjs.ui.ButtonsPanel._
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class ButtonsPanelSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  ButtonsPanel.buttonComp = "Button".asInstanceOf[ReactClass]

  it should "call onAction1 when press button1" in {
    //given
    val onAction1 = mockFunction[Unit]
    val onAction2 = mockFunction[Unit]
    val props = getButtonsPanelProps(List(
      "button1" -> onAction1,
      "button2" -> onAction2
    ))
    val comp = testRender(<(ButtonsPanel())(^.plain := props)())
    val (b1, _) = inside(findComponents(comp, buttonComp)) {
      case List(b1, b2) => (b1, b2)
    }

    //then
    var onActionCalled = false
    onAction1.expects().onCall { () =>
      onActionCalled = true
    }
    onAction2.expects().never()
    
    //when
    b1.props.onPress()

    //then
    eventually {
      onActionCalled shouldBe true
    }
  }

  it should "call onAction2 when press button2" in {
    //given
    val onAction1 = mockFunction[Unit]
    val onAction2 = mockFunction[Unit]
    val props = getButtonsPanelProps(List(
      "button1" -> onAction1,
      "button2" -> onAction2
    ))
    val comp = testRender(<(ButtonsPanel())(^.plain := props)())
    val (_, b2) = inside(findComponents(comp, buttonComp)) {
      case List(b1, b2) => (b1, b2)
    }

    //then
    onAction1.expects().never()
    var onActionCalled = false
    onAction2.expects().onCall { () =>
      onActionCalled = true
    }
    
    //when
    b2.props.onPress()

    //then
    eventually {
      onActionCalled shouldBe true
    }
  }

  it should "render component" in {
    //given
    val onAction = mockFunction[Unit]
    val props = getButtonsPanelProps(List(
      "test btn" -> onAction,
      "test btn2" -> onAction
    ))

    //when
    val result = testRender(<(ButtonsPanel())(^.plain := props)())

    //then
    assertButtonsPanel(result, props, List("  test btn  " -> 0, "  test btn2  " -> 15))
  }
  
  private def getButtonsPanelProps(actions: List[(String, () => Unit)]): ButtonsPanelProps = ButtonsPanelProps(
    top = 1,
    actions = js.Array(actions.map { case (label, onAction) =>
      ButtonsPanelAction(label, onAction)
    }: _*),
    style = new BlessedStyle {
      override val fg = "white"
      override val bg = "blue"
      override val focus = new BlessedStyle {
        override val fg = "cyan"
        override val bg = "black"
      }
    },
    padding = 2,
    margin = 3
  )

  private def assertButtonsPanel(result: TestInstance,
                                 props: ButtonsPanelProps,
                                 actions: List[(String, Int)]): Assertion = {

    val buttonsWidth = actions.map(_._1.length).sum + (actions.size - 1) * props.margin.getOrElse(0)
    
    assertNativeComponent(result,
      <.box(
        ^.rbWidth := buttonsWidth,
        ^.rbHeight := 1,
        ^.rbTop := props.top,
        ^.rbLeft := "center",
        ^.rbStyle := props.style
      )(), { buttons: List[TestInstance] =>
        buttons.size shouldBe actions.size
        buttons.zip(actions).foreach { case (btn, (action, pos)) =>
          assertNativeComponent(btn, <(buttonComp)(^.assertPlain[ButtonProps](inside(_) {
            case ButtonProps(left, top, label, resStyle, _) =>
              left shouldBe pos
              top shouldBe 0
              label shouldBe action
              resStyle shouldBe props.style
          }))())
        }
        Succeeded
      }
    )
  }
}
