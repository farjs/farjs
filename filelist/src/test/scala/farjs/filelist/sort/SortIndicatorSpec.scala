package farjs.filelist.sort

import farjs.filelist.sort.SortIndicator._
import farjs.filelist.stack.PanelStack
import farjs.filelist.stack.PanelStackCompSpec.withContext
import farjs.filelist.theme.FileListTheme
import farjs.filelist.theme.FileListThemeSpec.withThemeContext
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
    val stack = new PanelStack(isActive = true, js.Array(), null)
    val props = SortIndicatorProps(FileListSort(SortMode.Name, asc = true))
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
    val stack = new PanelStack(isActive = false, js.Array(), null)
    val props = SortIndicatorProps(FileListSort(SortMode.Name, asc = true))
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
    val stack = new PanelStack(isActive = true, js.Array(), null)
    val props = SortIndicatorProps(FileListSort(SortMode.Name, asc = true))

    //when
    val result = testRender(withContext(
      withThemeContext(<(SortIndicator())(^.wrapped := props)()), stack = stack
    ))

    //then
    val currTheme = FileListTheme.defaultTheme
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
    getIndicator(FileListSort(SortMode.Name, asc = true)) shouldBe "n"
    getIndicator(FileListSort(SortMode.Name, asc = false)) shouldBe "N"
    getIndicator(FileListSort(SortMode.Extension, asc = true)) shouldBe "x"
    getIndicator(FileListSort(SortMode.Extension, asc = false)) shouldBe "X"
    getIndicator(FileListSort(SortMode.ModificationTime, asc = true)) shouldBe "m"
    getIndicator(FileListSort(SortMode.ModificationTime, asc = false)) shouldBe "M"
    getIndicator(FileListSort(SortMode.Size, asc = true)) shouldBe "s"
    getIndicator(FileListSort(SortMode.Size, asc = false)) shouldBe "S"
    getIndicator(FileListSort(SortMode.Unsorted, asc = true)) shouldBe "u"
    getIndicator(FileListSort(SortMode.Unsorted, asc = false)) shouldBe "U"
    getIndicator(FileListSort(SortMode.CreationTime, asc = true)) shouldBe "c"
    getIndicator(FileListSort(SortMode.CreationTime, asc = false)) shouldBe "C"
    getIndicator(FileListSort(SortMode.AccessTime, asc = true)) shouldBe "a"
    getIndicator(FileListSort(SortMode.AccessTime, asc = false)) shouldBe "A"
  }
}
