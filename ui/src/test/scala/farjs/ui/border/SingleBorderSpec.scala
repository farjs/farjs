package farjs.ui.border

import farjs.ui.border.SingleBorder._
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

class SingleBorderSpec extends TestSpec with TestRendererUtils {

  SingleBorder.horizontalLineComp = () => "HorizontalLine".asInstanceOf[ReactClass]
  SingleBorder.verticalLineComp = () => "VerticalLine".asInstanceOf[ReactClass]

  it should "render component" in {
    //given
    val props = SingleBorderProps((3, 4), style = new BlessedStyle {
      override val fg = "black"
      override val bg = "cyan"
    })
    val comp = <(SingleBorder())(^.wrapped := props)()

    //when
    val result = createTestRenderer(comp).root

    //then
    val List(line1, line2, line3, line4) = result.children.toList
    assertTestComponent(line1, horizontalLineComp) {
      case HorizontalLineProps(pos, resLength, lineCh, style, startCh, endCh) =>
        pos shouldBe 0 -> 0
        resLength shouldBe 3
        lineCh shouldBe SingleBorder.horizontalCh
        style shouldBe props.style
        startCh shouldBe Some(SingleBorder.topLeftCh)
        endCh shouldBe Some(SingleBorder.topRightCh)
    }
    assertTestComponent(line2, verticalLineComp) {
      case VerticalLineProps(pos, resLength, lineCh, style, startCh, endCh) =>
        pos shouldBe 0 -> 1
        resLength shouldBe 2
        lineCh shouldBe SingleBorder.verticalCh
        style shouldBe props.style
        startCh shouldBe None
        endCh shouldBe None
    }
    assertTestComponent(line3, verticalLineComp) {
      case VerticalLineProps(pos, resLength, lineCh, style, startCh, endCh) =>
        pos shouldBe 2 -> 1
        resLength shouldBe 2
        lineCh shouldBe SingleBorder.verticalCh
        style shouldBe props.style
        startCh shouldBe None
        endCh shouldBe None
    }
    assertTestComponent(line4, horizontalLineComp) {
      case HorizontalLineProps(pos, resLength, lineCh, style, startCh, endCh) =>
        pos shouldBe 0 -> 3
        resLength shouldBe 3
        lineCh shouldBe SingleBorder.horizontalCh
        style shouldBe props.style
        startCh shouldBe Some(SingleBorder.bottomLeftCh)
        endCh shouldBe Some(SingleBorder.bottomRightCh)
    }
  }
}
