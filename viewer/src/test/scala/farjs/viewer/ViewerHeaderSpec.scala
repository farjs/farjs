package farjs.viewer

import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import farjs.ui.{TextAlign, TextLineProps, WithSizeProps}
import farjs.viewer.ViewerHeader._
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class ViewerHeaderSpec extends TestSpec with TestRendererUtils {

  ViewerHeader.withSizeComp = "WithSize".asInstanceOf[ReactClass]
  ViewerHeader.textLineComp = "TextLine".asInstanceOf[ReactClass]

  it should "render component" in {
    //given
    val props = ViewerHeaderProps(
      filePath = "/test/filePath",
      encoding = "utf-8",
      size = 12345.0,
      percent = 100
    )
    
    //when
    val result = createTestRenderer(withThemeContext(<(ViewerHeader())(^.plain := props)())).root

    //then
    assertViewerHeader(result, props)
  }

  private def assertViewerHeader(result: TestInstance, props: ViewerHeaderProps): Unit = {
    val style = DefaultTheme.menu.item
    val encodingWidth = math.max(props.encoding.length, 10)
    val sizeText = f"${props.size}%,.0f"
    val sizeWidth = math.max(sizeText.length, 12)
    val columnWidth = 8
    val percentWidth = 4
    val gapWidth = 2

    assertComponents(result.children, List(
      <(withSizeComp)(^.assertPlain[WithSizeProps](inside(_) {
        case WithSizeProps(render) =>
          val width = 80
          val dynamicWidth = width - encodingWidth - sizeWidth - columnWidth - percentWidth - gapWidth * 3
          val content = createTestRenderer(render(width, 25)).root

          assertNativeComponent(content, <.box(^.rbStyle := style)(
            <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
              case TextLineProps(align, left, top, resWidth, text, resStyle, focused, padding) =>
                align shouldBe TextAlign.left
                left shouldBe 0
                top shouldBe 0
                resWidth shouldBe dynamicWidth
                text shouldBe props.filePath
                resStyle shouldBe style
                focused shouldBe js.undefined
                padding shouldBe 0
            }))(),
            <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
              case TextLineProps(align, left, top, resWidth, text, resStyle, focused, padding) =>
                align shouldBe TextAlign.center
                left shouldBe (dynamicWidth + gapWidth)
                top shouldBe 0
                resWidth shouldBe encodingWidth
                text shouldBe props.encoding
                resStyle shouldBe style
                focused shouldBe js.undefined
                padding shouldBe 0
            }))(),
            <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
              case TextLineProps(align, left, top, resWidth, text, resStyle, focused, padding) =>
                align shouldBe TextAlign.right
                left shouldBe (dynamicWidth + encodingWidth + gapWidth * 2)
                top shouldBe 0
                resWidth shouldBe sizeWidth
                text shouldBe sizeText
                resStyle shouldBe style
                focused shouldBe js.undefined
                padding shouldBe 0
            }))(),
            <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
              case TextLineProps(align, left, top, resWidth, text, resStyle, focused, padding) =>
                align shouldBe TextAlign.left
                left shouldBe (width - columnWidth - percentWidth)
                top shouldBe 0
                resWidth shouldBe columnWidth
                text shouldBe s"Col ${props.column}"
                resStyle shouldBe style
                focused shouldBe js.undefined
                padding shouldBe 0
            }))(),
            <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
              case TextLineProps(align, left, top, resWidth, text, resStyle, focused, padding) =>
                align shouldBe TextAlign.right
                left shouldBe (width - percentWidth)
                top shouldBe 0
                resWidth shouldBe percentWidth
                text shouldBe s"${props.percent}%"
                resStyle shouldBe style
                focused shouldBe js.undefined
                padding shouldBe 0
            }))()
          ))
      }))()
    ))
  }
}
