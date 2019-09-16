package farclone.ui.filelist

import farclone.api.filelist._
import farclone.ui.filelist.FileListActions._
import farclone.ui.filelist.FileListActionsSpec._
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

    (api.readDir _).expects(parent, dir).returning(Future.successful(currDir))
    
    //then
    dispatch.expects(FileListDirChangedAction(isRight, dir, currDir))
    
    //when
    val FileListDirChangeAction(FutureTask(msg, future)) =
      actions.changeDir(dispatch, isRight, parent, dir)
    
    //then
    msg shouldBe "Changing Dir"
    future.map(_ => Succeeded)
  }
}

object FileListActionsSpec {

  private class FileListActionsTest(apiMock: FileListApi)
    extends FileListActions {

    protected def api: FileListApi = apiMock
  }
}
