package farjs.fs

import scala.scalajs.js

//noinspection NotImplementedCode
class MockFSService(
  openItemMock: (String, String) => js.Promise[Unit] = (_, _) => ???,
  readDiskMock: String => js.Promise[js.UndefOr[FSDisk]] = _ => ???,
  readDisksMock: () => js.Promise[js.Array[FSDisk]] = () => ???
) extends FSService {

  override def openItem(parent: String, item: String): js.Promise[Unit] =
    openItemMock(parent, item)
    
  override def readDisk(path: String): js.Promise[js.UndefOr[FSDisk]] =
    readDiskMock(path)
    
  override def readDisks(): js.Promise[js.Array[FSDisk]] =
    readDisksMock()
}
