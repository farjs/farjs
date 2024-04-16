package farjs.filelist

import farjs.filelist.api._
import farjs.ui.Dispatch
import farjs.ui.task.TaskAction

import scala.concurrent.Future
import scala.scalajs.js

//noinspection NotImplementedCode
class MockFileListActions(
  isLocalFSMock: Boolean = true,
  capabilitiesMock: js.Set[FileListCapability] = js.Set.empty,
  getDriveRootMock: String => Future[Option[String]] = _ => ???,
  changeDirMock: (Dispatch, Option[String], String) => TaskAction = (_, _, _) => ???,
  updateDirMock: (Dispatch, String) => TaskAction = (_, _) => ???,
  createDirMock: (Dispatch, String, String, Boolean) => TaskAction = (_, _, _, _) => ???,
  mkDirsMock: List[String] => Future[String] = _ => ???,
  readDirMock: (Option[String], String) => Future[FileListDir] = (_, _) => ???,
  deleteMock: (String, Seq[FileListItem]) => Future[Unit] = (_, _) => ???,
  deleteActionMock: (Dispatch, String, Seq[FileListItem]) => TaskAction = (_, _, _) => ???,
  scanDirsMock: (String, Seq[FileListItem], (String, Seq[FileListItem]) => Boolean) => Future[Boolean] = (_, _, _) => ???,
  writeFileMock: (List[String], String,
    FileListItem => js.Promise[js.UndefOr[Boolean]]) => Future[js.UndefOr[FileTarget]] = (_, _, _) => ???,
  readFileMock: (List[String], FileListItem, Double) => Future[FileSource] = (_, _, _) => ???,
  copyFileMock: (List[String], FileListItem, Future[js.UndefOr[FileTarget]],
    Double => Future[Boolean]) => Future[Boolean] = (_, _, _, _) => ???
) extends FileListActions {

  protected def api: FileListApi = ???

  val isLocalFS: Boolean = isLocalFSMock

  override val capabilities: js.Set[FileListCapability] = capabilitiesMock

  override def getDriveRoot(path: String): Future[Option[String]] =
    getDriveRootMock(path)

  override def changeDir(dispatch: Dispatch,
                         parent: Option[String],
                         dir: String): TaskAction = {

    changeDirMock(dispatch, parent, dir)
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
  
  override def mkDirs(dirs: List[String]): Future[String] = mkDirsMock(dirs)

  override def readDir(parent: Option[String], dir: String): Future[FileListDir] = readDirMock(parent, dir)

  override def delete(parent: String, items: Seq[FileListItem]): Future[Unit] = deleteMock(parent, items)

  override def deleteAction(dispatch: Dispatch,
                            dir: String,
                            items: Seq[FileListItem]): TaskAction = {

    deleteActionMock(dispatch, dir, items)
  }
  
  override def scanDirs(parent: String,
                        items: Seq[FileListItem],
                        onNextDir: (String, Seq[FileListItem]) => Boolean): Future[Boolean] = {

    scanDirsMock(parent, items, onNextDir)
  }
  
  override def writeFile(parentDirs: List[String],
                         fileName: String,
                         onExists: FileListItem => js.Promise[js.UndefOr[Boolean]]): Future[js.UndefOr[FileTarget]] = {

    writeFileMock(parentDirs, fileName, onExists)
  }

  override def readFile(parentDirs: List[String], file: FileListItem, position: Double): Future[FileSource] =
    readFileMock(parentDirs, file, position)
  
  override def copyFile(srcDirs: List[String],
                        srcItem: FileListItem,
                        dstFileF: Future[js.UndefOr[FileTarget]],
                        onProgress: Double => Future[Boolean]): Future[Boolean] = {

    copyFileMock(srcDirs, srcItem, dstFileF, onProgress)
  }
}
