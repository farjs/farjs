package farjs.app

import farjs.filelist.FileListActions._
import farjs.filelist.api.FileListDir
import farjs.ui.task.{AbstractTask, FutureTask}
import scommons.react.test.TestSpec

import scala.concurrent.Future

class FarjsStateReducerSpec extends TestSpec {

  it should "set currentTask when TaskAction" in {
    //given
    val currTask = mock[AbstractTask]
    val state = FarjsState(Some(currTask))
    val task = FutureTask("test task", Future.successful(FileListDir("/", isRoot = true, Seq.empty)))
    
    //when
    val result = FarjsStateReducer.apply(state, FileListDirChangeAction(task))
    
    //then
    result.currentTask shouldBe Some(task)
  }

  it should "return currentTask when any other action" in {
    //given
    val currTask = mock[AbstractTask]
    val state = FarjsState(Some(currTask))
    val action = "some_test_action"
    
    //when
    val result = FarjsStateReducer.apply(state, action)
    
    //then
    result.currentTask should be theSameInstanceAs state.currentTask
  }
}
