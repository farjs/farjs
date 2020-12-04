package farjs.ui.filelist

import farjs.api.filelist.{FileListApi, FileListDir, FileListItem}
import farjs.ui.filelist.FileListActions._
import farjs.ui.filelist.FileListActionsSpec._
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.redux.task.FutureTask

import scala.concurrent.Future

class FileListActionsSpec extends AsyncTestSpec {

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
