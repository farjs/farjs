package farjs.app.service

import farjs.domain.HistoryKindEntity
import farjs.domain.dao.HistoryKindDao

import scala.scalajs.js

object MockHistoryKindDao {

  //noinspection NotImplementedCode
  def apply(
             getAllMock: () => js.Promise[js.Array[HistoryKindEntity]] = () => ???,
             upsertMock: HistoryKindEntity => js.Promise[HistoryKindEntity] = _ => ???,
             deleteAllMock: () => js.Promise[Unit] = () => ???
           ): HistoryKindDao = {
    
    new HistoryKindDao {

      override def getAll(): js.Promise[js.Array[HistoryKindEntity]] = getAllMock()

      override def upsert(entity: HistoryKindEntity): js.Promise[HistoryKindEntity] = upsertMock(entity)

      override def deleteAll(): js.Promise[Unit] = deleteAllMock()
    }
  }
}
