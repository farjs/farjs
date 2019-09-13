package farclone.ui.border

import scommons.react._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.util.ShallowRendererUtils

class SingleBorderSpec extends TestSpec with ShallowRendererUtils {

  it should "render component" in {
    //given
    val props = SingleBorderProps((3, 4), style = new BlessedStyle {
      override val fg = "black"
      override val bg = "cyan"
    })
    val comp = <(SingleBorder())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertNativeComponent(result, <.>()(), { case List(line1, line2, line3, line4) =>
      line1.key shouldBe "0"
      assertComponent(line1, HorizontalLine) {
        case HorizontalLineProps(pos, resLength, lineCh, style, startCh, endCh) =>
          pos shouldBe 0 -> 0
          resLength shouldBe 3
          lineCh shouldBe SingleBorder.horizontalCh
          style shouldBe props.style
          startCh shouldBe Some(SingleBorder.topLeftCh)
          endCh shouldBe Some(SingleBorder.topRightCh)
      }
      line2.key shouldBe "1"
      assertComponent(line2, VerticalLine) {
        case VerticalLineProps(pos, resLength, lineCh, style, startCh, endCh) =>
          pos shouldBe 0 -> 1
          resLength shouldBe 2
          lineCh shouldBe SingleBorder.verticalCh
          style shouldBe props.style
          startCh shouldBe None
          endCh shouldBe None
      }
      line3.key shouldBe "2"
      assertComponent(line3, VerticalLine) {
        case VerticalLineProps(pos, resLength, lineCh, style, startCh, endCh) =>
          pos shouldBe 2 -> 1
          resLength shouldBe 2
          lineCh shouldBe SingleBorder.verticalCh
          style shouldBe props.style
          startCh shouldBe None
          endCh shouldBe None
      }
      line4.key shouldBe "3"
      assertComponent(line4, HorizontalLine) {
        case HorizontalLineProps(pos, resLength, lineCh, style, startCh, endCh) =>
          pos shouldBe 0 -> 3
          resLength shouldBe 3
          lineCh shouldBe SingleBorder.horizontalCh
          style shouldBe props.style
          startCh shouldBe Some(SingleBorder.bottomLeftCh)
          endCh shouldBe Some(SingleBorder.bottomRightCh)
      }
    })
  }
}
