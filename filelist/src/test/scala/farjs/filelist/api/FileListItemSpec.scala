package farjs.filelist.api

import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

object FileListItemSpec {

  def assertFileListItems(result: Seq[FileListItem], expected: List[FileListItem])(implicit position: Position): Assertion = {
    result.size shouldBe expected.size
    result.zip(expected).foreach { case (res, item) =>
      assertFileListItem(res, item)
    }
    Succeeded
  }

  def assertFileListItem(result: FileListItem, expected: FileListItem)(implicit position: Position): Assertion = {
    inside(result) {
      case FileListItem(name, isDir, isSymLink, size, atimeMs, mtimeMs, ctimeMs, birthtimeMs, permissions) =>
        name shouldBe expected.name
        isDir shouldBe expected.isDir
        isSymLink shouldBe expected.isSymLink
        size shouldBe expected.size
        atimeMs shouldBe expected.atimeMs
        mtimeMs shouldBe expected.mtimeMs
        ctimeMs shouldBe expected.ctimeMs
        birthtimeMs shouldBe expected.birthtimeMs
        permissions shouldBe expected.permissions
    }
  }
}
