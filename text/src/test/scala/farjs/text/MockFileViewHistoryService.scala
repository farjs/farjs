package farjs.text

import scala.concurrent.Future

//noinspection NotImplementedCode
class MockFileViewHistoryService(
  getAllMock: () => Future[Seq[FileViewHistory]] = () => ???,
  saveMock: FileViewHistory => Future[Unit] = _ => ???
) extends FileViewHistoryService {

  override def getAll: Future[Seq[FileViewHistory]] = getAllMock()

  override def save(h: FileViewHistory): Future[Unit] = saveMock(h)
}
