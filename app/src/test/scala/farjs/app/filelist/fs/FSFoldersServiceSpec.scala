package farjs.app.filelist.fs

import farjs.app.BaseDBContextSpec
import farjs.domain.HistoryFolder
import farjs.domain.dao.HistoryFolderDao
import org.scalatest.Succeeded

import scala.concurrent.Future
import scala.scalajs.js

class FSFoldersServiceSpec extends BaseDBContextSpec {

  it should "create new record when save" in withCtx { ctx =>
    //given
    val dao = new HistoryFolderDao(ctx)
    val service = new FSFoldersService(dao)
    val path = "test/path"
    
    val beforeF = dao.deleteAll()
    
    //when
    val resultF = beforeF.flatMap { _ =>
      service.save(path)
    }

    //then
    for {
      _ <- resultF
      results <- service.getAll
    } yield {
      results shouldBe List(path)
    }
  }
  
  it should "update existing record when save" in withCtx { ctx =>
    //given
    val dao = new HistoryFolderDao(ctx)
    val service = new FSFoldersService(dao)
    val path = "test/path"
    
    val beforeF = dao.getAll
    
    //when
    val resultF = beforeF.flatMap { existing =>
      existing.map(_.path) shouldBe List(path)
      val entity = existing.head
      dao.save(HistoryFolder(path, entity.updatedAt + 1), keepLast = 5).map { _ =>
        entity
      }
    }

    //then
    for {
      existing <- resultF
      entities <- dao.getAll
      results <- service.getAll
    } yield {
      results shouldBe List(path)
      existing.updatedAt should be < entities.head.updatedAt
    }
  }

  it should "keep last N records when save" in withCtx { ctx =>
    //given
    val dao = new HistoryFolderDao(ctx)
    val service = new FSFoldersService(dao)
    val path = "test/path"
    val keepLast = 3
    
    val beforeF = dao.getAll
    
    //when
    val resultF = beforeF.flatMap { existing =>
      existing.map(_.path) shouldBe List(path)
      val entity = existing.head
      Future.sequence((1 to 5).toList.map { i =>
        dao.save(HistoryFolder(s"$path$i", entity.updatedAt + i), keepLast)
      })
    }

    //then
    for {
      _ <- resultF
      results <- service.getAll
    } yield {
      results shouldBe List(
        "test/path3",
        "test/path4",
        "test/path5"
      )
    }
  }

  it should "recover and log error when getAll" in {
    //given
    val errorLogger = mockFunction[String, Unit]
    val dao = mock[HistoryFolderDao]
    val service = new FSFoldersService(dao)
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
    val dao = mock[HistoryFolderDao]
    val service = new FSFoldersService(dao)
    val path = "test/path"
    val ex = new Exception("test error")

    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    //then
    (dao.save _).expects(*, *).returning(Future.failed(ex))
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
