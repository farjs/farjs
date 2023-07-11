package farjs.ui.border

import farjs.ui._
import farjs.ui.border.DoubleBorder._
import org.scalatest.{Assertion, Succeeded}
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class DoubleBorderSpec extends TestSpec with TestRendererUtils {

  DoubleBorder.horizontalLineComp = mockUiComponent("HorizontalLine")
  DoubleBorder.verticalLineComp = mockUiComponent("VerticalLine")
  DoubleBorder.textLineComp = "TextLine".asInstanceOf[ReactClass]

  it should "render component" in {
    //given
    val props = DoubleBorderProps(3, 4, style = new BlessedStyle {
      override val fg = "black"
      override val bg = "cyan"
    }, left = 1, top = 2)
    val comp = <(DoubleBorder())(^.plain := props)()

    //when
    val result = createTestRenderer(comp).root

    //then
    assertDoubleBorder(result, props)
  }
  
  it should "render component with title" in {
    //given
    val props = DoubleBorderProps(15, 5, style = new BlessedStyle {
      override val fg = "black"
      override val bg = "cyan"
    }, left = 1, top = 2, title = "test title")
    val comp = <(DoubleBorder())(^.plain := props)()

    //when
    val result = createTestRenderer(comp).root

    //then
    assertDoubleBorder(result, props)
  }
  
  it should "render component with title and footer" in {
    //given
    val props = DoubleBorderProps(15, 5, style = new BlessedStyle {
      override val fg = "black"
      override val bg = "cyan"
    }, left = 1, top = 2, title = "test title", footer = "test footer")
    val comp = <(DoubleBorder())(^.plain := props)()

    //when
    val result = createTestRenderer(comp).root

    //then
    assertDoubleBorder(result, props)
  }
  
  private def assertDoubleBorder(result: TestInstance, props: DoubleBorderProps): Unit = {
    val left = props.left.getOrElse(0)
    val top = props.top.getOrElse(0)

    def assertComponents(line1: TestInstance,
                         title: Option[TestInstance],
                         line2: TestInstance,
                         line3: TestInstance,
                         line4: TestInstance,
                         footer: Option[TestInstance]): Assertion = {

      assertTestComponent(line1, horizontalLineComp, plain = true) {
        case HorizontalLineProps(resLeft, resTop, resLength, lineCh, style, startCh, endCh) =>
          resLeft shouldBe left
          resTop shouldBe top
          resLength shouldBe props.width
          lineCh shouldBe DoubleChars.horizontal
          style shouldBe props.style
          startCh shouldBe DoubleChars.topLeft
          endCh shouldBe DoubleChars.topRight
      }

      title.isDefined shouldBe props.title.isDefined
      title.foreach { t =>
        assertNativeComponent(t, <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
          case TextLineProps(align, resLeft, resTop, resWidth, text, style, focused, padding) =>
            align shouldBe TextAlign.center
            resLeft shouldBe left
            resTop shouldBe top
            resWidth shouldBe props.width
            text shouldBe props.title.get
            style shouldBe props.style
            focused shouldBe js.undefined
            padding shouldBe js.undefined
        }))())
      }

      assertTestComponent(line2, verticalLineComp, plain = true) {
        case VerticalLineProps(resLeft, resTop, resLength, lineCh, style, startCh, endCh) =>
          resLeft shouldBe left
          resTop shouldBe (top + 1)
          resLength shouldBe (props.height - 2)
          lineCh shouldBe DoubleChars.vertical
          style shouldBe props.style
          startCh shouldBe js.undefined
          endCh shouldBe js.undefined
      }
      assertTestComponent(line3, verticalLineComp, plain = true) {
        case VerticalLineProps(resLeft, resTop, resLength, lineCh, style, startCh, endCh) =>
          resLeft shouldBe (left + props.width - 1)
          resTop shouldBe (top + 1)
          resLength shouldBe (props.height - 2)
          lineCh shouldBe DoubleChars.vertical
          style shouldBe props.style
          startCh shouldBe js.undefined
          endCh shouldBe js.undefined
      }
      assertTestComponent(line4, horizontalLineComp, plain = true) {
        case HorizontalLineProps(resLeft, resTop, resLength, lineCh, style, startCh, endCh) =>
          resLeft shouldBe left
          resTop shouldBe (top + props.height - 1)
          resLength shouldBe props.width
          lineCh shouldBe DoubleChars.horizontal
          style shouldBe props.style
          startCh shouldBe DoubleChars.bottomLeft
          endCh shouldBe DoubleChars.bottomRight
      }

      footer.isDefined shouldBe props.footer.isDefined
      footer.foreach { t =>
        assertNativeComponent(t, <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
          case TextLineProps(align, resLeft, resTop, resWidth, text, style, focused, padding) =>
            align shouldBe TextAlign.center
            resLeft shouldBe left
            resTop shouldBe (top + props.height - 1)
            resWidth shouldBe props.width
            text shouldBe props.footer.get
            style shouldBe props.style
            focused shouldBe js.undefined
            padding shouldBe js.undefined
        }))())
      }
      Succeeded
    }

    inside(result.children.toList) {
      case List(line1, line2, line3, line4) =>
        assertComponents(line1, None, line2, line3, line4, None)
      case List(line1, title, line2, line3, line4) =>
        assertComponents(line1, Some(title), line2, line3, line4, None)
      case List(line1, title, line2, line3, line4, footer) =>
        assertComponents(line1, Some(title), line2, line3, line4, Some(footer))
    }
  }
}
