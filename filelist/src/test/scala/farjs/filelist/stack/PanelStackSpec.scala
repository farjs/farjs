package farjs.filelist.stack

import org.scalactic.source.Position
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

object PanelStackSpec {

  def assertPanelStackItem(result: PanelStackItem[_], expected: PanelStackItem[_])(implicit position: Position): Assertion = {
    result.component shouldBe expected.component
    result.dispatch shouldBe expected.dispatch
    result.actions shouldBe expected.actions
    result.state shouldBe expected.state
  }
}
