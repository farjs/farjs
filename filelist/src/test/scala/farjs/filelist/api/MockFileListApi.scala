package farjs.filelist.api

import scala.concurrent.Future

//noinspection NotImplementedCode
class MockFileListApi(
  readDir2Mock: (Option[String], String) => Future[FileListDir] = (_, _) => ???,
  readDirMock: String => Future[FileListDir] = _ => ???,
  deleteMock: (String, Seq[FileListItem]) => Future[Unit] = (_, _) => ???,
  mkDirsMock: List[String] => Future[Unit] = _ => ???,
  readFileMock: (List[String], FileListItem, Double) => Future[FileSource] = (_, _, _) => ???,
  writeFileMock: (List[String], String, FileListItem => Future[Option[Boolean]]) => Future[Option[FileTarget]] = (_, _, _) => ???
) extends FileListApi {

  override def readDir(parent: Option[String], dir: String): Future[FileListDir] =
    readDir2Mock(parent, dir)

  override def readDir(targetDir: String): Future[FileListDir] =
    readDirMock(targetDir)

  override def delete(parent: String, items: Seq[FileListItem]): Future[Unit] =
    deleteMock(parent, items)

  override def mkDirs(dirs: List[String]): Future[Unit] =
    mkDirsMock(dirs)

  override def readFile(parentDirs: List[String], file: FileListItem, position: Double): Future[FileSource] =
    readFileMock(parentDirs, file, position)

  override def writeFile(parentDirs: List[String],
                         fileName: String,
                         onExists: FileListItem => Future[Option[Boolean]]): Future[Option[FileTarget]] = {

    writeFileMock(parentDirs, fileName, onExists)
  }
}
