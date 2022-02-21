package farjs.filelist.copy

import scommons.nodejs.{FS, Stats}

import scala.concurrent.Future

//noinspection NotImplementedCode
class MockFS(
  readdirMock: String => Future[Seq[String]] = _ => ???,
  renameMock: (String, String) => Future[Unit] = (_, _) => ???,
  lstatSyncMock: String => Stats = _ => ???,
  existsSyncMock: String => Boolean = _ => ???
) extends FS {

  override def readdir(path: String): Future[Seq[String]] =
    readdirMock(path)
  
  override def rename(oldPath: String, newPath: String): Future[Unit] =
    renameMock(oldPath, newPath)

  override def lstatSync(path: String): Stats =
    lstatSyncMock(path)
    
  override def existsSync(path: String): Boolean =
    existsSyncMock(path)
}
