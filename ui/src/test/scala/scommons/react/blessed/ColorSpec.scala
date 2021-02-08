package scommons.react.blessed

import scommons.nodejs.test.TestSpec

class ColorSpec extends TestSpec {

  it should "provide Color enum" in {
    //when & then
    Color.black shouldBe "black"
    Color.red shouldBe "red"
    Color.green shouldBe "green"
    Color.yellow shouldBe "yellow"
    Color.blue shouldBe "blue"
    Color.magenta shouldBe "magenta"
    Color.cyan shouldBe "cyan"
    Color.white shouldBe "white"
    Color.lightblack shouldBe "lightblack"
    Color.lightred shouldBe "lightred"
    Color.lightgreen shouldBe "lightgreen"
    Color.lightyellow shouldBe "lightyellow"
    Color.lightblue shouldBe "lightblue"
    Color.lightmagenta shouldBe "lightmagenta"
    Color.lightcyan shouldBe "lightcyan"
    Color.lightwhite shouldBe "lightwhite"
    Color.grey shouldBe "grey"
    Color.lightgrey shouldBe "lightgrey"
  }
}
