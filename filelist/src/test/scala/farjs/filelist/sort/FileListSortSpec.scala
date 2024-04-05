package farjs.filelist.sort

import org.scalactic.source.Position
import org.scalatest.Assertion
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

object FileListSortSpec {

  def assertFileListSort(result: FileListSort, expected: FileListSort)(implicit position: Position): Assertion = {
    inside(result) {
      case FileListSort(mode, asc) =>
        mode shouldBe expected.mode
        asc shouldBe expected.asc
    }
  }  
}
