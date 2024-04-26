package farjs.filelist

import farjs.filelist.FileListStateSpec.assertFileListState
import farjs.filelist.stack.PanelStackSpec.withContext
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js

class FileListPanelControllerSpec extends TestSpec with TestRendererUtils {

  private val fileListPanelComp = mockUiComponent[FileListPanelProps]("FileListPanel")
  
  private val controller = new FileListPanelController(fileListPanelComp)

  it should "render component and update isActive" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = _ => ()
    val actions = new MockFileListActions
    val state = FileListState()
    state.isActive shouldBe false
    
    var stackState = List[PanelStackItem[FileListState]](
      PanelStackItem[FileListState]("fsPanel".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(state))
    )
    val stack = new PanelStack(isActive = true, stackState, { f =>
      stackState = f(stackState).asInstanceOf[List[PanelStackItem[FileListState]]]
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)

    //when & then
    val renderer = createTestRenderer(
      withContext(<(controller())()(), stack = stack)
    )
    assertTestComponent(renderer.root.children(0), fileListPanelComp) {
      case FileListPanelProps(resDispatch, resActions, resState, _) =>
        resDispatch shouldBe dispatch
        resActions shouldBe actions
        resState shouldBe state
    }

    //then
    inside(stackState.head) { case PanelStackItem(_, resDispatch, resActions, Some(resState)) =>
      resDispatch shouldBe Some(dispatch)
      resActions shouldBe Some(actions)
      assertFileListState(resState, FileListState.copy(state)(isActive = true))
    }
    val updaterMock = mockFunction[js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]], Unit]
    updaterMock.expects(*).never()
    
    //when & then
    TestRenderer.act { () =>
      renderer.update(
        withContext(<(controller())()(), stack = new PanelStack(isActive = true, stackState, updaterMock))
      )
    }
    assertTestComponent(renderer.root.children(0), fileListPanelComp) {
      case FileListPanelProps(resDispatch, resActions, resState, _) =>
        resDispatch shouldBe dispatch
        resActions shouldBe actions
        assertFileListState(resState, FileListState.copy(state)(isActive = true))
    }

    //then
    inside(stackState.head) { case PanelStackItem(_, resDispatch, resActions, Some(resState)) =>
      resDispatch shouldBe Some(dispatch)
      resActions shouldBe Some(actions)
      assertFileListState(resState, FileListState.copy(state)(isActive = true))
    }
  }
}
