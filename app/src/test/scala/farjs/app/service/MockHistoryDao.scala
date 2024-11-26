package farjs.app.service

import farjs.domain.dao.HistoryDao
import farjs.filelist.history.History

import scala.scalajs.js

object MockHistoryDao {

  //noinspection NotImplementedCode
  def apply(
             getAllMock: () => js.Promise[js.Array[History]] = () => ???,
             getByItemMock: String => js.Promise[js.UndefOr[History]] = _ => ???,
             saveMock: (History, Double) => js.Promise[Unit] = (_, _) => ???,
             deleteAllMock: () => js.Promise[Unit] = () => ???
           ): HistoryDao = {
    
    new HistoryDao {

      override def getAll(): js.Promise[js.Array[History]] = getAllMock()

      override def getByItem(item: String): js.Promise[js.UndefOr[History]] = getByItemMock(item)

      override def save(entity: History, updatedAt: Double): js.Promise[Unit] = saveMock(entity, updatedAt)

      override def deleteAll(): js.Promise[Unit] = deleteAllMock()
    }
  }
}
