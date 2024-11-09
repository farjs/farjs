package farjs.file

import farjs.file.FileViewHistory._
import org.scalactic.source.Position
import org.scalatest.Assertion
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import scommons.nodejs.test.TestSpec

class FileViewHistorySpec extends TestSpec {

  it should "convert path to item" in {
    //when & then
    pathToItem("test/path", isEdit = false) shouldBe "V:test/path"
    pathToItem("test/path", isEdit = true) shouldBe "E:test/path"
  }  

  it should "convert item to path" in {
    //when & then
    itemToPath("V:test/path") shouldBe "test/path"
    itemToPath("E:test/path") shouldBe "test/path"
    itemToPath("D:test/path") shouldBe "D:test/path"
  }  
}

object FileViewHistorySpec {

  def assertFileViewHistory(result: FileViewHistory, expected: FileViewHistory)(implicit position: Position): Assertion = {
    inside(result) {
      case FileViewHistory(path, params) =>
        path shouldBe expected.path
        assertFileViewHistoryParams(params, expected.params)
    }
  }

  def assertFileViewHistoryParams(result: FileViewHistoryParams, expected: FileViewHistoryParams)(implicit position: Position): Assertion = {
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
