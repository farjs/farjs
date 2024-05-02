package farjs.filelist

import farjs.filelist.api._
import farjs.ui.Dispatch
import farjs.ui.task.TaskAction

import scala.scalajs.js

//noinspection NotImplementedCode
class MockFileListActions(
  api: FileListApi = new MockFileListApi(),
  changeDirMock: (Dispatch, String, String) => TaskAction = (_, _, _) => ???,
  updateDirMock: (Dispatch, String) => TaskAction = (_, _) => ???,
  createDirMock: (Dispatch, String, String, Boolean) => TaskAction = (_, _, _, _) => ???,
  deleteItemsMock: (Dispatch, String, js.Array[FileListItem]) => TaskAction = (_, _, _) => ???,
  scanDirsMock: (String, js.Array[FileListItem], js.Function2[String, js.Array[FileListItem], Boolean]) => js.Promise[Boolean] = (_, _, _) => ???,
  copyFileMock: (String, FileListItem, js.Promise[js.UndefOr[FileTarget]],
    js.Function1[Double, js.Promise[Boolean]]) => js.Promise[Boolean] = (_, _, _, _) => ???
) extends FileListActions(api) {

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
                           items: js.Array[FileListItem]): TaskAction = {

    deleteItemsMock(dispatch, parent, items)
  }
  
  override def scanDirs(parent: String,
                        items: js.Array[FileListItem],
                        onNextDir: js.Function2[String, js.Array[FileListItem], Boolean]): js.Promise[Boolean] = {

    scanDirsMock(parent, items, onNextDir)
  }
  
  override def copyFile(srcDir: String,
                        srcItem: FileListItem,
                        dstFileF: js.Promise[js.UndefOr[FileTarget]],
                        onProgress: js.Function1[Double, js.Promise[Boolean]]): js.Promise[Boolean] = {

    copyFileMock(srcDir, srcItem, dstFileF, onProgress)
  }
}
