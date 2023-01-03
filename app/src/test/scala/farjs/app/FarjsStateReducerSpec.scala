package farjs.app

import farjs.filelist.FileListActions._
import farjs.filelist.FileListsState
import farjs.filelist.api.FileListDir
import farjs.fs.popups.FSPopupsState
import scommons.react.redux.task.{AbstractTask, FutureTask}
import scommons.react.test.TestSpec

import scala.concurrent.Future

class FarjsStateReducerSpec extends TestSpec {

  it should "return initial state" in {
    //when
    val result = FarjsStateReducer.reduce(None, "")
    
    //then
    inside(result) {
      case FarjsState(currentTask, fileListsState, fsPopups) =>
        currentTask shouldBe None
        fileListsState shouldBe FileListsState()
        fsPopups shouldBe FSPopupsState()
    }
  }
  
  it should "set currentTask when TaskAction" in {
    //given
    val currTask = mock[AbstractTask]
    val state = FarjsState(Some(currTask), FileListsState(), FSPopupsState())
    val task = FutureTask("test task", Future.successful(FileListDir("/", isRoot = true, Seq.empty)))
    
    //when
    val result = FarjsStateReducer.reduce(Some(state), FileListDirChangeAction(task))
    
    //then
    result.currentTask shouldBe Some(task)
  }
}
