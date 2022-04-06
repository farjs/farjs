package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.api._
import scommons.react.redux.Dispatch

import scala.concurrent.Future

//noinspection NotImplementedCode
class MockFileListActions(
  isLocalFSMock: Boolean = true,
  getDriveRootMock: String => Future[Option[String]] = _ => ???,
  changeDirMock: (Dispatch, Option[String], String) => FileListDirChangeAction = (_, _, _) => ???,
  updateDirMock: (Dispatch, String) => FileListDirUpdateAction = (_, _) => ???,
  createDirMock: (Dispatch, String, String, Boolean) => FileListDirCreateAction = (_, _, _, _) => ???,
  mkDirsMock: List[String] => Future[Unit] = _ => ???,
  readDirMock: (Option[String], String) => Future[FileListDir] = (_, _) => ???,
  deleteMock: (String, Seq[FileListItem]) => Future[Unit] = (_, _) => ???,
  deleteActionMock: (Dispatch, String, Seq[FileListItem]) => FileListTaskAction = (_, _, _) => ???,
  scanDirsMock: (String, Seq[FileListItem], (String, Seq[FileListItem]) => Boolean) => Future[Boolean] = (_, _, _) => ???,
  writeFileMock: (List[String], String,
    FileListItem => Future[Option[Boolean]]) => Future[Option[FileTarget]] = (_, _, _) => ???,
  readFileMock: (List[String], FileListItem, Double) => Future[FileSource] = (_, _, _) => ???,
  copyFileMock: (List[String], FileListItem, Future[Option[FileTarget]],
    Double => Future[Boolean]) => Future[Boolean] = (_, _, _, _) => ???
) extends FileListActions {

  protected def api: FileListApi = ???

  val isLocalFS: Boolean = isLocalFSMock

  override def getDriveRoot(path: String): Future[Option[String]] =
    getDriveRootMock(path)

  override def changeDir(dispatch: Dispatch,
                         parent: Option[String],
                         dir: String): FileListDirChangeAction = {

    changeDirMock(dispatch, parent, dir)
  }
  
  override def updateDir(dispatch: Dispatch, path: String): FileListDirUpdateAction = {
    updateDirMock(dispatch, path)
  }

  override def createDir(dispatch: Dispatch,
                         parent: String,
                         dir: String,
                         multiple: Boolean): FileListDirCreateAction = {

    createDirMock(dispatch, parent, dir, multiple)
  }
  
  override def mkDirs(dirs: List[String]): Future[Unit] = mkDirsMock(dirs)

  override def readDir(parent: Option[String], dir: String): Future[FileListDir] = readDirMock(parent, dir)

  override def delete(parent: String, items: Seq[FileListItem]): Future[Unit] = deleteMock(parent, items)

  override def deleteAction(dispatch: Dispatch,
                            dir: String,
                            items: Seq[FileListItem]): FileListTaskAction = {

    deleteActionMock(dispatch, dir, items)
  }
  
  override def scanDirs(parent: String,
                        items: Seq[FileListItem],
                        onNextDir: (String, Seq[FileListItem]) => Boolean): Future[Boolean] = {

    scanDirsMock(parent, items, onNextDir)
  }
  
  override def writeFile(parentDirs: List[String],
                         fileName: String,
                         onExists: FileListItem => Future[Option[Boolean]]): Future[Option[FileTarget]] = {

    writeFileMock(parentDirs, fileName, onExists)
  }

  override def readFile(parentDirs: List[String], file: FileListItem, position: Double): Future[FileSource] =
    readFileMock(parentDirs, file, position)
  
  override def copyFile(srcDirs: List[String],
                        srcItem: FileListItem,
                        dstFileF: Future[Option[FileTarget]],
                        onProgress: Double => Future[Boolean]): Future[Boolean] = {

    copyFileMock(srcDirs, srcItem, dstFileF, onProgress)
  }
}
