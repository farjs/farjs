package scommons.farc.ui.border

import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

class HorizontalLineSpec extends TestSpec with ShallowRendererUtils {

  it should "render line without start and end chars" in {
    //given
    val props = getHorizontalLineProps.copy(
      startCh = None,
      endCh = None
    )
    val comp = <(HorizontalLine())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertHorizontalLine(result, props)
  }
  
  it should "render line with start char" in {
    //given
    val props = getHorizontalLineProps.copy(
      startCh = Some("+"),
      endCh = None
    )
    val comp = <(HorizontalLine())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertHorizontalLine(result, props)
  }
  
  it should "render line with end char" in {
    //given
    val props = getHorizontalLineProps.copy(
      startCh = None,
      endCh = Some("-")
    )
    val comp = <(HorizontalLine())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertHorizontalLine(result, props)
  }

  it should "render line with start and end chars" in {
    //given
    val props = getHorizontalLineProps.copy(
      startCh = Some("+"),
      endCh = Some("-")
    )
    val comp = <(HorizontalLine())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertHorizontalLine(result, props)
  }

  private def getHorizontalLineProps: HorizontalLineProps = HorizontalLineProps(
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

  private def assertHorizontalLine(result: ShallowInstance, props: HorizontalLineProps): Unit = {
    val (left, top) = props.pos
    
    assertNativeComponent(result,
      <.text(
        ^.rbWidth := props.length,
        ^.rbHeight := 1,
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
