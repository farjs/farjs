package farjs.app.filelist.service

import farjs.app.BaseDBContextSpec
import farjs.domain.HistoryKindEntity
import farjs.domain.dao.{HistoryDao, HistoryKindDao}

class HistoryKindDaoSpec extends BaseDBContextSpec {

  it should "create new record when upsert" in withCtx { ctx =>
    //given
    val historyDao = new HistoryDao(ctx, HistoryKindEntity(-1, "non-existing"), maxItemsCount = 10)
    val dao = new HistoryKindDao(ctx)
    val entity = HistoryKindEntity(-1, "test_history")
    
    for {
      _ <- historyDao.deleteAll()
      _ <- dao.deleteAll()

      //when
      res <- dao.upsert(entity)

      //then
      results <- dao.getAll
    } yield {
      inside(res) {
        case HistoryKindEntity(id, name) =>
          id should be > 0
          name shouldBe entity.name
      }
      results shouldBe List(res)
    }
  }

  it should "return existing record when upsert" in withCtx { ctx =>
    //given
    val dao = new HistoryKindDao(ctx)
    
    for {
      existing <- dao.getAll.map(_.head)
      entity = existing.copy(id = -1)

      //when
      res <- dao.upsert(entity)

      //then
      results <- dao.getAll
    } yield {
      inside(res) {
        case HistoryKindEntity(id, name) =>
          id shouldBe existing.id
          name shouldBe entity.name
      }
      results shouldBe List(res)
    }
  }
}
