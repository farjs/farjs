package farjs.ui

import scommons.react.blessed._
import scommons.react.test._

class TextLineSpec extends TestSpec with TestRendererUtils {

  it should "render Left aligned text" in {
    //given
    val left = 1
    val props = TextLineProps.copy(getTextLineProps)(
      align = TextAlign.left,
      left = left,
      top = 2,
      width = 15,
      text = "test item"
    )

    //when
    val result = createTestRenderer(<(TextLine())(^.plain := props)()).root

    //then
    assertTextLine(result, props, left, " test item ")
  }
  
  it should "render Center aligned text" in {
    //given
    val left = 1
    val props = TextLineProps.copy(getTextLineProps)(
      align = TextAlign.center,
      left = left,
      top = 2,
      width = 15,
      text = "test item"
    )

    //when
    val result = createTestRenderer(<(TextLine())(^.plain := props)()).root

    //then
    assertTextLine(result, props, left + 2, " test item ")
  }

  it should "render Right aligned text" in {
    //given
    val left = 1
    val props = TextLineProps.copy(getTextLineProps)(
      align = TextAlign.right,
      left = left,
      top = 2,
      width = 15,
      text = "test item"
    )

    //when
    inside(createTestRenderer(<(TextLine())(^.plain := props)()).root) { case result =>
      //then
      //       10|  15|
      //     test item 
      assertTextLine(result, props, left + 4, " test item ")
    }

    //when empty
    inside(createTestRenderer(<(TextLine())(^.plain := TextLineProps.copy(props)(text = ""))()).root) { case result =>
      //then
      //       10|  15|
      //               
      assertTextLine(result, props, left + 13, "  ")
    }

    //when without padding
    inside(createTestRenderer(<(TextLine())(^.plain := TextLineProps.copy(props)(padding = 0))()).root) { case result =>
      //then
      //       10|  15|
      //      test item
      assertTextLine(result, props, left + 6, "test item")
    }
    
    //when empty text without padding
    inside(createTestRenderer(<(TextLine())(^.plain := TextLineProps.copy(props)(text = "", padding = 0))()).root) { case result =>
      //then
      assertTextLine(result, props, 0, "")
    }
  }

  it should "render focused text" in {
    //given
    val left = 1
    val props = TextLineProps.copy(getTextLineProps)(
      left = left,
      top = 2,
      width = 12,
      text = "test item",
      focused = true
    )

    //when
    val result = createTestRenderer(<(TextLine())(^.plain := props)()).root

    //then
    assertTextLine(result, props, left, " test item ")
  }
  
  it should "render text without padding" in {
    //given
    val left = 1
    val props = TextLineProps.copy(getTextLineProps)(
      left = left,
      top = 2,
      width = 12,
      text = "test item",
      focused = false,
      padding = 0
    )

    //when
    val result = createTestRenderer(<(TextLine())(^.plain := props)()).root

    //then
    assertTextLine(result, props, left, "test item")
  }
  
  it should "render long text with ellipsis" in {
    //given
    val left = 1
    val props = TextLineProps.copy(getTextLineProps)(
      left = left,
      top = 2,
      width = 12,
      text = "test long item"
    )

    //when
    val result = createTestRenderer(<(TextLine())(^.plain := props)()).root

    //then
    assertTextLine(result, props, left, " tes...item ")
  }
  
  private def getTextLineProps: TextLineProps = TextLineProps(
    align = TextAlign.left,
    left = 1,
    top = 2,
    width = 10,
    text = "test item",
    style = new BlessedStyle {
      override val fg = "white"
      override val bg = "blue"
      override val focus = new BlessedStyle {}
    }
  )

  private def assertTextLine(result: TestInstance,
                             props: TextLineProps,
                             left: Int,
                             text: String): Unit = {
    
    if (text.nonEmpty) {
      assertComponents(result.children, List(
        <.text(
          ^.rbWidth := text.length,
          ^.rbHeight := 1,
          ^.rbLeft := left,
          ^.rbTop := props.top,
          ^.rbStyle := {
            if (props.focused.getOrElse(false)) props.style.focus.orNull
            else props.style
          },
          ^.content := text
        )()
      ))
    }
    else result.children.toList shouldBe Nil
  }
}
