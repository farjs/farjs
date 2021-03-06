package farjs.filelist

import farjs.filelist.FileListPanel._
import farjs.ui._
import org.scalatest.Assertion
import scommons.react._
import scommons.react.test._

class FileListPanelSpec extends TestSpec with TestRendererUtils {

  FileListPanel.withSizeComp = () => "WithSize".asInstanceOf[ReactClass]
  FileListPanel.fileListPanelView = () => "FileListPanelView".asInstanceOf[ReactClass]

  it should "render component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState())

    //when
    val result = testRender(<(FileListPanel())(^.wrapped := props)())

    //then
    assertFileListPanel(result, props)
  }
  
  private def assertFileListPanel(result: TestInstance,
                                  props: FileListPanelProps): Unit = {
    
    val (width, height) = (25, 15)
    
    def assertComponents(view: TestInstance): Assertion = {

      assertTestComponent(view, fileListPanelView) {
        case FileListPanelViewProps(dispatch, actions, state, rewWidth, resHeight) =>
          dispatch shouldBe props.dispatch
          actions shouldBe props.actions
          state shouldBe props.state
          rewWidth shouldBe width
          resHeight shouldBe height
      }
    }
    
    assertTestComponent(result, withSizeComp) { case WithSizeProps(render) =>
      val result = createTestRenderer(render(width, height)).root
      
      assertComponents(result)
    }
  }
}
