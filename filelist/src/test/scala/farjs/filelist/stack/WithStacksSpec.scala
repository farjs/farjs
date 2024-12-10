package farjs.filelist.stack

import org.scalatest.Assertion
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import scommons.react._

object WithStacksSpec {

  def withContext(element: ReactElement,
                  left: WithStacksData,
                  right: WithStacksData): ReactElement = {

    <(WithStacks.Context.Provider)(^.contextValue := WithStacksProps(
      left = left,
      right = right
    ))(
      element
    )
  }
  
  def assertStacks(result: WithStacksProps, expected: WithStacksProps): Assertion = {
    inside(result) {
      case WithStacksProps(left, right) =>
        left shouldBe expected.left
        right shouldBe expected.right
    }
  }
}
