package farjs.text

import scala.concurrent.Future

//noinspection NotImplementedCode
class MockFileViewHistoryService(
  getAllMock: () => Future[Seq[FileViewHistory]] = () => ???,
  getOneMock: (String, Boolean) => Future[Option[FileViewHistory]] = (_, _) => ???,
  saveMock: FileViewHistory => Future[Unit] = _ => ???
) extends FileViewHistoryService {

  def getAll: Future[Seq[FileViewHistory]] = getAllMock()

  def getOne(path: String, isEdit: Boolean): Future[Option[FileViewHistory]] = getOneMock(path, isEdit)

  def save(h: FileViewHistory): Future[Unit] = saveMock(h)
}
