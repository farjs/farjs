package farjs.filelist.api

import farjs.filelist.api.FileListItemSpec.assertFileListItems
import org.scalactic.source.Position
import org.scalatest.Assertion
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

object FileListDirSpec {

  def assertFileListDir(result: FileListDir, expected: FileListDir)(implicit position: Position): Assertion = {
    inside(result) {
      case FileListDir(path, isRoot, items) =>
        path shouldBe expected.path
        isRoot shouldBe expected.isRoot
        assertFileListItems(items.toList, expected.items.toList)
    }
  }
}
