package farjs.ui

import farjs.ui.UI.splitText
import scommons.nodejs.test.TestSpec

class UISpec extends TestSpec {

  it should "split text when splitText" in {
    //when & then
    splitText("", 2) shouldBe List("")
    splitText("test", 2) shouldBe List("test")
    splitText("test1, test2", 11) shouldBe List("test1,", "test2")
    splitText("test1, test2", 12) shouldBe List("test1, test2")
    splitText("test1, test2, test3", 12) shouldBe List("test1,", "test2, test3")
    splitText("test1, test2, test3", 13) shouldBe List("test1, test2,", "test3")
    splitText("test1, \n\n test2, test3", 13) shouldBe List("test1,", "", "test2, test3")
  }
}
