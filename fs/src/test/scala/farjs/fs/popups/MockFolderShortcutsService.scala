package farjs.fs.popups

import scala.scalajs.js

//noinspection NotImplementedCode
class MockFolderShortcutsService(
  getAllMock: () => js.Promise[js.Array[js.UndefOr[String]]] = () => ???,
  saveMock: (Int, String) => js.Promise[Unit] = (_, _) => ???,
  deleteMock: Int => js.Promise[Unit] = _ => ???
) extends FolderShortcutsService {

  override def getAll(): js.Promise[js.Array[js.UndefOr[String]]] = getAllMock()

  override def save(index: Int, path: String): js.Promise[Unit] = saveMock(index, path)
  
  override def delete(index: Int): js.Promise[Unit] = deleteMock(index)
}
