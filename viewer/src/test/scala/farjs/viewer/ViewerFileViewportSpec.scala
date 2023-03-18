package farjs.viewer

import scommons.nodejs.test.TestSpec

class ViewerFileViewportSpec extends TestSpec {

  it should "handle unicode characters when content" in {
    //given
    val viewport = ViewerFileViewport(
      fileReader = new MockViewerFileReader,
      encoding = "win",
      size = 123,
      width = 10,
      height = 3,
      linesData = List(
        "Валютный 123" -> 1
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
      linesData = List(
        "\t\rline1\n\u0000\u0008\u001b" -> 1,
        "\u007fline2" -> 2
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
      linesData = List(
        "Валютный 12" -> 1,
        "Валютный 123" -> 2
      )
    )

    //when & then
    viewport.scrollIndicators shouldBe List(1)
  }
}
