package farjs.viewer.quickview

import farjs.filelist.stack._
import farjs.viewer._
import farjs.viewer.quickview.QuickViewFile._
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class QuickViewFileSpec extends TestSpec with TestRendererUtils {

  QuickViewFile.viewerController = mockUiComponent("ViewerController")

  it should "update viewport when setViewport" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val panelStack = PanelStackProps(
      isRight = false,
      panelInput = js.Dynamic.literal().asInstanceOf[BlessedElement],
      stack = new PanelStack(isActive = true, List(
        PanelStackItem("quickViewComp".asInstanceOf[ReactClass], Some(dispatch), None, None)
      ), null)
    )
    val props = QuickViewFileProps(dispatch, panelStack, "some/file/path", size = 123)
    val renderer = createTestRenderer(<(QuickViewFile())(^.wrapped := props)())
    val viewProps = findComponentProps(renderer.root, viewerController)
    viewProps.viewport shouldBe None

    val viewport = ViewerFileViewport(
      fileReader = new MockViewerFileReader,
      encoding = "uft8",
      size = 123,
      width = 3,
      height = 2,
      column = 1,
      linesData = List(
        "test" -> 4,
        "test content" -> 12
      )
    )
    
    //when
    viewProps.setViewport(Some(viewport))
    
    //then
    findComponentProps(renderer.root, viewerController).viewport shouldBe Some(viewport)
  }

  it should "render component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val panelStack = PanelStackProps(
      isRight = false,
      panelInput = js.Dynamic.literal().asInstanceOf[BlessedElement],
      stack = new PanelStack(isActive = true, List(
        PanelStackItem("quickViewComp".asInstanceOf[ReactClass], Some(dispatch), None, None)
      ), null)
    )
    val props = QuickViewFileProps(dispatch, panelStack, "some/file/path", size = 123)
    
    //when
    val renderer = createTestRenderer(<(QuickViewFile())(^.wrapped := props)())
    
    //then
    assertComponents(renderer.root.children, List(
      <(viewerController())(^.assertWrapped(inside(_) {
        case ViewerControllerProps(inputRef, dispatch, filePath, size, viewport, _) =>
          inputRef.current should be theSameInstanceAs props.panelStack.panelInput
          dispatch shouldBe props.dispatch
          filePath shouldBe props.filePath
          size shouldBe props.size
          viewport shouldBe None
      }))()
    ))
  }
}
