package farjs.ui

import farjs.ui.ScrollBar._
import scommons.react.blessed._
import scommons.react.test._

class ScrollBarSpec extends TestSpec with TestRendererUtils {

  it should "call onChange(min) if value = min when onClick up arrow" in {
    //given
    val onChange = mockFunction[Int, Unit]
    val props = getScrollBarProps(onChange = onChange)
    props.value shouldBe props.min
    val renderer = createTestRenderer(<(ScrollBar())(^.plain := props)())
    val text = inside(findComponents(renderer.root, <.text.name)) {
      case List(t, _, _, _, _) => t
    }

    //then
    onChange.expects(props.min)

    //when
    text.props.onClick(null)
  }

  it should "call onChange(value-1) if value > min when onClick up arrow" in {
    //given
    val onChange = mockFunction[Int, Unit]
    val props = getScrollBarProps(value = 2, onChange = onChange)
    props.value should be > props.min
    val renderer = createTestRenderer(<(ScrollBar())(^.plain := props)())
    val text = inside(findComponents(renderer.root, <.text.name)) {
      case List(t, _, _, _, _) => t
    }

    //then
    onChange.expects(props.value - 1)

    //when
    text.props.onClick(null)
  }

  it should "call onChange(min) if value < extent when onClick up block" in {
    //given
    val onChange = mockFunction[Int, Unit]
    val props = getScrollBarProps(value = 5, onChange = onChange)
    props.value should be < props.extent
    val renderer = createTestRenderer(<(ScrollBar())(^.plain := props)())
    val text = inside(findComponents(renderer.root, <.text.name)) {
      case List(_, t, _, _, _) => t
    }

    //then
    onChange.expects(props.min)

    //when
    text.props.onClick(null)
  }

  it should "call onChange(value-extent) if value > extent when onClick up block" in {
    //given
    val onChange = mockFunction[Int, Unit]
    val props = getScrollBarProps(value = 10, onChange = onChange)
    props.value should be > props.extent
    val renderer = createTestRenderer(<(ScrollBar())(^.plain := props)())
    val text = inside(findComponents(renderer.root, <.text.name)) {
      case List(_, t, _, _, _) => t
    }

    //then
    onChange.expects(props.value - props.extent)

    //when
    text.props.onClick(null)
  }

  it should "call onChange(value+extent) if value < extent when onClick down block" in {
    //given
    val onChange = mockFunction[Int, Unit]
    val props = getScrollBarProps(value = 10, onChange = onChange)
    props.value should be < (props.max - props.extent)
    val renderer = createTestRenderer(<(ScrollBar())(^.plain := props)())
    val text = inside(findComponents(renderer.root, <.text.name)) {
      case List(_, _, _, t, _) => t
    }

    //then
    onChange.expects(props.value + props.extent)

    //when
    text.props.onClick(null)
  }

  it should "call onChange(max) if value > extent when onClick down block" in {
    //given
    val onChange = mockFunction[Int, Unit]
    val props = getScrollBarProps(value = 15, onChange = onChange)
    props.value should be > (props.max - props.extent)
    val renderer = createTestRenderer(<(ScrollBar())(^.plain := props)())
    val text = inside(findComponents(renderer.root, <.text.name)) {
      case List(_, _, _, t, _) => t
    }

    //then
    onChange.expects(props.max)

    //when
    text.props.onClick(null)
  }

  it should "call onChange(value+1) if value < max when onClick down arrow" in {
    //given
    val onChange = mockFunction[Int, Unit]
    val props = getScrollBarProps(value = 15, onChange = onChange)
    props.value should be < props.max
    val renderer = createTestRenderer(<(ScrollBar())(^.plain := props)())
    val text = inside(findComponents(renderer.root, <.text.name)) {
      case List(_, _, _, _, t) => t
    }

    //then
    onChange.expects(props.value + 1)

    //when
    text.props.onClick(null)
  }

  it should "call onChange(max) if value = max when onClick down arrow" in {
    //given
    val onChange = mockFunction[Int, Unit]
    val props = getScrollBarProps(value = 20, onChange = onChange)
    props.value shouldBe props.max
    val renderer = createTestRenderer(<(ScrollBar())(^.plain := props)())
    val text = inside(findComponents(renderer.root, <.text.name)) {
      case List(_, _, _, _, t) => t
    }

    //then
    onChange.expects(props.max)

    //when
    text.props.onClick(null)
  }

  it should "render component at min position" in {
    //given
    val props = getScrollBarProps()
    props.value shouldBe props.min

    //when
    val result = createTestRenderer(<(ScrollBar())(^.plain := props)()).root

    //then
    assertScrollBar(result, props, upLength = 0)
  }

  it should "render component at min+1 position" in {
    //given
    val props = getScrollBarProps(value = 1)
    props.value shouldBe (props.min + 1)

    //when
    val result = createTestRenderer(<(ScrollBar())(^.plain := props)()).root

    //then
    assertScrollBar(result, props, upLength = 1)
  }

  it should "render component between min and max" in {
    //given
    val props = getScrollBarProps(value = 10)
    props.value should be > props.min
    props.value should be < props.max

    //when
    val result = createTestRenderer(<(ScrollBar())(^.plain := props)()).root

    //then
    assertScrollBar(result, props, upLength = 3)
  }

  it should "render component at max-1 position" in {
    //given
    val props = getScrollBarProps(value = 19)
    props.value shouldBe (props.max - 1)

    //when
    val result = createTestRenderer(<(ScrollBar())(^.plain := props)()).root

    //then
    assertScrollBar(result, props, upLength = 4)
  }

  it should "render component at max position" in {
    //given
    val props = getScrollBarProps(value = 20)
    props.value shouldBe props.max

    //when
    val result = createTestRenderer(<(ScrollBar())(^.plain := props)()).root

    //then
    assertScrollBar(result, props, upLength = 5)
  }

  private def getScrollBarProps(value: Int = 0,
                                extent: Int = 8,
                                min: Int = 0,
                                max: Int = 20,
                                onChange: Int => Unit = _ => ()): ScrollBarProps = {
    ScrollBarProps(
      left = 1,
      top = 2,
      length = 8,
      style = new BlessedStyle {},
      value = value,
      extent = extent,
      min = min,
      max = max,
      onChange = onChange
    )
  }

  private def assertScrollBar(result: TestInstance, props: ScrollBarProps, upLength: Int): Unit = {
    val markerLength = 1
    val downLength = props.length - 2 - upLength - markerLength

    assertComponents(result.children, List(
      <.text(
        ^.rbWidth := 1,
        ^.rbHeight := 1,
        ^.rbLeft := props.left,
        ^.rbTop := props.top,
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := props.style,
        ^.content := upArrowCh
      )(),
      <.text(
        ^.rbWidth := 1,
        ^.rbHeight := upLength,
        ^.rbLeft := props.left,
        ^.rbTop := props.top + 1,
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := props.style,
        ^.content := scrollCh * upLength
      )(),
      <.text(
        ^.rbWidth := 1,
        ^.rbHeight := markerLength,
        ^.rbLeft := props.left,
        ^.rbTop := props.top + 1 + upLength,
        ^.rbAutoFocus := false,
        ^.rbStyle := props.style,
        ^.content := markerCh
      )(),
      <.text(
        ^.rbWidth := 1,
        ^.rbHeight := downLength,
        ^.rbLeft := props.left,
        ^.rbTop := props.top + 1 + upLength + markerLength,
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := props.style,
        ^.content := scrollCh * downLength
      )(),
      <.text(
        ^.rbWidth := 1,
        ^.rbHeight := 1,
        ^.rbLeft := props.left,
        ^.rbTop := props.top + 1 + upLength + markerLength + downLength,
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := props.style,
        ^.content := downArrowCh
      )()
    ))
  }
}
