package farjs.viewer

import farjs.filelist.FileListActions.FileListTaskAction
import farjs.ui.WithSizeProps
import farjs.viewer.ViewerController._
import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.raw.FSConstants
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactRef
import scommons.react.blessed._
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future

class ViewerControllerSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  ViewerController.withSizeComp = mockUiComponent("WithSize")
  ViewerController.viewerContent = mockUiComponent("ViewerContent")

  //noinspection TypeAnnotation
  class FSMocks {
    val openSync = mockFunction[String, Int, Int]

    val fs = new MockFS(
      openSyncMock = openSync
    )
  }

  it should "dispatch error task if failed to open file when mount" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val inputRef = ReactRef.create[BlessedElement]
    val dispatch = mockFunction[Any, Any]
    val props = ViewerControllerProps(inputRef, dispatch, "test/file", 10, None)
    val expectedError = new Exception("test error")

    //then
    var openF: Future[_] = null
    fs.openSync.expects(props.filePath, FSConstants.O_RDONLY).throws(expectedError)
    dispatch.expects(*).onCall { action: Any =>
      inside(action) { case FileListTaskAction(FutureTask("Opening file", future)) =>
        openF = future
      }
    }

    //when
    val renderer = createTestRenderer(<(ViewerController())(^.wrapped := props)())

    //then
    eventually {
      openF should not be null
    }.flatMap(_ => openF.failed).map { ex =>
      ex shouldBe expectedError

      assertViewerController(renderer.root, props)
    }
  }
  
  it should "create viewport if not provided when mount" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val inputRef = ReactRef.create[BlessedElement]
    val dispatch = mockFunction[Any, Any]
    val setViewport = mockFunction[Option[ViewerFileViewport], Unit]
    val props = ViewerControllerProps(inputRef, dispatch, "test/file", 10, None, setViewport)
    val fd = 123
    var resViewport: ViewerFileViewport = null

    //then
    fs.openSync.expects(props.filePath, FSConstants.O_RDONLY).returning(fd)
    setViewport.expects(*).onCall { viewport: Option[ViewerFileViewport] =>
      inside(viewport) {
        case Some(vp) => resViewport = vp
      }
    }

    //when
    val renderer = createTestRenderer(<(ViewerController())(^.wrapped := props)())

    //then
    assertViewerController(renderer.root, props)
    eventually {
      inside(resViewport) {
        case ViewerFileViewport(_, encoding, size, width, height, position, linesData) =>
          encoding shouldBe "utf-8"
          size shouldBe props.size
          width shouldBe 0
          height shouldBe 0
          position shouldBe 0
          linesData shouldBe Nil
      }
    }.map { _ =>
      //cleanup
      TestRenderer.act { () =>
        renderer.unmount()
      }
      Succeeded
    }
  }

  it should "not create viewport if provided when mount" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val inputRef = ReactRef.create[BlessedElement]
    val dispatch = mockFunction[Any, Any]
    val viewport = ViewerFileViewport(
      fileReader = new MockViewerFileReader,
      encoding = "win",
      size = 123,
      width = 1,
      height = 2
    )
    val props = ViewerControllerProps(inputRef, dispatch, "test/file", 10, Some(viewport))

    //then
    fs.openSync.expects(*, *).never()

    //when
    val renderer = createTestRenderer(<(ViewerController())(^.wrapped := props)())

    //then
    assertViewerController(renderer.root, props)
    eventually {
      assertViewerController(renderer.root, props)
    }.map { _ =>
      //cleanup
      TestRenderer.act { () =>
        renderer.unmount()
      }
      Succeeded
    }
  }

  private def assertViewerController(result: TestInstance,
                                     props: ViewerControllerProps
                                    )(implicit pos: Position): Assertion = {

    assertComponents(result.children, List(
      <(withSizeComp())(^.assertPlain[WithSizeProps](inside(_) {
        case WithSizeProps(render) =>
          val width = 60
          val height = 20
          val renderRes = render(width, height)
          val resComp = createTestRenderer(renderRes).root

          assertNativeComponent(resComp,
            <.box(
              ^.rbStyle := contentStyle
            )(
              props.viewport.map { viewport =>
                <(viewerContent())(^.assertWrapped(inside(_) {
                  case ViewerContentProps(inputRef, resViewport, setViewport) =>
                    inputRef shouldBe props.inputRef
                    resViewport shouldBe viewport.copy(
                      width = width,
                      height = height
                    )
                    setViewport should be theSameInstanceAs props.setViewport
                }))()
              }
            )
          )
      }))()
    ))
  }
}
