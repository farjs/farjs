package farjs.app.service

import farjs.app.BaseDBContextSpec
import farjs.app.filelist.FileListModule
import farjs.domain.FolderShortcut
import org.scalatest.Succeeded

import scala.scalajs.js

class FolderShortcutsServiceImplSpec extends BaseDBContextSpec {

  //noinspection TypeAnnotation
  class FolderShortcutMocks {
    val getAll = mockFunction[js.Promise[js.Array[FolderShortcut]]]
    val save = mockFunction[FolderShortcut, js.Promise[Unit]]
    val delete = mockFunction[Int, js.Promise[Unit]]

    val dao = MockFolderShortcutDao(
      getAllMock = getAll,
      saveMock = save,
      deleteMock = delete
    )
  }

  it should "store new folder shortcut when save" in withCtx { db =>
    //given
    val module = new FileListModule(db)
    val dao = module.folderShortcutDao
    val service = module.folderShortcutsService
    val path = "test/path"
    
    for {
      _ <- dao.deleteAll().toFuture
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
  
  it should "update existing folder shortcut when save" in withCtx { db =>
    //given
    val module = new FileListModule(db)
    val dao = module.folderShortcutDao
    val service = module.folderShortcutsService
    val path = "test/path"
    
    for {
      _ <- dao.deleteAll().toFuture
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
  
  it should "delete folder shortcut when delete" in withCtx { db =>
    //given
    val module = new FileListModule(db)
    val dao = module.folderShortcutDao
    val service = module.folderShortcutsService
    val path = "test/path"
    
    for {
      _ <- dao.deleteAll().toFuture
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
    val mocks = new FolderShortcutMocks
    val service = new FolderShortcutsServiceImpl(mocks.dao)
    val error = js.Error("test error")

    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    //then
    mocks.getAll.expects().returning(js.Promise.reject(error))
    errorLogger.expects("Failed to read folder shortcuts, error: scala.scalajs.js.JavaScriptException: Error: test error")

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
    val mocks = new FolderShortcutMocks
    val service = new FolderShortcutsServiceImpl(mocks.dao)
    val path = "test/path"
    val error = js.Error("test error")

    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    //then
    mocks.save.expects(*).returning(js.Promise.reject(error))
    errorLogger.expects("Failed to save folder shortcut, error: scala.scalajs.js.JavaScriptException: Error: test error")

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
    val mocks = new FolderShortcutMocks
    val service = new FolderShortcutsServiceImpl(mocks.dao)
    val error = js.Error("test error")
    val index = 0

    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    //then
    mocks.delete.expects(index).returning(js.Promise.reject(error))
    errorLogger.expects("Failed to delete folder shortcut, error: scala.scalajs.js.JavaScriptException: Error: test error")

    //when
    val resultF = service.delete(index)

    //then
    resultF.map { _ =>
      js.Dynamic.global.console.error = savedConsoleError
      Succeeded
    }
  }
}
