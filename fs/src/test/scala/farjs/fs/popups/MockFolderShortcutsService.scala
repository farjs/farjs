package farjs.fs.popups

import scala.concurrent.Future

//noinspection NotImplementedCode
class MockFolderShortcutsService(
  getAllMock: () => Future[Seq[Option[String]]] = () => ???,
  saveMock: (Int, String) => Future[Unit] = (_, _) => ???,
  deleteMock: Int => Future[Unit] = _ => ???
) extends FolderShortcutsService {

  override def getAll: Future[Seq[Option[String]]] = getAllMock()

  override def save(index: Int, path: String): Future[Unit] = saveMock(index, path)
  
  override def delete(index: Int): Future[Unit] = deleteMock(index)
}
