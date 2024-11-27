package farjs.app.service

import farjs.app.BaseDBContextSpec
import farjs.app.service.HistoryServiceImplSpec.assertHistory
import farjs.domain.HistoryKindEntity
import farjs.domain.dao.{HistoryDao, HistoryKindDao}
import farjs.file.FileViewHistoryParams
import farjs.file.FileViewHistorySpec.assertFileViewHistoryParams
import farjs.filelist.history.History
import org.scalactic.source.Position
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.{Assertion, OptionValues, Succeeded}

import scala.scalajs.js

class HistoryServiceImplSpec extends BaseDBContextSpec with OptionValues {

  //noinspection TypeAnnotation
  class HistoryMocks {
    val getAll = mockFunction[js.Promise[js.Array[History]]]
    val getByItem = mockFunction[String, js.Promise[js.UndefOr[History]]]
    val save = mockFunction[History, Double, js.Promise[Unit]]

    val dao = MockHistoryDao(
      getAllMock = getAll,
      getByItemMock = getByItem,
      saveMock = save
    )
  }

  private val params = FileViewHistoryParams(
    isEdit = false,
    encoding = "test-encoding",
    position = 123,
    wrap = true,
    column = 4
  )

  it should "store and read items" in withCtx { db =>
    //given
    val kindDao = HistoryKindDao(db)
    val maxItemsCount = 10
    val dao0 = HistoryDao(db, HistoryKindEntity(-1, "non-existing"), maxItemsCount)

    for {
      _ <- dao0.deleteAll().toFuture
      _ <- kindDao.deleteAll().toFuture
      kind <- kindDao.upsert(HistoryKindEntity(-1, "test_kind1")).toFuture
      service = new HistoryServiceImpl(HistoryDao(db, kind, maxItemsCount))
      
      //when
      entity1 = History("test/path/1", js.undefined)
      entity2 = History("test/path/2", params)
      _ <- service.save(entity1).toFuture
      _ <- service.save(entity2).toFuture
      
      //then
      results <- service.getAll.toFuture
      result1 <- service.getOne(entity1.item).toFuture
      result2 <- service.getOne(entity2.item).toFuture
    } yield {
      assertHistory(result1.toOption.value, entity1, None)
      assertHistory(result2.toOption.value, entity2, Some(params))

      val expected = List((entity1, None), (entity2, Some(params)))
      results.size shouldBe expected.size
      results.zip(expected).foreach { case (res, (entity, params)) =>
        assertHistory(res, entity, params)
      }
      Succeeded
    }
  }
  
  it should "recover and log error when getAll" in {
    //given
    val errorLogger = mockFunction[String, Unit]
    val mocks = new HistoryMocks
    val service = new HistoryServiceImpl(mocks.dao)
    val error = js.Error("test error")

    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    //then
    mocks.getAll.expects().returning(js.Promise.reject(error))
    errorLogger.expects(s"Failed to read all history items, error: scala.scalajs.js.JavaScriptException: Error: test error")

    //when
    val resultF = service.getAll.toFuture

    //then
    resultF.map { results =>
      js.Dynamic.global.console.error = savedConsoleError
      results.toList shouldBe Nil
    }
  }

  it should "recover and log error when getOne" in {
    //given
    val errorLogger = mockFunction[String, Unit]
    val mocks = new HistoryMocks
    val service = new HistoryServiceImpl(mocks.dao)
    val error = js.Error("test error")
    val path = "test/path"

    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    //then
    mocks.getByItem.expects(path).returning(js.Promise.reject(error))
    errorLogger.expects(s"Failed to read history item, error: scala.scalajs.js.JavaScriptException: Error: test error")

    //when
    val resultF = service.getOne(path).toFuture

    //then
    resultF.map { result =>
      js.Dynamic.global.console.error = savedConsoleError
      result.toOption shouldBe None
    }
  }

  it should "recover and log error when save" in {
    //given
    val errorLogger = mockFunction[String, Unit]
    val mocks = new HistoryMocks
    val service = new HistoryServiceImpl(mocks.dao)
    val path = "test/path"
    val error = js.Error("test error")

    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    //then
    mocks.save.expects(*, *).returning(js.Promise.reject(error))
    errorLogger.expects(s"Failed to save history item, error: scala.scalajs.js.JavaScriptException: Error: test error")

    //when
    val resultF = service.save(History(path, js.undefined)).toFuture

    //then
    resultF.map { _ =>
      js.Dynamic.global.console.error = savedConsoleError
      Succeeded
    }
  }
}

object HistoryServiceImplSpec {
  
  def assertHistory(result: History,
                    expected: History,
                    params: Option[FileViewHistoryParams]
                   )(implicit position: Position): Assertion = {

    inside(result) {
      case History(item, resParams) =>
        item shouldBe expected.item
        resParams.toOption.size shouldBe params.size
        resParams.toOption.zip(params).foreach { case (res, expected) =>
          assertFileViewHistoryParams(res.asInstanceOf[FileViewHistoryParams], expected)
        }
        Succeeded
    }
  }
}
