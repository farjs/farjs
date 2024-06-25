package farjs.app.filelist.service

import farjs.app.BaseDBContextSpec
import farjs.app.filelist.FileListModule
import farjs.domain.dao.FileViewHistoryDao
import farjs.file.FileViewHistorySpec.assertFileViewHistory
import farjs.file.{FileViewHistory, FileViewHistoryParams}
import org.scalatest.Succeeded

import scala.concurrent.Future
import scala.scalajs.js

class FileViewHistoryServiceImplSpec extends BaseDBContextSpec {

  it should "store new entity when save" in withCtx { ctx =>
    //given
    val module = new FileListModule(ctx)
    val dao = module.fileViewHistoryDao
    val service = module.fileViewHistoryService
    val history = FileViewHistory(
      path = "test/path",
      params = FileViewHistoryParams(
        isEdit = false,
        encoding = "test-encoding",
        position = 123,
        wrap = true,
        column = 4
      )
    )
    
    for {
      _ <- dao.deleteAll()
      _ <- service.getAll.map { entities =>
        entities should be (empty)
      }
      _ <- service.getOne(history.path, history.params.isEdit).map { maybeHistory =>
        maybeHistory shouldBe None
      }

      //when
      _ <- service.save(history)

      //then
      allRes <- service.getAll
      oneRes <- service.getOne(history.path, history.params.isEdit)
    } yield {
      allRes.size shouldBe 1
      allRes.zip(List(history)).foreach { case (result, expected) =>
        assertFileViewHistory(result, expected)
      }
      oneRes.size shouldBe 1
      oneRes.zip(Some(history)).foreach { case (result, expected) =>
        assertFileViewHistory(result, expected)
      }
      Succeeded
    }
  }

  it should "update existing entity when save" in withCtx { ctx =>
    //given
    val module = new FileListModule(ctx)
    val dao = module.fileViewHistoryDao
    val service = module.fileViewHistoryService
    val history1 = FileViewHistory(
      path = "test/path",
      params = FileViewHistoryParams(
        isEdit = false,
        encoding = "test-encoding1",
        position = 1,
        wrap = true,
        column = 3
      )
    )
    val history2 = FileViewHistory(
      path = "test/path",
      params = FileViewHistoryParams(
        isEdit = true,
        encoding = "test-encoding2",
        position = 2,
        wrap = js.undefined,
        column = js.undefined
      )
    )
    val updated = history1.copy(
      params = FileViewHistoryParams.copy(history1.params)(
        encoding = "updated-encoding",
        position = 456,
        wrap = false,
        column = 5
      )
    )
    
    for {
      _ <- dao.deleteAll()
      _ <- service.save(history1)
      _ <- service.save(history2)
      _ <- service.getOne(history1.path, history1.params.isEdit).map { maybeRes =>
        inside(maybeRes) {
          case Some(res) => assertFileViewHistory(res, history1)
        }
      }
      _ <- service.getOne(history2.path, history2.params.isEdit).map { maybeRes =>
        inside(maybeRes) {
          case Some(res) => assertFileViewHistory(res, history2)
        }
      }
      _ <- service.getAll.map { allRes =>
        val allResSorted = allRes.sortBy(e => (e.path, e.params.isEdit))
        val expected = List(history1, history2)
        allResSorted.size shouldBe expected.size
        allResSorted.zip(expected).foreach { case (result, expected) =>
          assertFileViewHistory(result, expected)
        }
      }

      //when
      _ <- service.save(updated)

      //then
      allRes <- service.getAll
      oneRes <- service.getOne(history1.path, history1.params.isEdit)
    } yield {
      val resultsSorted = allRes.sortBy(e => (e.path, e.params.isEdit))
      val allExpected = List(updated, history2)
      resultsSorted.size shouldBe allExpected.size
      resultsSorted.zip(allExpected).foreach { case (result, expected) =>
        assertFileViewHistory(result, expected)
      }
      oneRes.size shouldBe 1
      oneRes.zip(Some(updated)).foreach { case (result, expected) =>
        assertFileViewHistory(result, expected)
      }
      Succeeded
    }
  }

  it should "keep last N records when save" in withCtx { ctx =>
    //given
    val dao = new FileViewHistoryDao(ctx, maxItemsCount = 3)
    val beforeF = dao.getAll

    //when
    val resultF = beforeF.flatMap { existing =>
      val entity = existing.head
      Future.sequence((1 to 5).toList.map { i =>
        dao.save(entity.copy(path = s"${entity.path}$i", updatedAt = entity.updatedAt + (i * 10)))
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

  it should "recover and log error when getAll" in {
    //given
    val errorLogger = mockFunction[String, Unit]
    val dao = mock[FileViewHistoryDao]
    val service = new FileViewHistoryServiceImpl(dao)
    val ex = new Exception("test error")

    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    //then
    (() => dao.getAll).expects().returning(Future.failed(ex))
    errorLogger.expects(s"Failed to read all file view history, error: $ex")

    //when
    val resultF = service.getAll

    //then
    resultF.map { results =>
      js.Dynamic.global.console.error = savedConsoleError
      results shouldBe Nil
    }
  }

  it should "recover and log error when getOne" in {
    //given
    val errorLogger = mockFunction[String, Unit]
    val dao = mock[FileViewHistoryDao]
    val service = new FileViewHistoryServiceImpl(dao)
    val ex = new Exception("test error")
    val path = "test/path"
    val isEdit = true

    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    //then
    (dao.getById _).expects(path, isEdit).returning(Future.failed(ex))
    errorLogger.expects(s"Failed to read one file view history, error: $ex")

    //when
    val resultF = service.getOne(path, isEdit)

    //then
    resultF.map { results =>
      js.Dynamic.global.console.error = savedConsoleError
      results shouldBe None
    }
  }

  it should "recover and log error when save" in {
    //given
    val errorLogger = mockFunction[String, Unit]
    val dao = mock[FileViewHistoryDao]
    val service = new FileViewHistoryServiceImpl(dao)
    val history = FileViewHistory(
      path = "test/path",
      params = FileViewHistoryParams(
        isEdit = false,
        encoding = "test-encoding",
        position = 123,
        wrap = true,
        column = 4
      )
    )
    val ex = new Exception("test error")

    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    //then
    (dao.save _).expects(*).returning(Future.failed(ex))
    errorLogger.expects(s"Failed to save file view history, error: $ex")

    //when
    val resultF = service.save(history)

    //then
    resultF.map { _ =>
      js.Dynamic.global.console.error = savedConsoleError
      Succeeded
    }
  }
}
