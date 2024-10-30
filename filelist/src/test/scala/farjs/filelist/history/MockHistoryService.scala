package farjs.filelist.history

import scala.scalajs.js

//noinspection NotImplementedCode
class MockHistoryService(
  getAllMock: () => js.Promise[js.Array[History]] = () => ???,
  getOneMock: String => js.Promise[js.UndefOr[History]] = _ => ???,
  saveMock: History => js.Promise[Unit] = _ => ???,
) extends HistoryService {

  override def getAll: js.Promise[js.Array[History]] = getAllMock()

  override def getOne(item: String): js.Promise[js.UndefOr[History]] = getOneMock(item)

  override def save(h: History): js.Promise[Unit] = saveMock(h)
}
