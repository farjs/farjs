package scommons.farc.app

import scommons.farc.api.filelist.{FileListDir, FileListItem}
import scommons.farc.ui.filelist.FileListActions._
import scommons.farc.ui.filelist.FileListState
import scommons.react.redux.task.{AbstractTask, FutureTask}
import scommons.react.test.TestSpec

import scala.concurrent.Future

class FarcStateReducerSpec extends TestSpec {

  it should "return initial state" in {
    //when
    val result = FarcStateReducer.reduce(None, "")
    
    //then
    inside(result) {
      case FarcState(currentTask, fileListState) =>
        currentTask shouldBe None
        fileListState shouldBe FileListState()
    }
  }
  
  it should "set currentTask when TaskAction" in {
    //given
    val currTask = mock[AbstractTask]
    val state = FarcState(Some(currTask), FileListState())
    val task = FutureTask("test task", Future.successful(
      (FileListDir("/", isRoot = true), Seq.empty[FileListItem]))
    )
    
    //when
    val result = FarcStateReducer.reduce(Some(state), FileListDirChangeAction(task))
    
    //then
    result.currentTask shouldBe Some(task)
  }
}
