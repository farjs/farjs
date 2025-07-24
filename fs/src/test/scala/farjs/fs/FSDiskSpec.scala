package farjs.fs

import org.scalactic.source.Position
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.{Assertion, Succeeded}

object FSDiskSpec {

  def assertFSDisks(result: Seq[FSDisk], expected: List[FSDisk])(implicit position: Position): Assertion = {
    result.size shouldBe expected.size
    result.zip(expected).foreach { case (res, item) =>
      assertFSDisk(res, item)
    }
    Succeeded
  }

  def assertFSDisk(result: FSDisk, expected: FSDisk)(implicit position: Position): Assertion = {
    inside(result) {
      case FSDisk(root, size, free, name) =>
        root shouldBe expected.root
        size shouldBe expected.size
        free shouldBe expected.free
        name shouldBe expected.name
    }
  }
}
