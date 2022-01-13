package farjs.filelist

import farjs.filelist.FileListActions.{FileListDirChangeAction, FileListDirCreateAction, FileListDirUpdateAction, FileListTaskAction}
import farjs.filelist.api.{FileListApi, FileListDir, FileListItem}
import scommons.react.redux.Dispatch

import scala.concurrent.Future

//noinspection NotImplementedCode
class MockFileListActions(
  openInDefaultAppMock: (String, String) => FileListTaskAction = (_, _) => ???,
  changeDirMock: (Dispatch, Boolean, Option[String], String) => FileListDirChangeAction = (_, _, _, _) => ???,
  updateDirMock: (Dispatch, Boolean, String) => FileListDirUpdateAction = (_, _, _) => ???,
  createDirMock: (Dispatch, Boolean, String, String, Boolean) => FileListDirCreateAction = (_, _, _, _, _) => ???,
  mkDirsMock: List[String] => Future[Unit] = _ => ???,
  readDirMock: (Option[String], String) => Future[FileListDir] = (_, _) => ???,
  deleteMock: (String, Seq[FileListItem]) => Future[Unit] = (_, _) => ???,
  deleteActionMock: (Dispatch, Boolean, String, Seq[FileListItem]) => FileListTaskAction = (_, _, _, _) => ???,
  scanDirsMock: (String, Seq[FileListItem], (String, Seq[FileListItem]) => Boolean) => Future[Boolean] = (_, _, _) => ???,
  copyFileMock: (List[String], FileListItem, List[String], String,
    FileListItem => Future[Option[Boolean]], Double => Future[Boolean]) => Future[Boolean] = (_, _, _, _, _, _) => ???
) extends FileListActions {

  protected def api: FileListApi = ???

  override def openInDefaultApp(parent: String, item: String): FileListTaskAction =
    openInDefaultAppMock(parent, item)

  override def changeDir(dispatch: Dispatch,
                         isRight: Boolean,
                         parent: Option[String],
                         dir: String): FileListDirChangeAction = {

    changeDirMock(dispatch, isRight, parent, dir)
  }
  
  override def updateDir(dispatch: Dispatch,
                         isRight: Boolean,
                         path: String): FileListDirUpdateAction = {

    updateDirMock(dispatch, isRight, path)
  }

  override def createDir(dispatch: Dispatch,
                         isRight: Boolean,
                         parent: String,
                         dir: String,
                         multiple: Boolean): FileListDirCreateAction = {

    createDirMock(dispatch, isRight, parent, dir, multiple)
  }
  
  override def mkDirs(dirs: List[String]): Future[Unit] = mkDirsMock(dirs)

  override def readDir(parent: Option[String], dir: String): Future[FileListDir] = readDirMock(parent, dir)

  override def delete(parent: String, items: Seq[FileListItem]): Future[Unit] = deleteMock(parent, items)

  override def deleteAction(dispatch: Dispatch,
                            isRight: Boolean,
                            dir: String,
                            items: Seq[FileListItem]): FileListTaskAction = {

    deleteActionMock(dispatch, isRight, dir, items)
  }
  
  override def scanDirs(parent: String,
                        items: Seq[FileListItem],
                        onNextDir: (String, Seq[FileListItem]) => Boolean): Future[Boolean] = {

    scanDirsMock(parent, items, onNextDir)
  }
  
  override def copyFile(srcDirs: List[String],
                        srcItem: FileListItem,
                        dstDirs: List[String],
                        dstName: String,
                        onExists: FileListItem => Future[Option[Boolean]],
                        onProgress: Double => Future[Boolean]): Future[Boolean] = {

    copyFileMock(srcDirs, srcItem, dstDirs, dstName, onExists, onProgress)
  }
}