package farjs.ui.border

import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class HorizontalLineSpec extends TestSpec with TestRendererUtils {

  it should "render line without start and end chars" in {
    //given
    val props = HorizontalLineProps.copy(getHorizontalLineProps)(
      startCh = js.undefined,
      endCh = js.undefined
    )
    val comp = <(HorizontalLine())(^.plain := props)()

    //when
    val result = testRender(comp)

    //then
    assertHorizontalLine(result, props)
  }
  
  it should "render line with start char" in {
    //given
    val props = HorizontalLineProps.copy(getHorizontalLineProps)(
      startCh = "+",
      endCh = js.undefined
    )
    val comp = <(HorizontalLine())(^.plain := props)()

    //when
    val result = testRender(comp)

    //then
    assertHorizontalLine(result, props)
  }
  
  it should "render line with end char" in {
    //given
    val props = HorizontalLineProps.copy(getHorizontalLineProps)(
      startCh = js.undefined,
      endCh = "-"
    )
    val comp = <(HorizontalLine())(^.plain := props)()

    //when
    val result = testRender(comp)

    //then
    assertHorizontalLine(result, props)
  }

  it should "render line with start and end chars" in {
    //given
    val props = HorizontalLineProps.copy(getHorizontalLineProps)(
      startCh = "+",
      endCh = "-"
    )
    val comp = <(HorizontalLine())(^.plain := props)()

    //when
    val result = testRender(comp)

    //then
    assertHorizontalLine(result, props)
  }

  private def getHorizontalLineProps: HorizontalLineProps = HorizontalLineProps(
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

  private def assertHorizontalLine(result: TestInstance, props: HorizontalLineProps): Unit = {
    assertNativeComponent(result,
      <.text(
        ^.rbWidth := props.length,
        ^.rbHeight := 1,
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
