package farjs.ui.border

import farjs.ui.border.SingleBorder._
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class SingleBorderSpec extends TestSpec with TestRendererUtils {

  SingleBorder.horizontalLineComp = "HorizontalLine".asInstanceOf[ReactClass]
  SingleBorder.verticalLineComp = "VerticalLine".asInstanceOf[ReactClass]

  it should "render component" in {
    //given
    val props = SingleBorderProps(3, 4, style = new BlessedStyle {
      override val fg = "black"
      override val bg = "cyan"
    })
    val comp = <(SingleBorder())(^.plain := props)()

    //when
    val result = createTestRenderer(comp).root

    //then
    val (line1, line2, line3, line4) = inside(result.children.toList) {
      case List(line1, line2, line3, line4) => (line1, line2, line3, line4)
    }
    assertNativeComponent(line1, <(horizontalLineComp)(^.assertPlain[HorizontalLineProps](inside(_) {
      case HorizontalLineProps(resLeft, resTop, resLength, lineCh, style, startCh, endCh) =>
        resLeft shouldBe 0
        resTop shouldBe 0
        resLength shouldBe 3
        lineCh shouldBe SingleChars.horizontal
        style shouldBe props.style
        startCh shouldBe SingleChars.topLeft
        endCh shouldBe SingleChars.topRight
    }))())
    assertNativeComponent(line2, <(verticalLineComp)(^.assertPlain[VerticalLineProps](inside(_) {
      case VerticalLineProps(resLeft, resTop, resLength, lineCh, style, startCh, endCh) =>
        resLeft shouldBe 0
        resTop shouldBe 1
        resLength shouldBe 2
        lineCh shouldBe SingleChars.vertical
        style shouldBe props.style
        startCh shouldBe js.undefined
        endCh shouldBe js.undefined
    }))())
    assertNativeComponent(line3, <(verticalLineComp)(^.assertPlain[VerticalLineProps](inside(_) {
      case VerticalLineProps(resLeft, resTop, resLength, lineCh, style, startCh, endCh) =>
        resLeft shouldBe 2
        resTop shouldBe 1
        resLength shouldBe 2
        lineCh shouldBe SingleChars.vertical
        style shouldBe props.style
        startCh shouldBe js.undefined
        endCh shouldBe js.undefined
    }))())
    assertNativeComponent(line4, <(horizontalLineComp)(^.assertPlain[HorizontalLineProps](inside(_) {
      case HorizontalLineProps(resLeft, resTop, resLength, lineCh, style, startCh, endCh) =>
        resLeft shouldBe 0
        resTop shouldBe 3
        resLength shouldBe 3
        lineCh shouldBe SingleChars.horizontal
        style shouldBe props.style
        startCh shouldBe SingleChars.bottomLeft
        endCh shouldBe SingleChars.bottomRight
    }))())
  }
}
