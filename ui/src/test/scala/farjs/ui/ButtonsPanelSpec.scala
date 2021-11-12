package farjs.ui

import farjs.ui.ButtonsPanel._
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.blessed._
import scommons.react.test._

class ButtonsPanelSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  ButtonsPanel.buttonComp = mockUiComponent("Button")

  it should "call onAction1 when press button1" in {
    //given
    val onAction1 = mockFunction[Unit]
    val onAction2 = mockFunction[Unit]
    val props = getButtonsPanelProps(List(
      "button1" -> onAction1,
      "button2" -> onAction2
    ))
    val comp = testRender(<(ButtonsPanel())(^.wrapped := props)())
    val (b1, _) = inside(findProps(comp, buttonComp)) {
      case List(b1, b2) => (b1, b2)
    }

    //then
    var onActionCalled = false
    onAction1.expects().onCall { () =>
      onActionCalled = true
    }
    onAction2.expects().never()
    
    //when
    b1.onPress()

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
    val comp = testRender(<(ButtonsPanel())(^.wrapped := props)())
    val (_, b2) = inside(findProps(comp, buttonComp)) {
      case List(b1, b2) => (b1, b2)
    }

    //then
    onAction1.expects().never()
    var onActionCalled = false
    onAction2.expects().onCall { () =>
      onActionCalled = true
    }
    
    //when
    b2.onPress()

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
    val result = testRender(<(ButtonsPanel())(^.wrapped := props)())

    //then
    assertButtonsPanel(result, props, List("  test btn  " -> 0, "  test btn2  " -> 15))
  }
  
  private def getButtonsPanelProps(actions: List[(String, () => Unit)]): ButtonsPanelProps = ButtonsPanelProps(
    top = 1,
    actions = actions,
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

    val buttonsWidth = actions.map(_._1.length).sum + (actions.size - 1) * props.margin
    
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
          assertTestComponent(btn, buttonComp) {
            case ButtonProps(resPos, label, resStyle, _) =>
              resPos shouldBe pos -> 0
              label shouldBe action
              resStyle shouldBe props.style
          }
        }
        Succeeded
      }
    )
  }
}
