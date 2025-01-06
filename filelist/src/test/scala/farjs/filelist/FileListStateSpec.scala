package farjs.filelist

import farjs.filelist.api.FileListDirSpec.assertFileListDir
import farjs.filelist.sort.FileListSortSpec.assertFileListSort
import org.scalactic.source.Position
import org.scalatest.Assertion
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

object FileListStateSpec {

  def assertFileListState(result: FileListState, expected: FileListState)(implicit position: Position): Assertion = {
    inside(result) {
      case FileListState(offset, index, currDir, selectedNames, diskSpace, sort) =>
        offset shouldBe expected.offset
        index shouldBe expected.index
        assertFileListDir(currDir, expected.currDir)
        selectedNames.toSet shouldBe expected.selectedNames.toSet
        diskSpace shouldBe expected.diskSpace
        assertFileListSort(sort, expected.sort)
    }
  }  
}
