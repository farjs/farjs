package farjs.viewer

import scommons.nodejs.test.TestSpec

class ViewerFileViewportSpec extends TestSpec {

  it should "replace control characters with spaces" in {
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
}
