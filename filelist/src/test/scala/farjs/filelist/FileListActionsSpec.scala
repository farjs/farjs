package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.FileListActionsSpec._
import farjs.filelist.api._
import farjs.filelist.fs.FSService
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.redux.task.FutureTask

import scala.concurrent.Future
import scala.scalajs.js.typedarray.Uint8Array

class FileListActionsSpec extends AsyncTestSpec {

  it should "dispatch FileListTaskAction when openInDefaultApp" in {
    //given
    val fsService = mock[FSService]
    val actions = new FileListActionsTest(mock[FileListApi])
    actions.fsService = fsService
    val parent = "test dir"
    val item = "test item"
    
    //then
    (fsService.openItem _).expects(parent, item).returning(Future.unit)
    
    //when
    val FileListTaskAction(FutureTask(msg, future)) =
      actions.openInDefaultApp(parent, item)
    
    //then
    msg shouldBe "Opening default app"
    future.map(_ => Succeeded)
  }
  
  it should "dispatch FileListDirChangedAction when changeDir" in {
    //given
    val api = mock[FileListApi]
    val fsService = mock[FSService]
    val actions = new FileListActionsTest(api)
    actions.fsService = fsService
    val dispatch = mockFunction[Any, Any]
    val currDir = FileListDir("/", isRoot = true, items = List(FileListItem("file 1")))
    val isRight = true
    val parent: Option[String] = Some("/")
    val dir = "test dir"

    (api.readDir(_: Option[String], _: String)).expects(parent, dir)
      .returning(Future.successful(currDir))
    
    //then
    dispatch.expects(FileListDirChangedAction(isRight, dir, currDir))
    
    //when
    val FileListDirChangeAction(FutureTask(msg, future)) =
      actions.changeDir(dispatch, isRight, parent, dir)
    
    //then
    msg shouldBe "Changing Dir"
    future.map(_ => Succeeded)
  }
  
  it should "dispatch FileListDirUpdatedAction when updateDir" in {
    //given
    val api = mock[FileListApi]
    val fsService = mock[FSService]
    val actions = new FileListActionsTest(api)
    actions.fsService = fsService
    val dispatch = mockFunction[Any, Any]
    val currDir = FileListDir("/", isRoot = true, items = List(FileListItem("file 1")))
    val isRight = true
    val path = "/test/path"

    (api.readDir(_: String)).expects(path).returning(Future.successful(currDir))
    
    //then
    dispatch.expects(FileListDirUpdatedAction(isRight, currDir))
    
    //when
    val FileListDirUpdateAction(FutureTask(msg, future)) =
      actions.updateDir(dispatch, isRight, path)
    
    //then
    msg shouldBe "Updating Dir"
    future.map(_ => Succeeded)
  }
  
  it should "dispatch FileListDirChangedAction when createDir(multiple=false)" in {
    //given
    val api = mock[FileListApi]
    val fsService = mock[FSService]
    val actions = new FileListActionsTest(api)
    actions.fsService = fsService
    val dispatch = mockFunction[Any, Any]
    val currDir = FileListDir("/", isRoot = true, items = List(FileListItem("file 1")))
    val isRight = true
    val parent = "/parent"
    val dir = "test/dir"
    val multiple = false

    (api.mkDirs _).expects(List(parent, dir)).returning(Future.unit)
    (api.readDir(_: String)).expects(parent).returning(Future.successful(currDir))
    
    //then
    dispatch.expects(FileListDirCreatedAction(isRight, dir, currDir))
    
    //when
    val FileListDirCreateAction(FutureTask(msg, future)) =
      actions.createDir(dispatch, isRight, parent, dir, multiple)
    
    //then
    msg shouldBe "Creating Dir"
    future.map(_ => Succeeded)
  }
  
  it should "dispatch FileListDirChangedAction when createDir(multiple=true)" in {
    //given
    val api = mock[FileListApi]
    val fsService = mock[FSService]
    val actions = new FileListActionsTest(api)
    actions.fsService = fsService
    val dispatch = mockFunction[Any, Any]
    val currDir = FileListDir("/", isRoot = true, items = List(FileListItem("file 1")))
    val isRight = true
    val parent = "/parent"
    val dir = "test/dir"
    val multiple = true

    (api.mkDirs _).expects(List(parent, "test", "dir")).returning(Future.unit)
    (api.readDir(_: String)).expects(parent).returning(Future.successful(currDir))
    
    //then
    dispatch.expects(FileListDirCreatedAction(isRight, "test", currDir))
    
    //when
    val FileListDirCreateAction(FutureTask(msg, future)) =
      actions.createDir(dispatch, isRight, parent, dir, multiple)
    
    //then
    msg shouldBe "Creating Dir"
    future.map(_ => Succeeded)
  }
  
  it should "dispatch FileListItemsDeletedAction when deleteItems" in {
    //given
    val api = mock[FileListApi]
    val actions = new FileListActionsTest(api)
    val dispatch = mockFunction[Any, Any]
    val dir = "test dir"
    val items = List(FileListItem("file 1"))
    val isRight = true

    (api.delete _).expects(dir, items).returning(Future.successful(()))
    
    //then
    dispatch.expects(FileListItemsDeletedAction(isRight))
    
    //when
    val FileListItemsDeleteAction(FutureTask(msg, future)) =
      actions.deleteAction(dispatch, isRight, dir, items)
    
    //then
    msg shouldBe "Deleting Items"
    future.map(_ => Succeeded)
  }
  
  it should "process sub-dirs and return true when scanDirs" in {
    //given
    val api = mock[FileListApi]
    val onNextDir = mockFunction[String, Seq[FileListItem], Boolean]
    val actions = new FileListActionsTest(api)
    val parent = "parent-dir"
    val items = List(
      FileListItem("dir 1", isDir = true),
      FileListItem("file 2")
    )
    val res = FileListDir("dir1", isRoot = false, List(
      FileListItem("dir 3", isDir = true),
      FileListItem("file 4")
    ))

    (api.readDir(_: Option[String], _: String)).expects(Some(parent), "dir 1")
      .returning(Future.successful(res))
    (api.readDir(_: Option[String], _: String)).expects(Some(res.path), "dir 3")
      .returning(Future.successful(FileListDir("dir3", isRoot = false, Nil)))
    onNextDir.expects(res.path, res.items).returning(true)
    onNextDir.expects("dir3", Nil).returning(true)
    
    //when
    val resultF = actions.scanDirs(parent, items, onNextDir)
    
    //then
    resultF.map { res =>
      res shouldBe true
    }
  }
  
  it should "process sub-dirs and return false when scanDirs" in {
    //given
    val api = mock[FileListApi]
    val onNextDir = mockFunction[String, Seq[FileListItem], Boolean]
    val actions = new FileListActionsTest(api)
    val parent = "parent-dir"
    val items = List(
      FileListItem("dir 1", isDir = true),
      FileListItem("file 2")
    )
    val res = FileListDir("dir1", isRoot = false, List(
      FileListItem("dir 3", isDir = true),
      FileListItem("file 4")
    ))

    (api.readDir(_: Option[String], _: String)).expects(Some(parent), "dir 1")
      .returning(Future.successful(res))
    onNextDir.expects(res.path, res.items).returning(false)
    
    //when
    val resultF = actions.scanDirs(parent, items, onNextDir)
    
    //then
    resultF.map { res =>
      res shouldBe false
    }
  }

  it should "copy new file when copyFile" in {
    //given
    val api = mock[FileListApi]
    val actions = new FileListActionsTest(api)
    val srcDirs = List("parent-dir")
    val file = FileListItem("test_file")
    val dstDirs = List("target-dir")
    val onExists = mockFunction[FileListItem, Future[Option[Boolean]]]
    val onProgress = mockFunction[Double, Future[Boolean]]
    val position = 1234.0
    
    val source = mock[FileSource]
    (source.readNextBytes _).expects(*).onCall { buff: Uint8Array =>
      buff.length shouldBe (64 * 1024)
      Future.successful(123)
    }
    (source.readNextBytes _).expects(*).onCall { _: Uint8Array =>
      Future.successful(0)
    }
    (source.close _).expects().returning(Future.unit)
    
    val target = mock[FileTarget]
    (target.writeNextBytes _).expects(*, *).onCall { (buff: Uint8Array, length: Int) =>
      buff.length shouldBe (64 * 1024)
      length shouldBe 123
      Future.successful(position)
    }
    (target.setModTime _).expects(file).returning(Future.unit)
    (target.close _).expects().returning(Future.unit)

    //then
    (api.writeFile _).expects(dstDirs, file.name, *).returning(Future.successful(Some(target)))
    (api.readFile _).expects(srcDirs, file, 0.0).returning(Future.successful(source))
    onExists.expects(*).never()
    onProgress.expects(position).returning(Future.successful(true))
    
    //when
    val resultF = actions.copyFile(srcDirs, file, dstDirs, onExists, onProgress)
    
    //then
    resultF.map { res =>
      res shouldBe true
    }
  }

  it should "overwrite existing file when copyFile" in {
    //given
    val api = mock[FileListApi]
    val actions = new FileListActionsTest(api)
    val srcDirs = List("parent-dir")
    val file = FileListItem("test_file")
    val existing = FileListItem("existing_file", size = 12)
    val dstDirs = List("target-dir")
    val onExists = mockFunction[FileListItem, Future[Option[Boolean]]]
    val onProgress = mockFunction[Double, Future[Boolean]]
    val position = 1234.0
    
    val source = mock[FileSource]
    (source.readNextBytes _).expects(*).onCall { buff: Uint8Array =>
      buff.length shouldBe (64 * 1024)
      Future.successful(123)
    }
    (source.readNextBytes _).expects(*).onCall { _: Uint8Array =>
      Future.successful(0)
    }
    (source.close _).expects().returning(Future.unit)
    
    val target = mock[FileTarget]
    (target.writeNextBytes _).expects(*, *).onCall { (buff: Uint8Array, length: Int) =>
      buff.length shouldBe (64 * 1024)
      length shouldBe 123
      Future.successful(position)
    }
    (target.setModTime _).expects(file).returning(Future.unit)
    (target.close _).expects().returning(Future.unit)

    //then
    onExists.expects(existing).returning(Future.successful(Some(true)))
    (api.writeFile _).expects(dstDirs, file.name, *).onCall { (_, _, onExists) =>
      onExists(existing).map { res =>
        res shouldBe Some(true)
        Some(target)
      }
    }
    (api.readFile _).expects(srcDirs, file, 0.0).returning(Future.successful(source))
    onProgress.expects(position).returning(Future.successful(true))
    
    //when
    val resultF = actions.copyFile(srcDirs, file, dstDirs, onExists, onProgress)
    
    //then
    resultF.map { res =>
      res shouldBe true
    }
  }

  it should "append to existing file when copyFile" in {
    //given
    val api = mock[FileListApi]
    val actions = new FileListActionsTest(api)
    val srcDirs = List("parent-dir")
    val file = FileListItem("test_file")
    val existing = FileListItem("existing_file", size = 12)
    val dstDirs = List("target-dir")
    val onExists = mockFunction[FileListItem, Future[Option[Boolean]]]
    val onProgress = mockFunction[Double, Future[Boolean]]
    val position = 1234.0
    
    val source = mock[FileSource]
    (source.readNextBytes _).expects(*).onCall { buff: Uint8Array =>
      buff.length shouldBe (64 * 1024)
      Future.successful(123)
    }
    (source.readNextBytes _).expects(*).onCall { _: Uint8Array =>
      Future.successful(0)
    }
    (source.close _).expects().returning(Future.unit)
    
    val target = mock[FileTarget]
    (target.writeNextBytes _).expects(*, *).onCall { (buff: Uint8Array, length: Int) =>
      buff.length shouldBe (64 * 1024)
      length shouldBe 123
      Future.successful(position)
    }
    (target.setModTime _).expects(file).returning(Future.unit)
    (target.close _).expects().returning(Future.unit)

    //then
    onExists.expects(existing).returning(Future.successful(Some(false)))
    (api.writeFile _).expects(dstDirs, file.name, *).onCall { (_, _, onExists) =>
      onExists(existing).map { res =>
        res shouldBe Some(false)
        Some(target)
      }
    }
    (api.readFile _).expects(srcDirs, file, existing.size).returning(Future.successful(source))
    onProgress.expects(position).returning(Future.successful(true))
    
    //when
    val resultF = actions.copyFile(srcDirs, file, dstDirs, onExists, onProgress)
    
    //then
    resultF.map { res =>
      res shouldBe true
    }
  }

  it should "call onProgress(file.size) if skip existing when copyFile" in {
    //given
    val api = mock[FileListApi]
    val actions = new FileListActionsTest(api)
    val srcDirs = List("parent-dir")
    val file = FileListItem("test_file", size = 123)
    val existing = FileListItem("existing_file", size = 12)
    val dstDirs = List("target-dir")
    val onExists = mockFunction[FileListItem, Future[Option[Boolean]]]
    val onProgress = mockFunction[Double, Future[Boolean]]
    
    //then
    onExists.expects(existing).returning(Future.successful(None))
    (api.writeFile _).expects(dstDirs, file.name, *).onCall { (_, _, onExists) =>
      onExists(existing).map { res =>
        res shouldBe None
        None
      }
    }
    onProgress.expects(file.size).returning(Future.successful(false))
    
    //when
    val resultF = actions.copyFile(srcDirs, file, dstDirs, onExists, onProgress)
    
    //then
    resultF.map { res =>
      res shouldBe false
    }
  }

  it should "return false and delete target file if cancelled when copyFile" in {
    //given
    val api = mock[FileListApi]
    val actions = new FileListActionsTest(api)
    val srcDirs = List("parent-dir")
    val file = FileListItem("test_file")
    val dstDirs = List("target-dir")
    val onExists = mockFunction[FileListItem, Future[Option[Boolean]]]
    val onProgress = mockFunction[Double, Future[Boolean]]
    val position = 1234.0
    
    val source = mock[FileSource]
    (source.readNextBytes _).expects(*).onCall { buff: Uint8Array =>
      buff.length shouldBe (64 * 1024)
      Future.successful(123)
    }
    (source.close _).expects().returning(Future.unit)
    
    val target = mock[FileTarget]
    (target.writeNextBytes _).expects(*, *).onCall { (buff: Uint8Array, length: Int) =>
      buff.length shouldBe (64 * 1024)
      length shouldBe 123
      Future.successful(position)
    }
    (target.close _).expects().returning(Future.unit)
    (target.delete _).expects().returning(Future.unit)

    //then
    (api.writeFile _).expects(dstDirs, file.name, *).returning(Future.successful(Some(target)))
    (api.readFile _).expects(srcDirs, file, 0.0).returning(Future.successful(source))
    onExists.expects(*).never()
    onProgress.expects(position).returning(Future.successful(false))
    
    //when
    val resultF = actions.copyFile(srcDirs, file, dstDirs, onExists, onProgress)
    
    //then
    resultF.map { res =>
      res shouldBe false
    }
  }
}

object FileListActionsSpec {

  private class FileListActionsTest(apiMock: FileListApi)
    extends FileListActions {

    protected def api: FileListApi = apiMock
  }
}
