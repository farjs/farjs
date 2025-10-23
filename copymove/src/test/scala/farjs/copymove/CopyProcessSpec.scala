package farjs.copymove

import farjs.filelist.api.FileListItemSpec.assertFileListItem
import org.scalactic.source.Position
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.{Assertion, Succeeded}

object CopyProcessSpec {

  def assertCopyProcessItems(result: Seq[CopyProcessItem], expected: List[CopyProcessItem])(implicit position: Position): Assertion = {
    result.size shouldBe expected.size
    result.zip(expected).foreach { case (res, item) =>
      assertCopyProcessItem(res, item)
    }
    Succeeded
  }

  def assertCopyProcessItem(result: CopyProcessItem, expected: CopyProcessItem)(implicit position: Position): Assertion = {
    inside(result) {
      case CopyProcessItem(item, toName) =>
        assertFileListItem(item, expected.item)
        toName shouldBe expected.toName
    }
  }
}
