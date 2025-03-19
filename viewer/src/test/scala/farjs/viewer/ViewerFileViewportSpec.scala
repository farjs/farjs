package farjs.viewer

import farjs.viewer.ViewerFileReaderSpec.assertViewerFileLines
import scommons.nodejs.test.TestSpec

import scala.scalajs.js

class ViewerFileViewportSpec extends TestSpec {

  it should "handle unicode characters when content" in {
    //given
    val viewport = ViewerFileViewport(
      fileReader = new MockViewerFileReader,
      encoding = "win",
      size = 123,
      width = 10,
      height = 3,
      linesData = js.Array(
        ViewerFileLine("Валютный 123", 1)
      )
    )
    
    //when & then
    viewport.content shouldBe {
      "Валютный 1" + "\n"
    }
  }

  it should "replace control characters with spaces when content" in {
    //given
    val viewport = ViewerFileViewport(
      fileReader = new MockViewerFileReader,
      encoding = "win",
      size = 123,
      width = 15,
      height = 3,
      linesData = js.Array(
        ViewerFileLine("\t\rline1\n\u0000\u0008\u001b", 1),
        ViewerFileLine("\u007fline2", 2)
      )
    )
    
    //when & then
    viewport.content shouldBe {
      "\t\rline1\n   " + "\n" +
        " line2" + "\n"
    }
  }

  it should "return list of indexes of long lines when scrollIndicators" in {
    //given
    val viewport = ViewerFileViewport(
      fileReader = new MockViewerFileReader,
      encoding = "win",
      size = 123,
      width = 10,
      height = 3,
      column = 1,
      linesData = js.Array(
        ViewerFileLine("Валютный 12", 1),
        ViewerFileLine("Валютный 123", 2)
      )
    )

    //when & then
    viewport.scrollIndicators.toList shouldBe List(1)
  }

  it should "return input data if wrap=false when doWrap" in {
    //given
    val viewport = ViewerFileViewport(
      fileReader = new MockViewerFileReader,
      encoding = "win",
      size = 123,
      width = 10,
      height = 3
    )
    val data = js.Array(ViewerFileLine("test line 1", 1))

    //when & then
    viewport.doWrap(1, up = false)(data) shouldBe data
  }

  it should "return wrapped data if up=false when doWrap" in {
    //given
    val viewport = ViewerFileViewport(
      fileReader = new MockViewerFileReader,
      encoding = "utf-8",
      size = 123,
      width = 6,
      height = 3,
      wrap = true
    )
    val data = js.Array(
      ViewerFileLine("test1 2", 30),
      ViewerFileLine("test3 4", 40)
    )

    //when
    val results = viewport.doWrap(3, up = false)(data)
    
    //then
    assertViewerFileLines(results, List(
      ViewerFileLine("test1 ", 6),
      ViewerFileLine("2", 24),
      ViewerFileLine("test3 ", 6)
    ))
  }

  it should "return wrapped data if up=true when doWrap" in {
    //given
    val viewport = ViewerFileViewport(
      fileReader = new MockViewerFileReader,
      encoding = "utf-8",
      size = 123,
      width = 6,
      height = 3,
      wrap = true
    )
    val data = js.Array(
      ViewerFileLine("test1 2", 30),
      ViewerFileLine("test3 4", 40)
    )

    //when
    val results = viewport.doWrap(3, up = true)(data)
    
    //then
    assertViewerFileLines(results, List(
      ViewerFileLine("est1 2", 6),
      ViewerFileLine("t", 34),
      ViewerFileLine("est3 4", 6)
    ))
  }
}
