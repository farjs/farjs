package farjs.app.filelist.service

import farjs.app.BaseDBContextSpec
import farjs.app.filelist.FileListModule
import farjs.domain.dao.HistoryDao
import org.scalatest.Succeeded

import scala.concurrent.Future
import scala.scalajs.js

class FileListHistoryServiceImplSpec extends BaseDBContextSpec {

  it should "store and read items" in withCtx { ctx =>
    //given
    val module = new FileListModule(ctx)
    val dao = module.folderDao
    val service = module.folderService
    val path = "test/path"
    val timeBeforeSave = System.currentTimeMillis()
    
    val beforeF = dao.deleteAll()
    
    //when
    val resultF = beforeF.flatMap { _ =>
      service.save(path)
    }

    //then
    for {
      _ <- resultF
      entities <- dao.getAll
      results <- service.getAll
    } yield {
      results shouldBe List(path)
      entities.head.updatedAt should be >= timeBeforeSave
    }
  }
  
  it should "recover and log error when getAll" in {
    //given
    val errorLogger = mockFunction[String, Unit]
    val dao = mock[HistoryDao]
    val service = new FileListHistoryServiceImpl(dao)
    val ex = new Exception("test error")

    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    //then
    (() => dao.getAll).expects().returning(Future.failed(ex))
    errorLogger.expects(s"Failed to read history items, error: $ex")

    //when
    val resultF = service.getAll

    //then
    resultF.map { results =>
      js.Dynamic.global.console.error = savedConsoleError
      results shouldBe Nil
    }
  }

  it should "recover and log error when save" in {
    //given
    val errorLogger = mockFunction[String, Unit]
    val dao = mock[HistoryDao]
    val service = new FileListHistoryServiceImpl(dao)
    val path = "test/path"
    val ex = new Exception("test error")

    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    //then
    (dao.save _).expects(*).returning(Future.failed(ex))
    errorLogger.expects(s"Failed to save history item, error: $ex")

    //when
    val resultF = service.save(path)

    //then
    resultF.map { _ =>
      js.Dynamic.global.console.error = savedConsoleError
      Succeeded
    }
  }
}
