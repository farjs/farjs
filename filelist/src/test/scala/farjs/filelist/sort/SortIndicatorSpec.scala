package farjs.filelist.sort

import farjs.filelist.sort.SortIndicator._
import farjs.filelist.stack.PanelStack
import farjs.filelist.stack.PanelStackSpec.withContext
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import scommons.nodejs._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class SortIndicatorSpec extends TestSpec with TestRendererUtils {

  it should "emit keypress event when onClick in Left panel" in {
    //given
    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)

    val isRight = false
    val stack = new PanelStack(isActive = true, Nil, null)
    val props = SortIndicatorProps(SortMode.Name, ascending = true)
    val textElem = testRender(withContext(
      withThemeContext(<(SortIndicator())(^.wrapped := props)()), stack = stack, isRight = isRight
    ))

    //then
    onKey.expects("l", false, true, false)

    //when
    textElem.props.onClick(null)

    //cleanup
    process.stdin.removeListener("keypress", listener)
  }

  it should "emit keypress event when onClick in Right panel" in {
    //given
    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)

    val isRight = true
    val stack = new PanelStack(isActive = false, Nil, null)
    val props = SortIndicatorProps(SortMode.Name, ascending = true)
    val textElem = testRender(withContext(
      withThemeContext(<(SortIndicator())(^.wrapped := props)()), stack = stack, isRight = isRight
    ))

    //then
    onKey.expects("r", false, true, false)

    //when
    textElem.props.onClick(null)

    //cleanup
    process.stdin.removeListener("keypress", listener)
  }

  it should "render component" in {
    //given
    val stack = new PanelStack(isActive = true, Nil, null)
    val props = SortIndicatorProps(SortMode.Name, ascending = true)

    //when
    val result = testRender(withContext(
      withThemeContext(<(SortIndicator())(^.wrapped := props)()), stack = stack
    ))

    //then
    val currTheme = DefaultTheme
    assertNativeComponent(result,
      <.text(
        ^.rbWidth := 2,
        ^.rbHeight := 1,
        ^.rbLeft := 1,
        ^.rbTop := 1,
        ^.rbAutoFocus := false,
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbStyle := currTheme.fileList.header,
        ^.content := "n "
      )()
    )
  }

  it should "return indicator name when getIndicator" in {
    //when & then
    getIndicator(SortMode.Name, ascending = true) shouldBe "n"
    getIndicator(SortMode.Name, ascending = false) shouldBe "N"
    getIndicator(SortMode.Extension, ascending = true) shouldBe "x"
    getIndicator(SortMode.Extension, ascending = false) shouldBe "X"
    getIndicator(SortMode.ModificationTime, ascending = true) shouldBe "m"
    getIndicator(SortMode.ModificationTime, ascending = false) shouldBe "M"
    getIndicator(SortMode.Size, ascending = true) shouldBe "s"
    getIndicator(SortMode.Size, ascending = false) shouldBe "S"
    getIndicator(SortMode.Unsorted, ascending = true) shouldBe "u"
    getIndicator(SortMode.Unsorted, ascending = false) shouldBe "U"
    getIndicator(SortMode.CreationTime, ascending = true) shouldBe "c"
    getIndicator(SortMode.CreationTime, ascending = false) shouldBe "C"
    getIndicator(SortMode.AccessTime, ascending = true) shouldBe "a"
    getIndicator(SortMode.AccessTime, ascending = false) shouldBe "A"
  }
}
