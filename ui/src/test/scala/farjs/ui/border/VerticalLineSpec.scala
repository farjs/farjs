package farjs.ui.border

import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class VerticalLineSpec extends TestSpec with TestRendererUtils {

  it should "render line without start and end chars" in {
    //given
    val props = VerticalLineProps.copy(getVerticalLineProps)(
      startCh = js.undefined,
      endCh = js.undefined
    )
    val comp = <(VerticalLine())(^.plain := props)()

    //when
    val result = testRender(comp)

    //then
    assertVerticalLine(result, props)
  }
  
  it should "render line with start char" in {
    //given
    val props = VerticalLineProps.copy(getVerticalLineProps)(
      startCh = "+",
      endCh = js.undefined
    )
    val comp = <(VerticalLine())(^.plain := props)()

    //when
    val result = testRender(comp)

    //then
    assertVerticalLine(result, props)
  }
  
  it should "render line with end char" in {
    //given
    val props = VerticalLineProps.copy(getVerticalLineProps)(
      startCh = js.undefined,
      endCh = "-"
    )
    val comp = <(VerticalLine())(^.plain := props)()

    //when
    val result = testRender(comp)

    //then
    assertVerticalLine(result, props)
  }

  it should "render line with start and end chars" in {
    //given
    val props = VerticalLineProps.copy(getVerticalLineProps)(
      startCh = "+",
      endCh = "-"
    )
    val comp = <(VerticalLine())(^.plain := props)()

    //when
    val result = testRender(comp)

    //then
    assertVerticalLine(result, props)
  }

  private def getVerticalLineProps: VerticalLineProps = VerticalLineProps(
    left = 1,
    top = 2,
    length = 5,
    lineCh = "*",
    style = new BlessedStyle {
      override val fg = "white"
      override val bg = "blue"
    },
    startCh = js.undefined,
    endCh = js.undefined
  )

  private def assertVerticalLine(result: TestInstance, props: VerticalLineProps): Unit = {
    assertNativeComponent(result,
      <.text(
        ^.rbWidth := 1,
        ^.rbHeight := props.length,
        ^.rbLeft := props.left,
        ^.rbTop := props.top,
        ^.rbStyle := props.style,
        ^.content := {
          val startCh = props.startCh.getOrElse("")
          val endCh = props.endCh.getOrElse("")

          startCh +
            props.lineCh * (props.length - startCh.length - endCh.length) +
            endCh
        }
      )()
    )
  }
}
