package farjs.viewer.quickview

import org.scalatest.Assertion
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

object QuickViewDirSpec {
  
  def assertQuickViewParams(result: QuickViewParams, expected: QuickViewParams): Assertion = {
    inside(result) {
      case QuickViewParams(name, parent, folders, files, filesSize) =>
        name shouldBe expected.name
        parent shouldBe expected.parent
        folders shouldBe expected.folders
        files shouldBe expected.files
        filesSize shouldBe expected.filesSize
    }
  }
}
