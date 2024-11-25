package farjs.app.service

import farjs.app.BaseDBContextSpec
import farjs.app.filelist.FileListModule
import farjs.domain.dao.FolderShortcutDao
import org.scalatest.Succeeded

import scala.concurrent.Future
import scala.scalajs.js

class FolderShortcutsServiceImplSpec extends BaseDBContextSpec {

  it should "store new folder shortcut when save" in withCtx { (db, ctx) =>
    //given
    val module = new FileListModule(db, ctx)
    val dao = module.folderShortcutDao
    val service = module.folderShortcutsService
    val path = "test/path"
    
    for {
      _ <- dao.deleteAll()
      _ <- service.getAll.map { shortcuts =>
        shortcuts shouldBe List.fill(10)(Option.empty[String])
      }

      //when
      _ <- service.save(1, path)

      results <- service.getAll
    } yield {
      //then
      results shouldBe {
        List.fill(10)(Option.empty[String]).updated(1, Some(path))
      }
    }
  }
  
  it should "update existing folder shortcut when save" in withCtx { (db, ctx) =>
    //given
    val module = new FileListModule(db, ctx)
    val dao = module.folderShortcutDao
    val service = module.folderShortcutsService
    val path = "test/path"
    
    for {
      _ <- dao.deleteAll()
      _ <- service.save(1, path)
      _ <- service.getAll.map { shortcuts =>
        shortcuts shouldBe {
          List.fill(10)(Option.empty[String]).updated(1, Some(path))
        }
      }

      //when
      _ <- service.save(1, s"$path-updated")

      results <- service.getAll
    } yield {
      //then
      results shouldBe {
        List.fill(10)(Option.empty[String]).updated(1, Some(s"$path-updated"))
      }
    }
  }
  
  it should "delete folder shortcut when delete" in withCtx { (db, ctx) =>
    //given
    val module = new FileListModule(db, ctx)
    val dao = module.folderShortcutDao
    val service = module.folderShortcutsService
    val path = "test/path"
    
    for {
      _ <- dao.deleteAll()
      _ <- service.save(1, path)
      _ <- service.getAll.map { shortcuts =>
        shortcuts shouldBe {
          List.fill(10)(Option.empty[String]).updated(1, Some(path))
        }
      }

      //when
      _ <- service.delete(1)

      results <- service.getAll
    } yield {
      //then
      results shouldBe List.fill(10)(Option.empty[String])
    }
  }
  
  it should "recover and log error when getAll" in {
    //given
    val errorLogger = mockFunction[String, Unit]
    val dao = mock[FolderShortcutDao]
    val service = new FolderShortcutsServiceImpl(dao)
    val ex = new Exception("test error")

    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    //then
    (() => dao.getAll).expects().returning(Future.failed(ex))
    errorLogger.expects(s"Failed to read folder shortcuts, error: $ex")

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
    val dao = mock[FolderShortcutDao]
    val service = new FolderShortcutsServiceImpl(dao)
    val path = "test/path"
    val ex = new Exception("test error")

    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    //then
    (dao.save _).expects(*).returning(Future.failed(ex))
    errorLogger.expects(s"Failed to save folder shortcut, error: $ex")

    //when
    val resultF = service.save(0, path)

    //then
    resultF.map { _ =>
      js.Dynamic.global.console.error = savedConsoleError
      Succeeded
    }
  }

  it should "recover and log error when delete" in {
    //given
    val errorLogger = mockFunction[String, Unit]
    val dao = mock[FolderShortcutDao]
    val service = new FolderShortcutsServiceImpl(dao)
    val ex = new Exception("test error")
    val index = 0

    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    //then
    (dao.delete _).expects(index).returning(Future.failed(ex))
    errorLogger.expects(s"Failed to delete folder shortcut, error: $ex")

    //when
    val resultF = service.delete(index)

    //then
    resultF.map { _ =>
      js.Dynamic.global.console.error = savedConsoleError
      Succeeded
    }
  }
}
