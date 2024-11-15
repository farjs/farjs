package farjs.app.service

import farjs.app.BaseDBContextSpec
import farjs.domain.HistoryKindEntity
import farjs.domain.dao.{HistoryDao, HistoryKindDao}
import farjs.file.FileViewHistoryParams
import farjs.file.FileViewHistorySpec.assertFileViewHistoryParams
import farjs.filelist.history.History
import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}

import scala.concurrent.Future
import scala.scalajs.js

class HistoryDaoSpec extends BaseDBContextSpec {

  private val testItem = "test/item"
  private val params = FileViewHistoryParams(
    isEdit = false,
    encoding = "test-encoding",
    position = 123,
    wrap = true,
    column = 4
  )
  
  it should "create new records when save" in withCtx { ctx =>
    //given
    val kindDao = new HistoryKindDao(ctx)
    val maxItemsCount = 10
    val dao0 = new HistoryDao(ctx, HistoryKindEntity(-1, "non-existing"), maxItemsCount)

    for {
      _ <- dao0.deleteAll()
      _ <- kindDao.deleteAll()
      _ <- dao0.getByItem("test").map { maybeHistory =>
        maybeHistory shouldBe None
      }

      //when & then
      kind1 <- kindDao.upsert(HistoryKindEntity(-1, "test_kind1"))
      dao1 = new HistoryDao(ctx, kind1, maxItemsCount)
      entity1 = History(testItem, params)
      _ <- dao1.save(entity1, js.Date.now())
      _ <- dao1.getByItem(entity1.item).map(inside(_) { case Some(res) =>
        assertHistoryEntity(res, entity1, Some(params))
      })

      //when & then
      kind2 <- kindDao.upsert(HistoryKindEntity(-1, "test_kind2"))
      dao2 = new HistoryDao(ctx, kind2, maxItemsCount)
      entity2 = History(testItem, js.undefined)
      _ <- dao2.save(entity2, js.Date.now())
      _ <- dao2.getByItem(entity2.item).map(inside(_) { case Some(res) =>
        assertHistoryEntity(res, entity2, None)
      })
      
      //then
      results1 <- dao1.getAll
      results2 <- dao2.getAll
    } yield {
      assertResults(results1, List((entity1, Some(params))))
      assertResults(results2, List((entity2, None)))
    }
  }
  
  it should "update existing record when save" in withCtx { ctx =>
    //given
    val kindDao = new HistoryKindDao(ctx)
    val maxItemsCount = 10
    val updatedParams = FileViewHistoryParams.copy(params)(
      encoding = "updated-encoding",
      position = 456,
      wrap = false,
      column = 5
    )

    for {
      allKinds <- kindDao.getAll
      (kind1, kind2) = inside(allKinds.toList) {
        case List(kind1, kind2) => (kind1, kind2)
      }
      dao1 = new HistoryDao(ctx, kind1, maxItemsCount)
      dao2 = new HistoryDao(ctx, kind2, maxItemsCount)
      all2 <- dao2.getAll
      entity2 = inside(all2.toList) {
        case List(entity2) => entity2
      }
      
      //when
      updated = History(testItem, updatedParams)
      _ <- dao1.save(updated, js.Date.now())

      //then
      results1 <- dao1.getAll
      results2 <- dao2.getAll
    } yield {
      assertResults(results1, List((updated, Some(updatedParams))))
      assertResults(results2, List((entity2, None)))
    }
  }

  it should "keep last N records when save" in withCtx { ctx =>
    //given
    val kindDao = new HistoryKindDao(ctx)
    val maxItemsCount = 3

    for {
      allKinds <- kindDao.getAll
      (kind1, kind2) = inside(allKinds.toList) {
        case List(kind1, kind2) => (kind1, kind2)
      }
      dao1 = new HistoryDao(ctx, kind1, maxItemsCount)
      dao2 = new HistoryDao(ctx, kind2, maxItemsCount)
      updatedAt = js.Date.now()

      //when
      _ <- Future.sequence((1 to 5).toList.map { i =>
        dao1.save(History(s"$testItem$i", js.undefined), updatedAt + i)
      })
      
      //then
      results1 <- dao1.getAll
      results2 <- dao2.getAll
    } yield {
      results1.map(_.item) shouldBe List(
        "test/item3",
        "test/item4",
        "test/item5"
      )
      results2.map(_.item) shouldBe List(
        "test/item"
      )
    }
  }
  
  private def assertResults(results: Seq[History],
                            expected: List[(History, Option[FileViewHistoryParams])]
                           )(implicit position: Position): Assertion = {

    results.size shouldBe expected.size
    results.zip(expected).foreach { case (res, (entity, params)) =>
      assertHistoryEntity(res, entity, params)
    }
    Succeeded
  }

  private def assertHistoryEntity(result: History,
                                  entity: History,
                                  params: Option[FileViewHistoryParams]
                                 )(implicit position: Position): Assertion = {
    inside(result) {
      case History(item, resParams) =>
        item shouldBe entity.item
        resParams.toOption.size shouldBe params.size
        resParams.toOption.zip(params).foreach { case (res, expected) =>
          assertFileViewHistoryParams(res.asInstanceOf[FileViewHistoryParams], expected)
        }
        Succeeded
    }
  }
}
