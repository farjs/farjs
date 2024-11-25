package farjs.app.service

import farjs.app.BaseDBContextSpec
import farjs.app.service.HistoryProviderImpl.limitMaxItemsCount
import farjs.app.service.HistoryServiceImplSpec.assertHistory
import farjs.domain.HistoryKindEntity
import farjs.domain.dao.{HistoryDao, HistoryKindDao}
import farjs.file.FileViewHistoryParams
import farjs.filelist.history.{History, HistoryKind}
import org.scalatest.OptionValues

import scala.scalajs.js

class HistoryProviderImplSpec extends BaseDBContextSpec with OptionValues {

  private val maxItemsCount = 10

  //noinspection TypeAnnotation
  class HistoryKindMocks {
    val upsert = mockFunction[HistoryKindEntity, js.Promise[HistoryKindEntity]]

    val kindDao = MockHistoryKindDao(
      upsertMock = upsert
    )
  }

  it should "save and read history" in withCtx { (db, ctx) =>
    //given
    val kindDao = HistoryKindDao(db)
    val provider = new HistoryProviderImpl(kindDao, ctx)
    val dao0 = new HistoryDao(ctx, HistoryKindEntity(-1, "non-existing"), maxItemsCount)
    val entity = History("test/path", js.undefined)

    for {
      _ <- dao0.deleteAll()
      _ <- kindDao.deleteAll().toFuture

      //when
      service1 <- provider.get(HistoryKind("test_kind", maxItemsCount)).toFuture
      service2 <- provider.get(HistoryKind("test_kind", maxItemsCount)).toFuture
      _ <- service2.save(entity).toFuture

      //then
      results <- service2.getAll.toFuture
      result <- service2.getOne(entity.item).toFuture
    } yield {
      service1 shouldBe service2
      val expected = List((entity, Option.empty[FileViewHistoryParams]))
      results.size shouldBe expected.size
      results.zip(expected).foreach { case (res, (entity, params)) =>
        assertHistory(res, entity, params)
      }
      assertHistory(result.toOption.value, entity, None)
    }
  }

  it should "recover and log error when get" in withCtx { (_, ctx) =>
    //given
    val errorLogger = mockFunction[String, Unit]
    val kindMocks = new HistoryKindMocks
    val provider = new HistoryProviderImpl(kindMocks.kindDao, ctx)
    val kind = HistoryKind("test_kind", maxItemsCount)
    val entity = History("test/path", js.undefined)
    val error = js.Error("test error")

    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    //then
    kindMocks.upsert.expects(*).returning(js.Promise.reject(error))
    errorLogger.expects(s"Failed to upsert history kind '${kind.name}', error: scala.scalajs.js.JavaScriptException: Error: test error")

    for {
      //when
      service <- provider.get(kind).toFuture
      _ <- service.save(entity).toFuture

      //then
      results <- service.getAll.toFuture
      result <- service.getOne(entity.item).toFuture
    } yield {
      js.Dynamic.global.console.error = savedConsoleError
      results.toList shouldBe Nil
      result.toOption shouldBe None
    }
  }

  it should "limit maxItemsCount from 5 to 150 when limitMaxItemsCount" in {
    //when & then
    limitMaxItemsCount(-1) shouldBe 5
    limitMaxItemsCount(0) shouldBe 5
    limitMaxItemsCount(1) shouldBe 5
    limitMaxItemsCount(5) shouldBe 5
    limitMaxItemsCount(6) shouldBe 6
    limitMaxItemsCount(150) shouldBe 150
    limitMaxItemsCount(151) shouldBe 150
    limitMaxItemsCount(152) shouldBe 150
  }
}
