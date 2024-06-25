package farjs.file

import org.scalactic.source.Position
import org.scalatest.Assertion
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

object FileViewHistorySpec {

  def assertFileViewHistory(result: FileViewHistory, expected: FileViewHistory)(implicit position: Position): Assertion = {
    inside(result) {
      case FileViewHistory(path, params) =>
        path shouldBe expected.path
        assertFileViewHistoryParams(params, expected.params)
    }
  }

  private def assertFileViewHistoryParams(result: FileViewHistoryParams, expected: FileViewHistoryParams)(implicit position: Position): Assertion = {
    inside(result) {
      case FileViewHistoryParams(isEdit, encoding, position, wrap, column) =>
        isEdit shouldBe expected.isEdit
        encoding shouldBe expected.encoding
        position shouldBe expected.position
        wrap shouldBe expected.wrap
        column shouldBe expected.column
    }
  }
}
