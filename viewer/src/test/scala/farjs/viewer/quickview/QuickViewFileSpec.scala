package farjs.viewer.quickview

import farjs.filelist.stack._
import farjs.viewer._
import farjs.viewer.quickview.QuickViewFile._
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class QuickViewFileSpec extends TestSpec with TestRendererUtils {

  QuickViewFile.viewerController = mockUiComponent("ViewerController")

  it should "update viewport when setViewport" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val panelStack = WithStackProps(
      isRight = false,
      panelInput = js.Dynamic.literal().asInstanceOf[BlessedElement],
      stack = new PanelStack(isActive = true, js.Array(
        PanelStackItem("quickViewComp".asInstanceOf[ReactClass], dispatch)
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
      linesData = js.Array(
        ViewerFileLine("test", 4),
        ViewerFileLine("test content", 12)
      )
    )
    
    //when
    viewProps.setViewport(Some(viewport))
    
    //then
    findComponentProps(renderer.root, viewerController).viewport shouldBe Some(viewport)
  }

  it should "emit onViewerOpenLeft event when onKeypress(F3)" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val emitMock = mockFunction[String, js.Any, js.Dynamic, Boolean]
    val panelStack = WithStackProps(
      isRight = true,
      panelInput = js.Dynamic.literal("emit" -> emitMock).asInstanceOf[BlessedElement],
      stack = new PanelStack(isActive = true, js.Array(
        PanelStackItem("quickViewComp".asInstanceOf[ReactClass], dispatch)
      ), null)
    )
    val props = QuickViewFileProps(dispatch, panelStack, "some/file/path", size = 123)
    val renderer = createTestRenderer(<(QuickViewFile())(^.wrapped := props)())
    val viewProps = findComponentProps(renderer.root, viewerController)

    //then
    emitMock.expects("keypress", *, *).onCall { (_, _, key) =>
      key.name shouldBe ""
      key.full shouldBe "onViewerOpenLeft"
      false
    }

    //when & then
    viewProps.onKeypress("f3") shouldBe true
  }

  it should "emit onViewerOpenRight event when onKeypress(F3)" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val emitMock = mockFunction[String, js.Any, js.Dynamic, Boolean]
    val panelStack = WithStackProps(
      isRight = false,
      panelInput = js.Dynamic.literal("emit" -> emitMock).asInstanceOf[BlessedElement],
      stack = new PanelStack(isActive = true, js.Array(
        PanelStackItem("quickViewComp".asInstanceOf[ReactClass], dispatch)
      ), null)
    )
    val props = QuickViewFileProps(dispatch, panelStack, "some/file/path", size = 123)
    val renderer = createTestRenderer(<(QuickViewFile())(^.wrapped := props)())
    val viewProps = findComponentProps(renderer.root, viewerController)

    //then
    emitMock.expects("keypress", *, *).onCall { (_, _, key) =>
      key.name shouldBe ""
      key.full shouldBe "onViewerOpenRight"
      false
    }

    //when & then
    viewProps.onKeypress("f3") shouldBe true
  }

  it should "return false if unknown key when onKeypress" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val panelStack = WithStackProps(
      isRight = false,
      panelInput = js.Dynamic.literal().asInstanceOf[BlessedElement],
      stack = new PanelStack(isActive = true, js.Array(
        PanelStackItem("quickViewComp".asInstanceOf[ReactClass], dispatch)
      ), null)
    )
    val props = QuickViewFileProps(dispatch, panelStack, "some/file/path", size = 123)
    val renderer = createTestRenderer(<(QuickViewFile())(^.wrapped := props)())
    val viewProps = findComponentProps(renderer.root, viewerController)

    //when & then
    viewProps.onKeypress("unknown") shouldBe false
  }

  it should "render component" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val panelStack = WithStackProps(
      isRight = false,
      panelInput = js.Dynamic.literal().asInstanceOf[BlessedElement],
      stack = new PanelStack(isActive = true, js.Array(
        PanelStackItem("quickViewComp".asInstanceOf[ReactClass], dispatch)
      ), null)
    )
    val props = QuickViewFileProps(dispatch, panelStack, "some/file/path", size = 123)
    
    //when
    val renderer = createTestRenderer(<(QuickViewFile())(^.wrapped := props)())
    
    //then
    assertComponents(renderer.root.children, List(
      <(viewerController())(^.assertWrapped(inside(_) {
        case ViewerControllerProps(inputRef, dispatch, filePath, size, viewport, _, _) =>
          inputRef.current should be theSameInstanceAs props.panelStack.panelInput
          dispatch shouldBe props.dispatch
          filePath shouldBe props.filePath
          size shouldBe props.size
          viewport shouldBe None
      }))()
    ))
  }
}
