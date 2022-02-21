package farjs.app.filelist.fs

import scommons.nodejs.Process.Platform

import scala.concurrent.Future

//noinspection NotImplementedCode
class MockFSService(
  openItemMock: (String, String) => Future[Unit] = (_, _) => ???,
  readDiskMock: String => Future[Option[FSDisk]] = _ => ???,
  readDisksMock: () => Future[List[FSDisk]] = () => ???
) extends FSService(Platform.darwin, new MockChildProcess()) {

  override def openItem(parent: String, item: String): Future[Unit] =
    openItemMock(parent, item)
    
  override def readDisk(path: String): Future[Option[FSDisk]] =
    readDiskMock(path)
    
  override def readDisks(): Future[List[FSDisk]] =
    readDisksMock()
}
