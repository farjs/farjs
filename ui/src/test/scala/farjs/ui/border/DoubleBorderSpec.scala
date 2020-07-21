package farjs.ui.border

import farjs.ui._
import org.scalatest.Assertion
import scommons.react._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

class DoubleBorderSpec extends TestSpec with ShallowRendererUtils {

  it should "render component" in {
    //given
    val props = DoubleBorderProps((3, 4), style = new BlessedStyle {
      override val fg = "black"
      override val bg = "cyan"
    }, pos = (1, 2))
    val comp = <(DoubleBorder())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertDoubleBorder(result, props)
  }
  
  it should "render component with title" in {
    //given
    val props = DoubleBorderProps((15, 5), style = new BlessedStyle {
      override val fg = "black"
      override val bg = "cyan"
    }, pos = (1, 2), title = Some("test title"))
    val comp = <(DoubleBorder())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertDoubleBorder(result, props)
  }
  
  private def assertDoubleBorder(result: ShallowInstance, props: DoubleBorderProps): Unit = {
    val (width, height) = props.size
    val (left, top) = props.pos

    def assertComponents(line1: ShallowInstance,
                         title: Option[ShallowInstance],
                         line2: ShallowInstance,
                         line3: ShallowInstance,
                         line4: ShallowInstance): Assertion = {

      assertComponent(line1, HorizontalLine) {
        case HorizontalLineProps(pos, resLength, lineCh, style, startCh, endCh) =>
          pos shouldBe props.pos
          resLength shouldBe width
          lineCh shouldBe DoubleBorder.horizontalCh
          style shouldBe props.style
          startCh shouldBe Some(DoubleBorder.topLeftCh)
          endCh shouldBe Some(DoubleBorder.topRightCh)
      }

      title.isDefined shouldBe props.title.isDefined
      title.foreach { t =>
        assertComponent(t, TextLine) {
          case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
            align shouldBe TextLine.Center
            pos shouldBe props.pos
            resWidth shouldBe width
            text shouldBe props.title.get
            style shouldBe props.style
            focused shouldBe false
            padding shouldBe 1
        }
      }

      assertComponent(line2, VerticalLine) {
        case VerticalLineProps(pos, resLength, lineCh, style, startCh, endCh) =>
          pos shouldBe left -> (top + 1)
          resLength shouldBe (height - 2)
          lineCh shouldBe DoubleBorder.verticalCh
          style shouldBe props.style
          startCh shouldBe None
          endCh shouldBe None
      }
      assertComponent(line3, VerticalLine) {
        case VerticalLineProps(pos, resLength, lineCh, style, startCh, endCh) =>
          pos shouldBe (left + width - 1) -> (top + 1)
          resLength shouldBe (height - 2)
          lineCh shouldBe DoubleBorder.verticalCh
          style shouldBe props.style
          startCh shouldBe None
          endCh shouldBe None
      }
      assertComponent(line4, HorizontalLine) {
        case HorizontalLineProps(pos, resLength, lineCh, style, startCh, endCh) =>
          pos shouldBe left -> (top + height - 1)
          resLength shouldBe width
          lineCh shouldBe DoubleBorder.horizontalCh
          style shouldBe props.style
          startCh shouldBe Some(DoubleBorder.bottomLeftCh)
          endCh shouldBe Some(DoubleBorder.bottomRightCh)
      }
    }

    assertNativeComponent(result, <.>()(), {
      case List(line1, line2, line3, line4) =>
        assertComponents(line1, None, line2, line3, line4)
      case List(line1, title, line2, line3, line4) =>
        assertComponents(line1, Some(title), line2, line3, line4)
    })
  }
}
