package scommons.farc.ui.filelist

import org.scalatest.Succeeded
import scommons.farc.api.filelist._
import scommons.farc.ui.filelist.FileListActions._
import scommons.farc.ui.filelist.FileListActionsSpec._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.redux.task.FutureTask

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.JavaScriptException

class FileListActionsSpec extends AsyncTestSpec {

  it should "dispatch FileListDirChangedAction when changeDir(None)" in {
    //given
    val api = mock[FileListApi]
    val actions = new FileListActionsTest(api)
    val dispatch = mockFunction[Any, Any]
    val currDir = FileListDir("/", isRoot = true)
    val files = List(FileListItem("file 1"))
    val isRight = true
    val dir: Option[String] = None

    (api.currDir _).expects().returning(Future.successful(currDir))
    (api.listFiles _).expects().returning(Future.successful(files))
    
    //then
    dispatch.expects(FileListDirChangedAction(isRight, dir, currDir, files))
    
    //when
    val FileListDirChangeAction(FutureTask(msg, future)) =
      actions.changeDir(dispatch, isRight, dir)
    
    //then
    msg shouldBe "Changing Dir"
    future.map(_ => Succeeded)
  }
  
  it should "dispatch FileListDirChangedAction when changeDir(Some(...))" in {
    //given
    val api = mock[FileListApi]
    val actions = new FileListActionsTest(api)
    val dispatch = mockFunction[Any, Any]
    val currDir = FileListDir("/", isRoot = true)
    val files = List(FileListItem("file 1"))
    val isRight = true
    val dir = "test dir"

    (api.changeDir _).expects(dir).returning(Future.successful(currDir))
    (api.listFiles _).expects().returning(Future.successful(files))
    
    //then
    dispatch.expects(FileListDirChangedAction(isRight, Some(dir), currDir, files))
    
    //when
    val FileListDirChangeAction(FutureTask(msg, future)) =
      actions.changeDir(dispatch, isRight, Some(dir))
    
    //then
    msg shouldBe "Changing Dir"
    future.map(_ => Succeeded)
  }
  
  it should "fail when api.changeDir failed" in {
    //given
    val api = mock[FileListApi]
    val errHandler = mockFunction[Throwable, Unit]
    val actions = new FileListActionsTest(api, Some(errHandler))
    val dispatch = mockFunction[Any, Any]
    val e = new Exception("test error")
    val isRight = true
    val dir = "test dir"

    (api.changeDir _).expects(dir).returning(Future.failed(e))
    
    //then
    errHandler.expects(e)
    (api.listFiles _).expects().never()
    dispatch.expects(*).never()
    
    //when
    val FileListDirChangeAction(FutureTask(_, future)) =
      actions.changeDir(dispatch, isRight, Some(dir))
    
    //then
    future.failed.map(_ => Succeeded)
  }
  
  it should "return Nil when api.listFiles failed" in {
    //given
    val api = mock[FileListApi]
    val errHandler = mockFunction[Throwable, Unit]
    val actions = new FileListActionsTest(api, Some(errHandler))
    val dispatch = mockFunction[Any, Any]
    val currDir = FileListDir("/", isRoot = true)
    val e = new Exception("test error")
    val isRight = true
    val dir = "test dir"

    (api.changeDir _).expects(dir).returning(Future.successful(currDir))
    (api.listFiles _).expects().returning(Future.failed(e))
    
    //then
    errHandler.expects(e)
    dispatch.expects(FileListDirChangedAction(isRight, Some(dir), currDir, Nil))
    
    //when
    val FileListDirChangeAction(FutureTask(_, future)) =
      actions.changeDir(dispatch, isRight, Some(dir))
    
    //then
    future.map(_ => Succeeded)
  }
  
  it should "log error when onError" in {
    //given
    val oldLog = g.console.log
    
    val api = mock[FileListApi]
    val actions = new FileListActionsTest(api)
    val jsError = JavaScriptException("test JS error")
    val generalError = new Exception("test general error")
    val logMock = mockFunction[String, Unit]
    val log: js.Function1[String, Unit] = { msg =>
      logMock(msg)
    }
    g.console.log = log
    
    //then
    logMock.expects("test JS error")
    logMock.expects(generalError.toString)

    //when & then
    actions.onError(123)(jsError) shouldBe 123
    actions.onError("456")(generalError) shouldBe "456"

    //cleanup
    g.console.log = oldLog
    Succeeded
  }
}

object FileListActionsSpec {

  private class FileListActionsTest(apiMock: FileListApi,
                                    errHandler: Option[Throwable => Unit] = None) extends FileListActions {

    protected def api: FileListApi = apiMock

    override def onError[T](value: T): Throwable => T = errHandler match {
      case None => super.onError(value)
      case Some(handler) => { e =>
        handler(e)
        value
      }
    }
  }
}
