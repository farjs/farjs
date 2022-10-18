package farjs.app.filelist.history

import farjs.app.BaseDBContextSpec
import farjs.domain.dao.HistoryDao
import farjs.domain.{FarjsDBContext, HistoryEntity}

import scala.concurrent.Future

abstract class BaseHistoryDaoSpec extends BaseDBContextSpec {

  protected def createDao(ctx: FarjsDBContext, maxItemsCount: Int = 10): HistoryDao

  private val testItemPath = "test/path"
  
  it should "create new record when save" in withCtx { ctx =>
    //given
    val dao = createDao(ctx)
    val entity = HistoryEntity(testItemPath, System.currentTimeMillis())
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
      existing.map(_.path) shouldBe List(testItemPath)
      val entity = existing.head
      val updated = HistoryEntity(testItemPath, entity.updatedAt + 1)
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
      existing.map(_.path) shouldBe List(testItemPath)

      val entity = existing.head
      Future.sequence((1 to 5).toList.map { i =>
        dao.save(HistoryEntity(s"$testItemPath$i", entity.updatedAt + i))
      })
    }

    //then
    for {
      _ <- resultF
      results <- dao.getAll
    } yield {
      results.map(_.path) shouldBe List(
        "test/path3",
        "test/path4",
        "test/path5"
      )
    }
  }
}
