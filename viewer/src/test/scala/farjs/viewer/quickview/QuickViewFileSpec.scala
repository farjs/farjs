package farjs.viewer.quickview

import farjs.viewer._
import farjs.viewer.quickview.QuickViewFile._
import scommons.react.ReactClass
import scommons.react.raw.React
import scommons.react.test._

import scala.scalajs.js

class QuickViewFileSpec extends TestSpec with TestRendererUtils {

  QuickViewFile.viewerController = "ViewerController".asInstanceOf[ReactClass]

  it should "update viewport when setViewport" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val inputRef = React.createRef()
    val props = QuickViewFileProps(dispatch, inputRef, isRight = false, "some/file/path", size = 123)
    val renderer = createTestRenderer(<(QuickViewFile())(^.plain := props)())
    val viewProps = inside(findComponents(renderer.root, viewerController)) {
      case List(c) => c.props.asInstanceOf[ViewerControllerProps]
    }
    viewProps.viewport shouldBe js.undefined

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
    viewProps.setViewport(viewport)
    
    //then
    inside(findComponents(renderer.root, viewerController)) {
      case List(c) => c.props.asInstanceOf[ViewerControllerProps].viewport shouldBe viewport
    }
  }

  it should "emit onViewerOpenLeft event when onKeypress(F3)" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val emitMock = mockFunction[String, js.Any, js.Dynamic, Boolean]
    val inputRef = React.createRef()
    inputRef.current = js.Dynamic.literal("emit" -> emitMock)
    val props = QuickViewFileProps(dispatch, inputRef, isRight = true, "some/file/path", size = 123)
    val renderer = createTestRenderer(<(QuickViewFile())(^.plain := props)())
    val viewProps = inside(findComponents(renderer.root, viewerController)) {
      case List(c) => c.props.asInstanceOf[ViewerControllerProps]
    }

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
    val inputRef = React.createRef()
    inputRef.current = js.Dynamic.literal("emit" -> emitMock)
    val props = QuickViewFileProps(dispatch, inputRef, isRight = false, "some/file/path", size = 123)
    val renderer = createTestRenderer(<(QuickViewFile())(^.plain := props)())
    val viewProps = inside(findComponents(renderer.root, viewerController)) {
      case List(c) => c.props.asInstanceOf[ViewerControllerProps]
    }

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
    val inputRef = React.createRef()
    val props = QuickViewFileProps(dispatch, inputRef, isRight = false, "some/file/path", size = 123)
    val renderer = createTestRenderer(<(QuickViewFile())(^.plain := props)())
    val viewProps = inside(findComponents(renderer.root, viewerController)) {
      case List(c) => c.props.asInstanceOf[ViewerControllerProps]
    }

    //when & then
    viewProps.onKeypress("unknown") shouldBe false
  }

  it should "render component" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val inputRef = React.createRef()
    val props = QuickViewFileProps(dispatch, inputRef, isRight = false, "some/file/path", size = 123)
    
    //when
    val renderer = createTestRenderer(<(QuickViewFile())(^.plain := props)())
    
    //then
    assertComponents(renderer.root.children, List(
      <(viewerController)(^.assertPlain[ViewerControllerProps](inside(_) {
        case ViewerControllerProps(resInputRef, dispatch, filePath, size, viewport, _, _) =>
          resInputRef should be theSameInstanceAs inputRef
          dispatch shouldBe props.dispatch
          filePath shouldBe props.filePath
          size shouldBe props.size
          viewport shouldBe js.undefined
      }))()
    ))
  }
}
