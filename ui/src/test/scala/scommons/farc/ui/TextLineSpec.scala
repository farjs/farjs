package scommons.farc.ui

import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

class TextLineSpec extends TestSpec with ShallowRendererUtils {

  it should "render Left aligned text" in {
    //given
    val left = 1
    val props = getTextLineProps.copy(
      align = TextLine.Left,
      pos = (left, 2),
      width = 15,
      text = "test item"
    )

    //when
    val result = shallowRender(<(TextLine())(^.wrapped := props)())

    //then
    assertTextLine(result, props, left, " test item ")
  }
  
  it should "render Center aligned text" in {
    //given
    val left = 1
    val props = getTextLineProps.copy(
      align = TextLine.Center,
      pos = (left, 2),
      width = 15,
      text = "test item"
    )

    //when
    val result = shallowRender(<(TextLine())(^.wrapped := props)())

    //then
    assertTextLine(result, props, left + 2, " test item ")
  }

  it should "render Right aligned text" in {
    //given
    val left = 1
    val props = getTextLineProps.copy(
      align = TextLine.Right,
      pos = (left, 2),
      width = 15,
      text = "test item"
    )

    //when
    val result = shallowRender(<(TextLine())(^.wrapped := props)())

    //then
    assertTextLine(result, props, left + 4, " test item ")
  }

  it should "render focused text" in {
    //given
    val left = 1
    val props = getTextLineProps.copy(
      pos = (left, 2),
      width = 12,
      text = "test item",
      focused = true
    )

    //when
    val result = shallowRender(<(TextLine())(^.wrapped := props)())

    //then
    assertTextLine(result, props, left, " test item ")
  }
  
  it should "render text without padding" in {
    //given
    val left = 1
    val props = getTextLineProps.copy(
      pos = (left, 2),
      width = 12,
      text = "test item",
      focused = false,
      padding = 0
    )

    //when
    val result = shallowRender(<(TextLine())(^.wrapped := props)())

    //then
    assertTextLine(result, props, left, "test item")
  }
  
  it should "render long text with ellipsis" in {
    //given
    val left = 1
    val props = getTextLineProps.copy(
      pos = (left, 2),
      width = 12,
      text = "test long item"
    )

    //when
    val result = shallowRender(<(TextLine())(^.wrapped := props)())

    //then
    assertTextLine(result, props, left, " tes...item ")
  }
  
  private def getTextLineProps: TextLineProps = TextLineProps(
    align = TextLine.Left,
    pos = (1, 2),
    width = 10,
    text = "test item",
    style = new BlessedStyle {
      override val fg = "white"
      override val bg = "blue"
      override val focus = new BlessedStyle {}
    }
  )

  private def assertTextLine(result: ShallowInstance,
                             props: TextLineProps,
                             left: Int,
                             text: String): Unit = {
    val (_, top) = props.pos
    
    assertNativeComponent(result,
      <.text(
        ^.rbWidth := text.length,
        ^.rbHeight := 1,
        ^.rbLeft := left,
        ^.rbTop := top,
        ^.rbStyle := {
          if (props.focused) props.style.focus.orNull
          else props.style
        },
        ^.content := text
      )()
    )
  }
}
