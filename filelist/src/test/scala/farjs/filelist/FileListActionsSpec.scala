package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.FileListActionsSpec._
import farjs.filelist.api._
import farjs.ui.task.Task
import org.scalatest.Succeeded
import scommons.nodejs.path
import scommons.nodejs.test.AsyncTestSpec

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

class FileListActionsSpec extends AsyncTestSpec {

  //noinspection TypeAnnotation
  class Api(capabilities: Set[String] = Set.empty) {
    val readDir2 = mockFunction[Option[String], String, Future[FileListDir]]
    val readDir = mockFunction[String, Future[FileListDir]]
    val delete = mockFunction[String, Seq[FileListItem], Future[Unit]]
    val mkDirs = mockFunction[List[String], Future[Unit]]
    val readFile = mockFunction[List[String], FileListItem, Double, Future[FileSource]]
    val writeFile = mockFunction[List[String], String, FileListItem => Future[Option[Boolean]], Future[Option[FileTarget]]]
    
    val api = new MockFileListApi(
      capabilitiesMock = capabilities,
      readDir2Mock = readDir2,
      readDirMock = readDir,
      deleteMock = delete,
      mkDirsMock = mkDirs,
      readFileMock = readFile,
      writeFileMock = writeFile
    )
  }

  //noinspection TypeAnnotation
  class Source {
    val readNextBytes = mockFunction[Uint8Array, Future[Int]]
    val close = mockFunction[Future[Unit]]

    val source = new MockFileSource(
      readNextBytesMock = readNextBytes,
      closeMock = close
    )
  }

  //noinspection TypeAnnotation
  class Target {
    val writeNextBytes = mockFunction[Uint8Array, Int, Future[Double]]
    val setAttributes = mockFunction[FileListItem, Future[Unit]]
    val close = mockFunction[Future[Unit]]
    val delete = mockFunction[Future[Unit]]

    val target = new MockFileTarget(
      writeNextBytesMock = writeNextBytes,
      setAttributesMock = setAttributes,
      closeMock = close,
      deleteMock = delete
    )
  }

  it should "return api capabilities" in {
    //given
    val capabilities = Set("test.capability")
    val api = new Api(capabilities)
    val actions = new FileListActionsTest(api.api)
    
    //when & then
    actions.capabilities shouldBe capabilities
  }

  it should "dispatch FileListDirChangedAction when changeDir" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val dispatch = mockFunction[Any, Any]
    val currDir = FileListDir("/", isRoot = true, items = js.Array(FileListItem("file 1")))
    val parent: Option[String] = Some("/")
    val dir = "test dir"

    api.readDir2.expects(parent, dir).returning(Future.successful(currDir))
    
    //then
    dispatch.expects(FileListDirChangedAction(dir, currDir))
    
    //when
    val FileListDirChangeAction(task) =
      actions.changeDir(dispatch, parent, dir)
    
    //then
    task.message shouldBe "Changing Dir"
    task.result.toFuture.map(_ => Succeeded)
  }
  
  it should "dispatch FileListDirUpdatedAction when updateDir" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val dispatch = mockFunction[Any, Any]
    val currDir = FileListDir("/", isRoot = true, items = js.Array(FileListItem("file 1")))
    val path = "/test/path"

    api.readDir.expects(path).returning(Future.successful(currDir))
    
    //then
    dispatch.expects(FileListDirUpdatedAction(currDir))
    
    //when
    val FileListDirUpdateAction(task) =
      actions.updateDir(dispatch, path)
    
    //then
    task.message shouldBe "Updating Dir"
    task.result.toFuture.map(_ => Succeeded)
  }
  
  it should "dispatch FileListItemCreatedAction when createDir(multiple=false)" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val dispatch = mockFunction[Any, Any]
    val currDir = FileListDir("/", isRoot = true, items = js.Array(FileListItem("file 1")))
    val parent = "/parent"
    val dir = "test/dir"
    val multiple = false

    api.mkDirs.expects(List(parent, dir)).returning(Future.unit)
    api.readDir.expects(parent).returning(Future.successful(currDir))
    
    //then
    dispatch.expects(FileListItemCreatedAction(dir, currDir))
    
    //when
    val FileListDirCreateAction(task) =
      actions.createDir(dispatch, parent, dir, multiple)
    
    //then
    task.message shouldBe "Creating Dir"
    task.result.toFuture.map(_ => Succeeded)
  }
  
  it should "dispatch FileListItemCreatedAction when createDir(multiple=true)" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val dispatch = mockFunction[Any, Any]
    val currDir = FileListDir("/", isRoot = true, items = js.Array(FileListItem("file 1")))
    val parent = "parent"
    val dir = path.join("test", "dir")
    val multiple = true

    api.mkDirs.expects(List(parent, "test", "dir")).returning(Future.unit)
    api.readDir.expects(parent).returning(Future.successful(currDir))
    
    //then
    dispatch.expects(FileListItemCreatedAction("test", currDir))
    
    //when
    val FileListDirCreateAction(task) =
      actions.createDir(dispatch, parent, dir, multiple)
    
    //then
    task.message shouldBe "Creating Dir"
    task.result.toFuture.map(_ => Succeeded)
  }
  
  it should "dispatch FileListDirUpdatedAction when deleteAction" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val dispatch = mockFunction[Any, Any]
    val dir = "test dir"
    val items = List(FileListItem("file 1"))
    val currDir = FileListDir("/", isRoot = true, items = js.Array(FileListItem("file 1")))

    api.delete.expects(dir, items).returning(Future.successful(()))
    api.readDir.expects(dir).returning(Future.successful(currDir))
    
    //then
    dispatch.expects(FileListDirUpdatedAction(currDir))
    var resultAction: FileListDirUpdateAction = null
    dispatch.expects(*).onCall { action: Any =>
      resultAction = action.asInstanceOf[FileListDirUpdateAction]
    }
    
    //when
    val FileListTaskAction(task) =
      actions.deleteAction(dispatch, dir, items)
    
    //then
    task.message shouldBe "Deleting Items"
    task.result.toFuture.flatMap { _ =>
      inside(resultAction) { case FileListDirUpdateAction(Task("Updating Dir", future)) =>
        future.map(_ => Succeeded)
      }
    }
  }
  
  it should "process sub-dirs and return true when scanDirs" in {
    //given
    val api = new Api
    val onNextDir = mockFunction[String, Seq[FileListItem], Boolean]
    val actions = new FileListActionsTest(api.api)
    val parent = "parent-dir"
    val items = List(
      FileListItem("dir 1", isDir = true),
      FileListItem("file 2")
    )
    val res = FileListDir("dir1", isRoot = false, js.Array(
      FileListItem("dir 3", isDir = true),
      FileListItem("file 4")
    ))

    api.readDir2.expects(Some(parent), "dir 1")
      .returning(Future.successful(res))
    api.readDir2.expects(Some(res.path), "dir 3")
      .returning(Future.successful(FileListDir("dir3", isRoot = false, js.Array())))
    onNextDir.expects(res.path, *).onCall { (_, items) =>
      items.toList shouldBe res.items.toList
      true
    }
    onNextDir.expects("dir3", *).onCall { (_, items) =>
      items.length shouldBe 0
      true
    }
    
    //when
    val resultF = actions.scanDirs(parent, items, onNextDir)
    
    //then
    resultF.map { res =>
      res shouldBe true
    }
  }
  
  it should "process sub-dirs and return false when scanDirs" in {
    //given
    val api = new Api
    val onNextDir = mockFunction[String, Seq[FileListItem], Boolean]
    val actions = new FileListActionsTest(api.api)
    val parent = "parent-dir"
    val items = List(
      FileListItem("dir 1", isDir = true),
      FileListItem("file 2")
    )
    val res = FileListDir("dir1", isRoot = false, js.Array(
      FileListItem("dir 3", isDir = true),
      FileListItem("file 4")
    ))

    api.readDir2.expects(Some(parent), "dir 1")
      .returning(Future.successful(res))
    onNextDir.expects(res.path, *).onCall { (_, items) =>
      items.toList shouldBe res.items.toList
      false
    }
    
    //when
    val resultF = actions.scanDirs(parent, items, onNextDir)
    
    //then
    resultF.map { res =>
      res shouldBe false
    }
  }

  it should "copy new file when copyFile" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val srcDirs = List("parent-dir")
    val file = FileListItem("test_file")
    val dstDirs = List("target-dir")
    val onExists = mockFunction[FileListItem, Future[Option[Boolean]]]
    val onProgress = mockFunction[Double, Future[Boolean]]
    val position = 1234.0
    
    val source = new Source
    source.readNextBytes.expects(*).onCall { buff: Uint8Array =>
      buff.length shouldBe (64 * 1024)
      Future.successful(123)
    }
    source.readNextBytes.expects(*).onCall { _: Uint8Array =>
      Future.successful(0)
    }
    source.close.expects().returning(Future.unit)
    
    val target = new Target
    target.writeNextBytes.expects(*, *).onCall { (buff: Uint8Array, length: Int) =>
      buff.length shouldBe (64 * 1024)
      length shouldBe 123
      Future.successful(position)
    }
    target.setAttributes.expects(file).returning(Future.unit)
    target.close.expects().returning(Future.unit)
    val dstName = "newName"

    //then
    api.writeFile.expects(dstDirs, dstName, *).returning(Future.successful(Some(target.target)))
    api.readFile.expects(srcDirs, file, 0.0).returning(Future.successful(source.source))
    onExists.expects(*).never()
    onProgress.expects(position).returning(Future.successful(true))
    
    //when
    val resultF = actions.copyFile(srcDirs, file, actions.writeFile(dstDirs, dstName, onExists), onProgress)
    
    //then
    resultF.map { res =>
      res shouldBe true
    }
  }

  it should "overwrite existing file when copyFile" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val srcDirs = List("parent-dir")
    val file = FileListItem("test_file")
    val existing = FileListItem("existing_file", size = 12)
    val dstDirs = List("target-dir")
    val onExists = mockFunction[FileListItem, Future[Option[Boolean]]]
    val onProgress = mockFunction[Double, Future[Boolean]]
    val position = 1234.0
    
    val source = new Source
    source.readNextBytes.expects(*).onCall { buff: Uint8Array =>
      buff.length shouldBe (64 * 1024)
      Future.successful(123)
    }
    source.readNextBytes.expects(*).onCall { _: Uint8Array =>
      Future.successful(0)
    }
    source.close.expects().returning(Future.unit)
    
    val target = new Target
    target.writeNextBytes.expects(*, *).onCall { (buff: Uint8Array, length: Int) =>
      buff.length shouldBe (64 * 1024)
      length shouldBe 123
      Future.successful(position)
    }
    target.setAttributes.expects(file).returning(Future.unit)
    target.close.expects().returning(Future.unit)
    val dstName = "newName"

    //then
    onExists.expects(existing).returning(Future.successful(Some(true)))
    api.writeFile.expects(dstDirs, dstName, *).onCall { (_, _, onExists) =>
      onExists(existing).map { res =>
        res shouldBe Some(true)
        Some(target.target)
      }
    }
    api.readFile.expects(srcDirs, file, 0.0).returning(Future.successful(source.source))
    onProgress.expects(position).returning(Future.successful(true))
    
    //when
    val resultF = actions.copyFile(srcDirs, file, actions.writeFile(dstDirs, dstName, onExists), onProgress)
    
    //then
    resultF.map { res =>
      res shouldBe true
    }
  }

  it should "append to existing file when copyFile" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val srcDirs = List("parent-dir")
    val file = FileListItem("test_file")
    val existing = FileListItem("existing_file", size = 12)
    val dstDirs = List("target-dir")
    val onExists = mockFunction[FileListItem, Future[Option[Boolean]]]
    val onProgress = mockFunction[Double, Future[Boolean]]
    val position = 1234.0
    
    val source = new Source
    source.readNextBytes.expects(*).onCall { buff: Uint8Array =>
      buff.length shouldBe (64 * 1024)
      Future.successful(123)
    }
    source.readNextBytes.expects(*).onCall { _: Uint8Array =>
      Future.successful(0)
    }
    source.close.expects().returning(Future.unit)
    
    val target = new Target
    target.writeNextBytes.expects(*, *).onCall { (buff: Uint8Array, length: Int) =>
      buff.length shouldBe (64 * 1024)
      length shouldBe 123
      Future.successful(position)
    }
    target.setAttributes.expects(file).returning(Future.unit)
    target.close.expects().returning(Future.unit)
    val dstName = "newName"

    //then
    onExists.expects(existing).returning(Future.successful(Some(false)))
    api.writeFile.expects(dstDirs, dstName, *).onCall { (_, _, onExists) =>
      onExists(existing).map { res =>
        res shouldBe Some(false)
        Some(target.target)
      }
    }
    api.readFile.expects(srcDirs, file, 0.0).returning(Future.successful(source.source))
    onProgress.expects(position).returning(Future.successful(true))
    
    //when
    val resultF = actions.copyFile(srcDirs, file, actions.writeFile(dstDirs, dstName, onExists), onProgress)
    
    //then
    resultF.map { res =>
      res shouldBe true
    }
  }

  it should "call onProgress(file.size) if skip existing when copyFile" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val srcDirs = List("parent-dir")
    val file = FileListItem("test_file", size = 123)
    val existing = FileListItem("existing_file", size = 12)
    val dstDirs = List("target-dir")
    val onExists = mockFunction[FileListItem, Future[Option[Boolean]]]
    val onProgress = mockFunction[Double, Future[Boolean]]
    val dstName = "newName"
    
    //then
    onExists.expects(existing).returning(Future.successful(None))
    api.writeFile.expects(dstDirs, dstName, *).onCall { (_, _, onExists) =>
      onExists(existing).map { res =>
        res shouldBe None
        None
      }
    }
    onProgress.expects(file.size).returning(Future.successful(false))
    
    //when
    val resultF = actions.copyFile(srcDirs, file, actions.writeFile(dstDirs, dstName, onExists), onProgress)
    
    //then
    resultF.map { res =>
      res shouldBe false
    }
  }

  it should "return false and delete target file if cancelled when copyFile" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val srcDirs = List("parent-dir")
    val file = FileListItem("test_file")
    val dstDirs = List("target-dir")
    val onExists = mockFunction[FileListItem, Future[Option[Boolean]]]
    val onProgress = mockFunction[Double, Future[Boolean]]
    val position = 1234.0
    
    val source = new Source
    source.readNextBytes.expects(*).onCall { buff: Uint8Array =>
      buff.length shouldBe (64 * 1024)
      Future.successful(123)
    }
    source.close.expects().returning(Future.unit)
    
    val target = new Target
    target.writeNextBytes.expects(*, *).onCall { (buff: Uint8Array, length: Int) =>
      buff.length shouldBe (64 * 1024)
      length shouldBe 123
      Future.successful(position)
    }
    target.close.expects().returning(Future.unit)
    target.delete.expects().returning(Future.unit)
    val dstName = "newName"

    //then
    api.writeFile.expects(dstDirs, dstName, *).returning(Future.successful(Some(target.target)))
    api.readFile.expects(srcDirs, file, 0.0).returning(Future.successful(source.source))
    onExists.expects(*).never()
    onProgress.expects(position).returning(Future.successful(false))
    
    //when
    val resultF = actions.copyFile(srcDirs, file, actions.writeFile(dstDirs, dstName, onExists), onProgress)
    
    //then
    resultF.map { res =>
      res shouldBe false
    }
  }

  it should "return failed Future and delete target file if failed when copyFile" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val srcDirs = List("parent-dir")
    val file = FileListItem("test_file")
    val dstDirs = List("target-dir")
    val onExists = mockFunction[FileListItem, Future[Option[Boolean]]]
    val onProgress = mockFunction[Double, Future[Boolean]]
    val error = new Exception("test error")
    
    val source = new Source
    source.readNextBytes.expects(*).onCall { buff: Uint8Array =>
      buff.length shouldBe (64 * 1024)
      Future.failed(error)
    }
    source.close.expects().returning(Future.unit)
    
    val target = new Target
    target.writeNextBytes.expects(*, *).never()
    target.close.expects().returning(Future.unit)
    target.delete.expects().returning(Future.unit)
    val dstName = "newName"

    //then
    api.writeFile.expects(dstDirs, dstName, *).returning(Future.successful(Some(target.target)))
    api.readFile.expects(srcDirs, file, 0.0).returning(Future.successful(source.source))
    onExists.expects(*).never()
    onProgress.expects(*).never()
    
    //when
    val resultF = actions.copyFile(srcDirs, file, actions.writeFile(dstDirs, dstName, onExists), onProgress)
    
    //then
    resultF.failed.map { ex =>
      ex shouldBe error
    }
  }
}

//noinspection NotImplementedCode
object FileListActionsSpec {

  private class FileListActionsTest(apiMock: FileListApi)
    extends FileListActions {

    protected def api: FileListApi = apiMock

    val isLocalFS: Boolean = true

    def getDriveRoot(path: String): Future[Option[String]] = ???
  }
}
