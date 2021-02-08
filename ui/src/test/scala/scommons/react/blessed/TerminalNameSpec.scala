package scommons.react.blessed

import scommons.nodejs.test.TestSpec

class TerminalNameSpec extends TestSpec {

  it should "provide TerminalName enum" in {
    //when & then
    TerminalName.`xterm-256color` shouldBe "xterm-256color"
  }
}
