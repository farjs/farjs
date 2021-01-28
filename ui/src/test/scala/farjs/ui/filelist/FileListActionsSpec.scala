package farjs.ui.filelist

import farjs.api.filelist.{FileListApi, FileListDir, FileListItem}
import farjs.ui.filelist.FileListActions._
import farjs.ui.filelist.FileListActionsSpec._
import org.scalatest.Succeeded
import scommons.nodejs.ChildProcess
import scommons.nodejs.ChildProcess._
import scommons.nodejs.Process.Platform
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.redux.task.FutureTask

import scala.concurrent.Future
import scala.scalajs.js

class FileListActionsSpec extends AsyncTestSpec {

  it should "dispatch FileListOpenInDefaultAppAction on Mac OS" in {
    //given
    val childProcess = mock[ChildProcess]
    val actions = new FileListActionsTest(mock[FileListApi])
    actions.platform = Platform.darwin
    actions.childProcess = childProcess
    val parent = "test dir"
    val item = ".."
    val result = (new js.Object, new js.Object)
    
    //then
    (childProcess.exec _).expects(*, *)
      .onCall { (command, options) =>
      command shouldBe """open ".""""
      assertObject(options.get, new ChildProcessOptions {
        override val cwd = parent
        override val windowsHide = true
      })

      (null, Future.successful(result))
    }
    
    //when
    val FileListOpenInDefaultAppAction(FutureTask(msg, future)) =
      actions.openInDefaultApp(parent, item)
    
    //then
    msg shouldBe "Opening default app"
    future.map { res =>
      res should be theSameInstanceAs result
    }
  }
  
  it should "dispatch FileListOpenInDefaultAppAction on Windows" in {
    //given
    val childProcess = mock[ChildProcess]
    val actions = new FileListActionsTest(mock[FileListApi])
    actions.platform = Platform.win32
    actions.childProcess = childProcess
    val parent = "test dir"
    val item = "file 1"
    val result = (new js.Object, new js.Object)
    
    //then
    (childProcess.exec _).expects(*, *)
      .onCall { (command, options) =>
      command shouldBe s"""start "" "$item""""
      assertObject(options.get, new ChildProcessOptions {
        override val cwd = parent
        override val windowsHide = true
      })

      (null, Future.successful(result))
    }
    
    //when
    val FileListOpenInDefaultAppAction(FutureTask(msg, future)) =
      actions.openInDefaultApp(parent, item)
    
    //then
    msg shouldBe "Opening default app"
    future.map { res =>
      res should be theSameInstanceAs result
    }
  }
  
  it should "dispatch FileListOpenInDefaultAppAction on Linux" in {
    //given
    val childProcess = mock[ChildProcess]
    val actions = new FileListActionsTest(mock[FileListApi])
    actions.platform = Platform.linux
    actions.childProcess = childProcess
    val parent = "test dir"
    val item = "file 1"
    val result = (new js.Object, new js.Object)
    
    //then
    (childProcess.exec _).expects(*, *)
      .onCall { (command, options) =>
      command shouldBe s"""xdg-open "$item""""
      assertObject(options.get, new ChildProcessOptions {
        override val cwd = parent
        override val windowsHide = true
      })

      (null, Future.successful(result))
    }
    
    //when
    val FileListOpenInDefaultAppAction(FutureTask(msg, future)) =
      actions.openInDefaultApp(parent, item)
    
    //then
    msg shouldBe "Opening default app"
    future.map { res =>
      res should be theSameInstanceAs result
    }
  }
  
  it should "dispatch FileListDirChangedAction when changeDir" in {
    //given
    val api = mock[FileListApi]
    val actions = new FileListActionsTest(api)
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
  
  it should "dispatch FileListDirChangedAction when createDir" in {
    //given
    val api = mock[FileListApi]
    val actions = new FileListActionsTest(api)
    val dispatch = mockFunction[Any, Any]
    val currDir = FileListDir("/", isRoot = true, items = List(FileListItem("file 1")))
    val isRight = true
    val parent = "/"
    val dir = "test/dir"
    val multiple = true

    (api.mkDir _).expects(parent, dir, multiple).returning(Future.successful("test"))
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
      actions.deleteItems(dispatch, isRight, dir, items)
    
    //then
    msg shouldBe "Deleting Items"
    future.map(_ => Succeeded)
  }
}

object FileListActionsSpec {

  private class FileListActionsTest(apiMock: FileListApi)
    extends FileListActions {

    protected def api: FileListApi = apiMock
  }
}
