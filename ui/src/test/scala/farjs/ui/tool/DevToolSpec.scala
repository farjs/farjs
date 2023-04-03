package farjs.ui.tool

import farjs.ui.tool.DevTool._
import scommons.nodejs.test.TestSpec

class DevToolSpec extends TestSpec {

  it should "return correct value when shouldResize" in {
    //when & then
    shouldResize(Hidden, Hidden) shouldBe true
    shouldResize(Hidden, Logs) shouldBe true
    shouldResize(Logs, Hidden) shouldBe true
    shouldResize(Colors, Hidden) shouldBe true
    shouldResize(Logs, Logs) shouldBe false
    shouldResize(Logs, Inputs) shouldBe false
    shouldResize(Inputs, Colors) shouldBe false
  }
  
  it should "transition state from Hidden to Hidden when getNext" in {
    //given
    var state: DevTool = Hidden.getNext
    state should not be Hidden
    
    //when
    var count = 1
    while (state != Hidden && count < 10) {
      count += 1
      state = state.getNext
    }

    //then
    state shouldBe Hidden
    count shouldBe 4
  }
}
