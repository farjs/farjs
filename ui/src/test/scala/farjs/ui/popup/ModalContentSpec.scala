package farjs.ui.popup

import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.theme.Theme
import org.scalatest.Assertion
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

class ModalContentSpec extends TestSpec with TestRendererUtils {

  ModalContent.doubleBorderComp = () => "DoubleBorder".asInstanceOf[ReactClass]

  it should "render component" in {
    //given
    val props = ModalContentProps("test title", (10, 20), Theme.current.popup.regular)
    val children = <.button()("some child")

    //when
    val result = testRender(<(ModalContent())(^.wrapped := props)(
      children
    ))

    //then
    assertModalContent(result, props, children)
  }

  private def assertModalContent(result: TestInstance,
                                 props: ModalContentProps,
                                 children: ReactElement): Assertion = {
    
    val (width, height) = props.size
    
    assertNativeComponent(result,
      <.box(
        ^.rbClickable := true,
        ^.rbAutoFocus := false,
        ^.rbWidth := width,
        ^.rbHeight := height,
        ^.rbTop := "center",
        ^.rbLeft := "center",
        ^.rbShadow := true,
        ^.rbPadding := new BlessedPadding {
          val left: Int = paddingHorizontal
          val right: Int = paddingHorizontal
          val top: Int = paddingVertical
          val bottom: Int = paddingVertical
        },
        ^.rbStyle := props.style
      )(), inside(_) { case List(border, child)=>
        assertTestComponent(border, doubleBorderComp) {
          case DoubleBorderProps(resSize, style, pos, title) =>
            resSize shouldBe (width - paddingHorizontal * 2) -> (height - paddingVertical * 2)
            style shouldBe props.style
            pos shouldBe 0 -> 0
            title shouldBe Some(props.title)
        }

        assertNativeComponent(child, children)
      }
    )
  }
}
