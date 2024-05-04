package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.api.FileListDirSpec.assertFileListDir
import org.scalactic.source.Position
import org.scalatest.Assertion
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

object FileListActionsSpec {

  def assertFileListParamsChangedAction(action: Any, expected: FileListParamsChangedAction)(implicit position: Position): Assertion = {
    inside(action.asInstanceOf[FileListParamsChangedAction]) {
      case FileListParamsChangedAction(offset, index, selectedNames) =>
        offset shouldBe expected.offset
        index shouldBe expected.index
        selectedNames.toSet shouldBe expected.selectedNames.toSet
    }
  }

  def assertFileListDirChangedAction(action: Any, expected: FileListDirChangedAction)(implicit position: Position): Assertion = {
    inside(action.asInstanceOf[FileListDirChangedAction]) {
      case FileListDirChangedAction(dir, currDir) =>
        dir shouldBe expected.dir
        assertFileListDir(currDir, expected.currDir)
    }
  }

  def assertFileListDirUpdatedAction(action: Any, expected: FileListDirUpdatedAction)(implicit position: Position): Assertion = {
    inside(action.asInstanceOf[FileListDirUpdatedAction]) {
      case FileListDirUpdatedAction(currDir) =>
        assertFileListDir(currDir, expected.currDir)
    }
  }

  def assertFileListItemCreatedAction(action: Any, expected: FileListItemCreatedAction)(implicit position: Position): Assertion = {
    inside(action.asInstanceOf[FileListItemCreatedAction]) {
      case FileListItemCreatedAction(name, currDir) =>
        name shouldBe expected.name
        assertFileListDir(currDir, expected.currDir)
    }
  }

  def assertFileListDiskSpaceUpdatedAction(action: Any, expected: FileListDiskSpaceUpdatedAction)(implicit position: Position): Assertion = {
    inside(action.asInstanceOf[FileListDiskSpaceUpdatedAction]) {
      case FileListDiskSpaceUpdatedAction(diskSpace) =>
        diskSpace shouldBe expected.diskSpace
    }
  }

  def assertFileListSortAction(action: Any, expected: FileListSortAction)(implicit position: Position): Assertion = {
    inside(action.asInstanceOf[FileListSortAction]) {
      case FileListSortAction(mode) =>
        mode shouldBe expected.mode
    }
  }
}
