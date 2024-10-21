package farjs.app.filelist.service

import farjs.app.BaseDBContextSpec
import farjs.domain.dao.BaseHistoryDao
import farjs.domain.{FarjsDBContext, BaseHistory}

import scala.concurrent.Future

abstract class BaseHistoryDaoSpec extends BaseDBContextSpec {

  protected def createDao(ctx: FarjsDBContext, maxItemsCount: Int = 10): BaseHistoryDao

  private val testItem = "test/item"
  
  it should "create new record when save" in withCtx { ctx =>
    //given
    val dao = createDao(ctx)
    val entity = BaseHistory(testItem, System.currentTimeMillis())
    val beforeF = dao.deleteAll()
    
    //when
    val resultF = beforeF.flatMap { _ =>
      dao.save(entity)
    }

    //then
    for {
      _ <- resultF
      results <- dao.getAll
    } yield {
      results shouldBe List(entity)
    }
  }
  
  it should "update existing record when save" in withCtx { ctx =>
    //given
    val dao = createDao(ctx)
    val beforeF = dao.getAll
    
    //when
    val resultF = beforeF.flatMap { existing =>
      existing.map(_.item) shouldBe List(testItem)
      val entity = existing.head
      val updated = BaseHistory(testItem, entity.updatedAt + 1)
      dao.save(updated).map { _ =>
        updated
      }
    }

    //then
    for {
      updated <- resultF
      results <- dao.getAll
    } yield {
      results shouldBe List(updated)
    }
  }

  it should "keep last N records when save" in withCtx { ctx =>
    //given
    val dao = createDao(ctx, maxItemsCount = 3)
    val beforeF = dao.getAll
    
    //when
    val resultF = beforeF.flatMap { existing =>
      existing.map(_.item) shouldBe List(testItem)

      val entity = existing.head
      Future.sequence((1 to 5).toList.map { i =>
        dao.save(BaseHistory(s"$testItem$i", entity.updatedAt + i))
      })
    }

    //then
    for {
      _ <- resultF
      results <- dao.getAll
    } yield {
      results.map(_.item) shouldBe List(
        "test/item3",
        "test/item4",
        "test/item5"
      )
    }
  }
}
