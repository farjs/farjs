package farjs.ui

import scommons.react.blessed._
import scommons.react.test._

class ProgressBarSpec extends TestSpec with TestRendererUtils {

  it should "render component" in {
    //given
    val props = ProgressBarProps(
      percent = 15,
      pos = (1, 2),
      length = 9,
      style = new BlessedStyle {
        override val fg = "white"
        override val bg = "blue"
      }
    )

    //when
    val result = createTestRenderer(<(ProgressBar())(^.wrapped := props)()).root

    //then
    assertProgressBar(result, props)
  }

  it should "return filled length when filledLength" in {
    //when & then
    ProgressBar.filledLength(99, 1) shouldBe 0
    ProgressBar.filledLength(100, 1) shouldBe 1
    ProgressBar.filledLength(9, 10) shouldBe 0
    ProgressBar.filledLength(10, 10) shouldBe 1
    ProgressBar.filledLength(20, 10) shouldBe 2
  }

  private def assertProgressBar(result: TestInstance, props: ProgressBarProps): Unit = {
    val (left, top) = props.pos
    
    inside(result.children.toList) { case List(text) =>
      assertNativeComponent(text,
        <.text(
          ^.rbHeight := 1,
          ^.rbLeft := left,
          ^.rbTop := top,
          ^.rbStyle := props.style,
          ^.content := "█░░░░░░░░"
        )()
      )
    }
  }
}
