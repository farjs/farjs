package farjs.filelist

import farjs.filelist.stack.WithStackSpec.withContext
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import org.scalatest.OptionValues
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js

class FileListPanelControllerSpec extends TestSpec with TestRendererUtils with OptionValues {

  private val fileListPanelComp = mockUiComponent[FileListPanelProps]("FileListPanel")
  
  private val controller = new FileListPanelController(fileListPanelComp)

  it should "render component" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = _ => ()
    val actions = new MockFileListActions
    val state = FileListState()
    val stackState = js.Array[PanelStackItem[_]](
      new PanelStackItem[FileListState]("fsPanel".asInstanceOf[ReactClass], dispatch, actions, state)
    )
    val stack = new PanelStack(isActive = true, stackState, _ => ())

    //when
    val renderer = createTestRenderer(
      withContext(<(controller())()(), stack = stack)
    )
    
    //then
    assertTestComponent(renderer.root.children(0), fileListPanelComp) {
      case FileListPanelProps(resDispatch, resActions, resState, _) =>
        resDispatch shouldBe dispatch
        resActions shouldBe actions
        resState shouldBe state
    }
  }
}
