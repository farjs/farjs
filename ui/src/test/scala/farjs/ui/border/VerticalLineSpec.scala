package farjs.ui.border

import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

class VerticalLineSpec extends TestSpec with ShallowRendererUtils {

  it should "render line without start and end chars" in {
    //given
    val props = getVerticalLineProps.copy(
      startCh = None,
      endCh = None
    )
    val comp = <(VerticalLine())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertVerticalLine(result, props)
  }
  
  it should "render line with start char" in {
    //given
    val props = getVerticalLineProps.copy(
      startCh = Some("+"),
      endCh = None
    )
    val comp = <(VerticalLine())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertVerticalLine(result, props)
  }
  
  it should "render line with end char" in {
    //given
    val props = getVerticalLineProps.copy(
      startCh = None,
      endCh = Some("-")
    )
    val comp = <(VerticalLine())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertVerticalLine(result, props)
  }

  it should "render line with start and end chars" in {
    //given
    val props = getVerticalLineProps.copy(
      startCh = Some("+"),
      endCh = Some("-")
    )
    val comp = <(VerticalLine())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertVerticalLine(result, props)
  }

  private def getVerticalLineProps: VerticalLineProps = VerticalLineProps(
    pos = (1, 2),
    length = 5,
    lineCh = "*",
    style = new BlessedStyle {
      override val fg = "white"
      override val bg = "blue"
    },
    startCh = None,
    endCh = None
  )

  private def assertVerticalLine(result: ShallowInstance, props: VerticalLineProps): Unit = {
    val (left, top) = props.pos
    
    assertNativeComponent(result,
      <.text(
        ^.rbWidth := 1,
        ^.rbHeight := props.length,
        ^.rbLeft := left,
        ^.rbTop := top,
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
