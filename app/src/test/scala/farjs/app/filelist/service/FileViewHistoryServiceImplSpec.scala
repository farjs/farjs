package farjs.app.filelist.service

import farjs.app.BaseDBContextSpec
import farjs.app.filelist.FileListModule
import farjs.domain.dao.FileViewHistoryDao
import farjs.file.FileViewHistory
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
      isEdit = false,
      encoding = "test-encoding",
      position = 123,
      wrap = Some(true),
      column = Some(4)
    )
    
    for {
      _ <- dao.deleteAll()
      _ <- service.getAll.map { entities =>
        entities should be (empty)
      }
      _ <- service.getOne(history.path, history.isEdit).map { maybeHistory =>
        maybeHistory shouldBe None
      }

      //when
      _ <- service.save(history)

      //then
      allRes <- service.getAll
      oneRes <- service.getOne(history.path, history.isEdit)
    } yield {
      allRes shouldBe List(history)
      oneRes shouldBe Some(history)
    }
  }

  it should "update existing entity when save" in withCtx { ctx =>
    //given
    val module = new FileListModule(ctx)
    val dao = module.fileViewHistoryDao
    val service = module.fileViewHistoryService
    val history1 = FileViewHistory(
      path = "test/path",
      isEdit = false,
      encoding = "test-encoding1",
      position = 1,
      wrap = Some(true),
      column = Some(3)
    )
    val history2 = FileViewHistory(
      path = "test/path",
      isEdit = true,
      encoding = "test-encoding2",
      position = 2,
      wrap = None,
      column = None
    )
    val updated = history1.copy(
      encoding = "updated-encoding",
      position = 456,
      wrap = Some(false),
      column = Some(5)
    )
    
    for {
      _ <- dao.deleteAll()
      _ <- service.save(history1)
      _ <- service.save(history2)
      _ <- service.getOne(history1.path, history1.isEdit).map { entity =>
        entity shouldBe Some(history1)
      }
      _ <- service.getOne(history2.path, history2.isEdit).map { entity =>
        entity shouldBe Some(history2)
      }
      _ <- service.getAll.map { entities =>
        entities.sortBy(e => (e.path, e.isEdit)) shouldBe List(history1, history2)
      }

      //when
      _ <- service.save(updated)

      //then
      allRes <- service.getAll
      oneRes <- service.getOne(history1.path, history1.isEdit)
    } yield {
      allRes.sortBy(e => (e.path, e.isEdit)) shouldBe List(updated, history2)
      oneRes shouldBe Some(updated)
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
      isEdit = false,
      encoding = "test-encoding",
      position = 123,
      wrap = Some(true),
      column = Some(4)
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
