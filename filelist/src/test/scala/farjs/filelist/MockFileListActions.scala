package farjs.filelist

import farjs.filelist.api._
import farjs.ui.Dispatch
import farjs.ui.task.TaskAction

import scala.concurrent.Future
import scala.scalajs.js

//noinspection NotImplementedCode
class MockFileListActions(
  val api: FileListApi = new MockFileListApi(),
  changeDirMock: (Dispatch, String, String) => TaskAction = (_, _, _) => ???,
  updateDirMock: (Dispatch, String) => TaskAction = (_, _) => ???,
  createDirMock: (Dispatch, String, String, Boolean) => TaskAction = (_, _, _, _) => ???,
  deleteItemsMock: (Dispatch, String, Seq[FileListItem]) => TaskAction = (_, _, _) => ???,
  scanDirsMock: (String, Seq[FileListItem], (String, Seq[FileListItem]) => Boolean) => Future[Boolean] = (_, _, _) => ???,
  copyFileMock: (String, FileListItem, Future[js.UndefOr[FileTarget]],
    Double => Future[Boolean]) => Future[Boolean] = (_, _, _, _) => ???
) extends FileListActions {

  override def changeDir(dispatch: Dispatch,
                         path: String,
                         dir: String): TaskAction = {

    changeDirMock(dispatch, path, dir)
  }
  
  override def updateDir(dispatch: Dispatch, path: String): TaskAction = {
    updateDirMock(dispatch, path)
  }

  override def createDir(dispatch: Dispatch,
                         parent: String,
                         dir: String,
                         multiple: Boolean): TaskAction = {

    createDirMock(dispatch, parent, dir, multiple)
  }
  
  override def deleteItems(dispatch: Dispatch,
                           parent: String,
                           items: Seq[FileListItem]): TaskAction = {

    deleteItemsMock(dispatch, parent, items)
  }
  
  override def scanDirs(parent: String,
                        items: Seq[FileListItem],
                        onNextDir: (String, Seq[FileListItem]) => Boolean): Future[Boolean] = {

    scanDirsMock(parent, items, onNextDir)
  }
  
  override def copyFile(srcDir: String,
                        srcItem: FileListItem,
                        dstFileF: Future[js.UndefOr[FileTarget]],
                        onProgress: Double => Future[Boolean]): Future[Boolean] = {

    copyFileMock(srcDir, srcItem, dstFileF, onProgress)
  }
}
