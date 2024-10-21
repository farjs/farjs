package farjs.app.filelist.service

import farjs.app.BaseDBContextSpec
import farjs.domain.dao.{HistoryDao, HistoryKindDao}
import farjs.domain.{HistoryEntity, HistoryKind}
import farjs.file.FileViewHistoryParams
import farjs.file.FileViewHistorySpec.assertFileViewHistoryParams
import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}

import scala.concurrent.Future

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
    val dao = new HistoryDao(ctx, maxItemsCount = 10)

    for {
      _ <- dao.deleteAll()
      _ <- kindDao.deleteAll()
      _ <- dao.getByItem(-1, "test").map { maybeHistory =>
        maybeHistory shouldBe None
      }

      //when & then
      kind1 <- kindDao.upsert(HistoryKind(-1, "test_kind1"))
      entity1 = HistoryEntity(kind1.id, testItem, Some(params), System.currentTimeMillis())
      _ <- dao.save(entity1)
      _ <- dao.getByItem(entity1.kindId, entity1.item).map(inside(_) { case Some(res) =>
        assertHistoryEntity(res, entity1, Some(params))
      })

      //when & then
      kind2 <- kindDao.upsert(HistoryKind(-1, "test_kind2"))
      entity2 = HistoryEntity(kind2.id, testItem, None, System.currentTimeMillis())
      _ <- dao.save(entity2)
      _ <- dao.getByItem(entity2.kindId, entity2.item).map(inside(_) { case Some(res) =>
        assertHistoryEntity(res, entity2, None)
      })
      
      //then
      results <- dao.getAll
    } yield {
      val expected = List((entity1, Some(params)), (entity2, None))
      results.size shouldBe expected.size
      results.zip(expected).foreach { case (res, (entity, params)) =>
        assertHistoryEntity(res, entity, params)
      }
      Succeeded
    }
  }
  
  it should "update existing record when save" in withCtx { ctx =>
    //given
    val dao = new HistoryDao(ctx, maxItemsCount = 10)
    val updatedParams = FileViewHistoryParams.copy(params)(
      encoding = "updated-encoding",
      position = 456,
      wrap = false,
      column = 5
    )

    for {
      all <- dao.getAll
      (entity1, entity2) = inside(all.toList) {
        case List(entity1, entity2) => (entity1, entity2)
      }
      
      //when
      updated = HistoryEntity(entity1.kindId, testItem, Some(updatedParams), entity1.updatedAt + 1)
      _ <- dao.save(updated)

      //then
      results <- dao.getAll
    } yield {
      val expected = List((updated, Some(updatedParams)), (entity2, None))
      results.size shouldBe expected.size
      results.zip(expected).foreach { case (res, (entity, params)) =>
        assertHistoryEntity(res, entity, params)
      }
      Succeeded
    }
  }

  it should "keep last N records when save" in withCtx { ctx =>
    //given
    val dao = new HistoryDao(ctx, maxItemsCount = 3)

    for {
      all <- dao.getAll

      //when
      entity = all.head
      _ <- Future.sequence((1 to 5).toList.map { i =>
        dao.save(HistoryEntity(entity.kindId, s"$testItem$i", None, entity.updatedAt + i))
      })
      
      //then
      results <- dao.getAll
    } yield {
      results.map(_.item) shouldBe List(
        //kind1
        "test/item3",
        "test/item4",
        "test/item5",
        //kind2
        "test/item"
      )
    }
  }

  private def assertHistoryEntity(result: HistoryEntity,
                                  entity: HistoryEntity,
                                  params: Option[FileViewHistoryParams])(implicit position: Position): Assertion = {
    inside(result) {
      case HistoryEntity(kindId, item, resParams, _) =>
        kindId shouldBe entity.kindId
        item shouldBe entity.item
        resParams.size shouldBe params.size
        resParams.zip(params).foreach { case (res, expected) =>
          assertFileViewHistoryParams(res.asInstanceOf[FileViewHistoryParams], expected)
        }
        Succeeded
    }
  }
}
