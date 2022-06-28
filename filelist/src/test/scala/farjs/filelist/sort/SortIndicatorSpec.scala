package farjs.filelist.sort

import farjs.filelist.sort.SortIndicator._
import farjs.ui.theme.Theme
import scommons.react.blessed._
import scommons.react.test._

class SortIndicatorSpec extends TestSpec with TestRendererUtils {

  it should "render component" in {
    //given
    val props = SortIndicatorProps(SortMode.Name, ascending = true)

    //when
    val result = testRender(<(SortIndicator())(^.wrapped := props)())

    //then
    assertNativeComponent(result,
      <.text(
        ^.rbWidth := 2,
        ^.rbHeight := 1,
        ^.rbLeft := 1,
        ^.rbTop := 1,
        ^.rbStyle := Theme.current.fileList.header,
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
