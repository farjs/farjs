package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.FileListActionsSpec._
import farjs.filelist.api.FileListDirSpec.assertFileListDir
import farjs.filelist.api.FileListItemSpec.assertFileListItem
import farjs.filelist.api._
import farjs.ui.task.{Task, TaskAction}
import org.scalactic.source.Position
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.path
import scommons.nodejs.test.AsyncTestSpec

import scala.scalajs.js
import scala.scalajs.js.JSConverters.JSRichFutureNonThenable
import scala.scalajs.js.typedarray.Uint8Array

class FileListActionsSpec extends AsyncTestSpec {

  //noinspection TypeAnnotation
  class Api(capabilities: js.Set[FileListCapability] = js.Set.empty) {
    val readDir = mockFunction[String, js.UndefOr[String], js.Promise[FileListDir]]
    val delete = mockFunction[String, js.Array[FileListItem], js.Promise[Unit]]
    val mkDirs = mockFunction[js.Array[String], js.Promise[String]]
    val readFile = mockFunction[String, FileListItem, Double, js.Promise[FileSource]]
    val writeFile = mockFunction[String, String, js.Function1[FileListItem, js.Promise[js.UndefOr[Boolean]]], js.Promise[js.UndefOr[FileTarget]]]
    
    val api = new MockFileListApi(
      capabilitiesMock = capabilities,
      readDirMock = readDir,
      deleteMock = delete,
      mkDirsMock = mkDirs,
      readFileMock = readFile,
      writeFileMock = writeFile
    )
  }

  //noinspection TypeAnnotation
  class Source {
    val readNextBytes = mockFunction[Uint8Array, js.Promise[Int]]
    val close = mockFunction[js.Promise[Unit]]

    val source = MockFileSource(
      readNextBytesMock = readNextBytes,
      closeMock = close
    )
  }

  //noinspection TypeAnnotation
  class Target {
    val writeNextBytes = mockFunction[Uint8Array, Int, js.Promise[Double]]
    val setAttributes = mockFunction[FileListItem, js.Promise[Unit]]
    val close = mockFunction[js.Promise[Unit]]
    val delete = mockFunction[js.Promise[Unit]]

    val target = MockFileTarget(
      writeNextBytesMock = writeNextBytes,
      setAttributesMock = setAttributes,
      closeMock = close,
      deleteMock = delete
    )
  }

  it should "dispatch FileListDirChangedAction when changeDir" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val dispatch = mockFunction[js.Any, Unit]
    val currDir = FileListDir("/", isRoot = true, items = js.Array(FileListItem("file 1")))
    val parent = "/"
    val dir = "test dir"

    api.readDir.expects(parent, dir: js.UndefOr[String]).returning(js.Promise.resolve[FileListDir](currDir))
    
    //then
    dispatch.expects(*).onCall { action: js.Any =>
      assertFileListDirChangedAction(action, FileListDirChangedAction(dir, currDir))
      ()
    }
    
    //when
    val TaskAction(task) =
      actions.changeDir(dispatch, parent, dir)
    
    //then
    task.message shouldBe "Changing Dir"
    task.result.toFuture.map(_ => Succeeded)
  }
  
  it should "dispatch FileListDirUpdatedAction when updateDir" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val dispatch = mockFunction[js.Any, Unit]
    val currDir = FileListDir("/", isRoot = true, items = js.Array(FileListItem("file 1")))
    val path = "/test/path"

    api.readDir.expects(path, js.undefined).returning(js.Promise.resolve[FileListDir](currDir))
    
    //then
    dispatch.expects(*).onCall { action: js.Any =>
      assertFileListDirUpdatedAction(action, FileListDirUpdatedAction(currDir))
      ()
    }
    
    //when
    val TaskAction(task) =
      actions.updateDir(dispatch, path)
    
    //then
    task.message shouldBe "Updating Dir"
    task.result.toFuture.map(_ => Succeeded)
  }
  
  it should "dispatch FileListItemCreatedAction when createDir(multiple=false)" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val dispatch = mockFunction[js.Any, Unit]
    val currDir = FileListDir("/", isRoot = true, items = js.Array(FileListItem("file 1")))
    val parent = "/parent"
    val dir = "test/dir"
    val multiple = false

    api.mkDirs.expects(*).onCall { dirs: js.Array[String] =>
      dirs.toList shouldBe List(parent, dir)
      js.Promise.resolve[String]("")
    }
    api.readDir.expects(parent, js.undefined).returning(js.Promise.resolve[FileListDir](currDir))
    
    //then
    dispatch.expects(*).onCall { action: Any =>
      assertFileListItemCreatedAction(action, FileListItemCreatedAction(dir, currDir))
      ()
    }
    
    //when
    val TaskAction(task) =
      actions.createDir(dispatch, parent, dir, multiple)
    
    //then
    task.message shouldBe "Creating Dir"
    task.result.toFuture.map(_ => Succeeded)
  }
  
  it should "dispatch FileListItemCreatedAction when createDir(multiple=true)" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val dispatch = mockFunction[js.Any, Unit]
    val currDir = FileListDir("/", isRoot = true, items = js.Array(FileListItem("file 1")))
    val parent = "parent"
    val dir = path.join("test", "dir")
    val multiple = true

    api.mkDirs.expects(*).onCall { dirs: js.Array[String] =>
      dirs.toList shouldBe List(parent, "test", "dir")
      js.Promise.resolve[String]("")
    }
    api.readDir.expects(parent, js.undefined).returning(js.Promise.resolve[FileListDir](currDir))
    
    //then
    dispatch.expects(*).onCall { action: Any =>
      assertFileListItemCreatedAction(action, FileListItemCreatedAction("test", currDir))
      ()
    }
    
    //when
    val TaskAction(task) =
      actions.createDir(dispatch, parent, dir, multiple)
    
    //then
    task.message shouldBe "Creating Dir"
    task.result.toFuture.map(_ => Succeeded)
  }
  
  it should "dispatch FileListDirUpdatedAction when deleteItems" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val dispatch = mockFunction[js.Any, Unit]
    val dir = "test dir"
    val items = js.Array(FileListItem("file 1"))
    val currDir = FileListDir("/", isRoot = true, items = js.Array(FileListItem("file 1")))

    api.delete.expects(dir, *).onCall { (_, resItems) =>
      resItems.toList shouldBe items.toList
      js.Promise.resolve[Unit](())
    }
    api.readDir.expects(dir, js.undefined).returning(js.Promise.resolve[FileListDir](currDir))
    
    //then
    var resultAction: TaskAction = null
    dispatch.expects(*).onCall { action: Any =>
      resultAction = action.asInstanceOf[TaskAction]
    }
    dispatch.expects(*).onCall { action: Any =>
      assertFileListDirUpdatedAction(action, FileListDirUpdatedAction(currDir))
      ()
    }
    
    //when
    val TaskAction(task) =
      actions.deleteItems(dispatch, dir, items)
    
    //then
    task.message shouldBe "Deleting Items"
    task.result.toFuture.flatMap { _ =>
      inside(resultAction) { case TaskAction(Task("Updating Dir", future)) =>
        future.map(_ => Succeeded)
      }
    }
  }
  
  it should "process sub-dirs and return true when scanDirs" in {
    //given
    val api = new Api
    val onNextDir = mockFunction[String, js.Array[FileListItem], Boolean]
    val actions = new FileListActionsTest(api.api)
    val parent = "parent-dir"
    val items = js.Array(
      FileListItem("dir 1", isDir = true),
      FileListItem("file 2")
    )
    val res = FileListDir("dir1", isRoot = false, js.Array(
      FileListItem("dir 3", isDir = true),
      FileListItem("file 4")
    ))

    api.readDir.expects(parent, "dir 1": js.UndefOr[String])
      .returning(js.Promise.resolve[FileListDir](res))
    api.readDir.expects(res.path, "dir 3": js.UndefOr[String])
      .returning(js.Promise.resolve[FileListDir](FileListDir("dir3", isRoot = false, js.Array())))
    onNextDir.expects(res.path, *).onCall { (_, items) =>
      items.toList shouldBe res.items.toList
      true
    }
    onNextDir.expects("dir3", *).onCall { (_, items) =>
      items.length shouldBe 0
      true
    }
    
    //when
    val resultF = actions.scanDirs(parent, items, onNextDir).toFuture
    
    //then
    resultF.map { res =>
      res shouldBe true
    }
  }
  
  it should "process sub-dirs and return false when scanDirs" in {
    //given
    val api = new Api
    val onNextDir = mockFunction[String, js.Array[FileListItem], Boolean]
    val actions = new FileListActionsTest(api.api)
    val parent = "parent-dir"
    val items = js.Array(
      FileListItem("dir 1", isDir = true),
      FileListItem("file 2")
    )
    val res = FileListDir("dir1", isRoot = false, js.Array(
      FileListItem("dir 3", isDir = true),
      FileListItem("file 4")
    ))

    api.readDir.expects(parent, "dir 1": js.UndefOr[String])
      .returning(js.Promise.resolve[FileListDir](res))
    onNextDir.expects(res.path, *).onCall { (_, items) =>
      items.toList shouldBe res.items.toList
      false
    }
    
    //when
    val resultF = actions.scanDirs(parent, items, onNextDir).toFuture
    
    //then
    resultF.map { res =>
      res shouldBe false
    }
  }

  it should "copy new file when copyFile" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val srcDir = "parent-dir"
    val file = FileListItem("test_file")
    val dstDir = "target-dir"
    val onExists = mockFunction[FileListItem, js.Promise[js.UndefOr[Boolean]]]
    val onProgress = mockFunction[Double, js.Promise[Boolean]]
    val position = 1234.0
    
    val source = new Source
    source.readNextBytes.expects(*).onCall { buff: Uint8Array =>
      buff.length shouldBe (64 * 1024)
      js.Promise.resolve[Int](123)
    }
    source.readNextBytes.expects(*).onCall { _: Uint8Array =>
      js.Promise.resolve[Int](0)
    }
    source.close.expects().returning(js.Promise.resolve[Unit](()))
    
    val target = new Target
    target.writeNextBytes.expects(*, *).onCall { (buff: Uint8Array, length: Int) =>
      buff.length shouldBe (64 * 1024)
      length shouldBe 123
      js.Promise.resolve[Double](position)
    }
    target.setAttributes.expects(file).returning(js.Promise.resolve[Unit](()))
    target.close.expects().returning(js.Promise.resolve[Unit](()))
    val dstName = "newName"

    //then
    api.writeFile.expects(*, dstName, *).onCall { (resDir, _, _) =>
      resDir shouldBe dstDir
      js.Promise.resolve[FileTarget](target.target)
    }
    api.readFile.expects(*, file, 0.0).onCall { (resDir, _, _) =>
      resDir shouldBe srcDir
      js.Promise.resolve[FileSource](source.source)
    }
    onExists.expects(*).never()
    onProgress.expects(position).returning(js.Promise.resolve[Boolean](true))
    
    //when
    val resultF = actions.copyFile(srcDir, file, api.writeFile(dstDir, dstName, onExists), onProgress).toFuture
    
    //then
    resultF.map { res =>
      res shouldBe true
    }
  }

  it should "overwrite existing file when copyFile" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val srcDir = "parent-dir"
    val file = FileListItem("test_file")
    val existing = FileListItem.copy(FileListItem("existing_file"))(size = 12)
    val dstDir = "target-dir"
    val onExists = mockFunction[FileListItem, js.Promise[js.UndefOr[Boolean]]]
    val onProgress = mockFunction[Double, js.Promise[Boolean]]
    val position = 1234.0
    
    val source = new Source
    source.readNextBytes.expects(*).onCall { buff: Uint8Array =>
      buff.length shouldBe (64 * 1024)
      js.Promise.resolve[Int](123)
    }
    source.readNextBytes.expects(*).onCall { _: Uint8Array =>
      js.Promise.resolve[Int](0)
    }
    source.close.expects().returning(js.Promise.resolve[Unit](()))
    
    val target = new Target
    target.writeNextBytes.expects(*, *).onCall { (buff: Uint8Array, length: Int) =>
      buff.length shouldBe (64 * 1024)
      length shouldBe 123
      js.Promise.resolve[Double](position)
    }
    target.setAttributes.expects(file).returning(js.Promise.resolve[Unit](()))
    target.close.expects().returning(js.Promise.resolve[Unit](()))
    val dstName = "newName"

    //then
    onExists.expects(*).onCall { item: FileListItem =>
      assertFileListItem(item, existing)
      js.Promise.resolve[js.UndefOr[Boolean]](true)
    }
    api.writeFile.expects(*, dstName, *).onCall { (dir, _, onExists) =>
      dir shouldBe dstDir
      onExists(existing).toFuture.map { res =>
        res shouldBe true
        target.target
      }.toJSPromise
    }
    api.readFile.expects(*, file, 0.0).onCall { (dir, _, _) =>
      dir shouldBe srcDir
      js.Promise.resolve[FileSource](source.source)
    }
    onProgress.expects(position).returning(js.Promise.resolve[Boolean](true))
    
    //when
    val resultF = actions.copyFile(srcDir, file, api.writeFile(dstDir, dstName, onExists), onProgress).toFuture
    
    //then
    resultF.map { res =>
      res shouldBe true
    }
  }

  it should "append to existing file when copyFile" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val srcDir = "parent-dir"
    val file = FileListItem("test_file")
    val existing = FileListItem.copy(FileListItem("existing_file"))(size = 12)
    val dstDir = "target-dir"
    val onExists = mockFunction[FileListItem, js.Promise[js.UndefOr[Boolean]]]
    val onProgress = mockFunction[Double, js.Promise[Boolean]]
    val position = 1234.0
    
    val source = new Source
    source.readNextBytes.expects(*).onCall { buff: Uint8Array =>
      buff.length shouldBe (64 * 1024)
      js.Promise.resolve[Int](123)
    }
    source.readNextBytes.expects(*).onCall { _: Uint8Array =>
      js.Promise.resolve[Int](0)
    }
    source.close.expects().returning(js.Promise.resolve[Unit](()))
    
    val target = new Target
    target.writeNextBytes.expects(*, *).onCall { (buff: Uint8Array, length: Int) =>
      buff.length shouldBe (64 * 1024)
      length shouldBe 123
      js.Promise.resolve[Double](position)
    }
    target.setAttributes.expects(file).returning(js.Promise.resolve[Unit](()))
    target.close.expects().returning(js.Promise.resolve[Unit](()))
    val dstName = "newName"

    //then
    onExists.expects(existing).returning(js.Promise.resolve[js.UndefOr[Boolean]](false))
    api.writeFile.expects(*, dstName, *).onCall { (dir, _, onExists) =>
      dir shouldBe dstDir
      onExists(existing).toFuture.map { res =>
        res shouldBe false
        target.target
      }.toJSPromise
    }
    api.readFile.expects(*, file, 0.0).onCall { (dir, _, _) =>
      dir shouldBe srcDir
      js.Promise.resolve[FileSource](source.source)
    }
    onProgress.expects(position).returning(js.Promise.resolve[Boolean](true))
    
    //when
    val resultF = actions.copyFile(srcDir, file, api.writeFile(dstDir, dstName, onExists), onProgress).toFuture
    
    //then
    resultF.map { res =>
      res shouldBe true
    }
  }

  it should "call onProgress(file.size) if skip existing when copyFile" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val srcDir = "parent-dir"
    val file = FileListItem.copy(FileListItem("test_file"))(size = 123)
    val existing = FileListItem.copy(FileListItem("existing_file"))(size = 12)
    val dstDir = "target-dir"
    val onExists = mockFunction[FileListItem, js.Promise[js.UndefOr[Boolean]]]
    val onProgress = mockFunction[Double, js.Promise[Boolean]]
    val dstName = "newName"
    
    //then
    onExists.expects(existing).returning(js.Promise.resolve[js.UndefOr[Boolean]](js.undefined))
    api.writeFile.expects(*, dstName, *).onCall { (dir, _, onExists) =>
      dir shouldBe dstDir
      onExists(existing).toFuture.map { res =>
        res shouldBe js.undefined
        js.undefined
      }.toJSPromise
    }
    onProgress.expects(file.size).returning(js.Promise.resolve[Boolean](false))
    
    //when
    val resultF = actions.copyFile(srcDir, file, api.writeFile(dstDir, dstName, onExists), onProgress).toFuture
    
    //then
    resultF.map { res =>
      res shouldBe false
    }
  }

  it should "return false and delete target file if cancelled when copyFile" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val srcDir = "parent-dir"
    val file = FileListItem("test_file")
    val dstDir = "target-dir"
    val onExists = mockFunction[FileListItem, js.Promise[js.UndefOr[Boolean]]]
    val onProgress = mockFunction[Double, js.Promise[Boolean]]
    val position = 1234.0
    
    val source = new Source
    source.readNextBytes.expects(*).onCall { buff: Uint8Array =>
      buff.length shouldBe (64 * 1024)
      js.Promise.resolve[Int](123)
    }
    source.close.expects().returning(js.Promise.resolve[Unit](()))
    
    val target = new Target
    target.writeNextBytes.expects(*, *).onCall { (buff: Uint8Array, length: Int) =>
      buff.length shouldBe (64 * 1024)
      length shouldBe 123
      js.Promise.resolve[Double](position)
    }
    target.close.expects().returning(js.Promise.resolve[Unit](()))
    target.delete.expects().returning(js.Promise.resolve[Unit](()))
    val dstName = "newName"

    //then
    api.writeFile.expects(*, dstName, *).onCall { (dir, _, _) =>
      dir shouldBe dstDir
      js.Promise.resolve[FileTarget](target.target)
    }
    api.readFile.expects(*, file, 0.0).onCall { (dir, _, _) =>
      dir shouldBe srcDir
      js.Promise.resolve[FileSource](source.source)
    }
    onExists.expects(*).never()
    onProgress.expects(position).returning(js.Promise.resolve[Boolean](false))
    
    //when
    val resultF = actions.copyFile(srcDir, file, api.writeFile(dstDir, dstName, onExists), onProgress).toFuture
    
    //then
    resultF.map { res =>
      res shouldBe false
    }
  }

  it should "return failed Future and delete target file if failed when copyFile" in {
    //given
    val api = new Api
    val actions = new FileListActionsTest(api.api)
    val srcDir = "parent-dir"
    val file = FileListItem("test_file")
    val dstDir = "target-dir"
    val onExists = mockFunction[FileListItem, js.Promise[js.UndefOr[Boolean]]]
    val onProgress = mockFunction[Double, js.Promise[Boolean]]
    val error = js.Error("test error")
    
    val source = new Source
    source.readNextBytes.expects(*).onCall { buff: Uint8Array =>
      buff.length shouldBe (64 * 1024)
      js.Promise.reject(error)
    }
    source.close.expects().returning(js.Promise.resolve[Unit](()))
    
    val target = new Target
    target.writeNextBytes.expects(*, *).never()
    target.close.expects().returning(js.Promise.resolve[Unit](()))
    target.delete.expects().returning(js.Promise.resolve[Unit](()))
    val dstName = "newName"

    //then
    api.writeFile.expects(*, dstName, *).onCall { (dir, _, _) =>
      dir shouldBe dstDir
      js.Promise.resolve[FileTarget](target.target)
    }
    api.readFile.expects(*, file, 0.0).onCall { (dir, _, _) =>
      dir shouldBe srcDir
      js.Promise.resolve[FileSource](source.source)
    }
    onExists.expects(*).never()
    onProgress.expects(*).never()
    
    //when
    val resultF = actions.copyFile(srcDir, file, api.writeFile(dstDir, dstName, onExists), onProgress).toFuture
    
    //then
    resultF.failed.map(inside(_) {
      case js.JavaScriptException(ex) =>
        ex shouldBe error
    })
  }
}

object FileListActionsSpec {

  private class FileListActionsTest(api: FileListApi) extends FileListActions(api)

  def assertFileListParamsChangedAction(action: Any, expected: FileListParamsChangedAction)(implicit position: Position): Assertion = {
    inside(action.asInstanceOf[FileListParamsChangedAction]) {
      case FileListParamsChangedAction(offset, index, selectedNames) =>
        offset shouldBe expected.offset
        index shouldBe expected.index
        selectedNames.toSet shouldBe expected.selectedNames.toSet
    }
  }

  def assertFileListDirChangedAction(action: Any, expected: FileListDirChangedAction)(implicit position: Position): Assertion = {
    inside(action.asInstanceOf[FileListDirChangedAction]) {
      case FileListDirChangedAction(dir, currDir) =>
        dir shouldBe expected.dir
        assertFileListDir(currDir, expected.currDir)
    }
  }

  def assertFileListDirUpdatedAction(action: Any, expected: FileListDirUpdatedAction)(implicit position: Position): Assertion = {
    inside(action.asInstanceOf[FileListDirUpdatedAction]) {
      case FileListDirUpdatedAction(currDir) =>
        assertFileListDir(currDir, expected.currDir)
    }
  }

  def assertFileListItemCreatedAction(action: Any, expected: FileListItemCreatedAction)(implicit position: Position): Assertion = {
    inside(action.asInstanceOf[FileListItemCreatedAction]) {
      case FileListItemCreatedAction(name, currDir) =>
        name shouldBe expected.name
        assertFileListDir(currDir, expected.currDir)
    }
  }

  def assertFileListDiskSpaceUpdatedAction(action: Any, expected: FileListDiskSpaceUpdatedAction)(implicit position: Position): Assertion = {
    inside(action.asInstanceOf[FileListDiskSpaceUpdatedAction]) {
      case FileListDiskSpaceUpdatedAction(diskSpace) =>
        diskSpace shouldBe expected.diskSpace
    }
  }

  def assertFileListSortAction(action: Any, expected: FileListSortAction)(implicit position: Position): Assertion = {
    inside(action.asInstanceOf[FileListSortAction]) {
      case FileListSortAction(mode) =>
        mode shouldBe expected.mode
    }
  }
}
