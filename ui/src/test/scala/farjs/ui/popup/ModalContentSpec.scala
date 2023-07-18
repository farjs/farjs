package farjs.ui.popup

import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.theme.DefaultTheme
import org.scalatest.Assertion
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class ModalContentSpec extends TestSpec with TestRendererUtils {

  ModalContent.doubleBorderComp = "DoubleBorder".asInstanceOf[ReactClass]

  it should "render component" in {
    //given
    val props = ModalContentProps("test title", 10, 20, DefaultTheme.popup.regular)
    val children = <.button()("some child")

    //when
    val result = testRender(<(ModalContent())(^.plain := props)(
      children
    ))

    //then
    assertModalContent(result, props, children)
  }

  it should "render component with footer" in {
    //given
    val props = ModalContentProps(
      title = "test title",
      width = 10,
      height = 20,
      style = DefaultTheme.popup.regular,
      padding = js.undefined,
      left = js.undefined,
      footer = "test footer"
    )
    val children = <.button()("some child")

    //when
    val result = testRender(<(ModalContent())(^.plain := props)(
      children
    ))

    //then
    assertModalContent(result, props, children)
  }

  private def assertModalContent(result: TestInstance,
                                 props: ModalContentProps,
                                 children: ReactElement): Assertion = {
    
    val width = props.width
    val height = props.height
    
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
        assertNativeComponent(border, <(doubleBorderComp)(^.assertPlain[DoubleBorderProps](inside(_) {
          case DoubleBorderProps(resWidth, resHeight, style, resLeft, resTop, title, footer) =>
            resWidth shouldBe (width - paddingHorizontal * 2)
            resHeight shouldBe (height - paddingVertical * 2)
            style shouldBe props.style
            resLeft shouldBe js.undefined
            resTop shouldBe js.undefined
            title shouldBe props.title
            footer shouldBe props.footer
        }))())

        assertNativeComponent(child, children)
      }
    )
  }
}
