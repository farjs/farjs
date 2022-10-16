package farjs.filelist.history

import scala.concurrent.Future

//noinspection NotImplementedCode
class MockFileListHistoryService(
  getAllMock: () => Future[Seq[String]] = () => ???,
  saveMock: String => Future[Unit] = _ => ???
) extends FileListHistoryService {

  override def getAll: Future[Seq[String]] = getAllMock()

  override def save(path: String): Future[Unit] = saveMock(path)
}
