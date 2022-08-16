package farjs.ui.border

import farjs.ui.border.SingleBorder._
import scommons.react.blessed._
import scommons.react.test._

class SingleBorderSpec extends TestSpec with TestRendererUtils {

  SingleBorder.horizontalLineComp = mockUiComponent("HorizontalLine")
  SingleBorder.verticalLineComp = mockUiComponent("VerticalLine")

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
    val (line1, line2, line3, line4) = inside(result.children.toList) {
      case List(line1, line2, line3, line4) => (line1, line2, line3, line4)
    }
    assertTestComponent(line1, horizontalLineComp, plain = true) {
      case HorizontalLineProps(resLeft, resTop, resLength, lineCh, style, startCh, endCh) =>
        resLeft shouldBe 0
        resTop shouldBe 0
        resLength shouldBe 3
        lineCh shouldBe SingleBorder.horizontalCh
        style shouldBe props.style
        startCh shouldBe SingleBorder.topLeftCh
        endCh shouldBe SingleBorder.topRightCh
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
    assertTestComponent(line4, horizontalLineComp, plain = true) {
      case HorizontalLineProps(resLeft, resTop, resLength, lineCh, style, startCh, endCh) =>
        resLeft shouldBe 0
        resTop shouldBe 3
        resLength shouldBe 3
        lineCh shouldBe SingleBorder.horizontalCh
        style shouldBe props.style
        startCh shouldBe SingleBorder.bottomLeftCh
        endCh shouldBe SingleBorder.bottomRightCh
    }
  }
}
