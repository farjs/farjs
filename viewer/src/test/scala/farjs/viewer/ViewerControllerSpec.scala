package farjs.viewer

import farjs.filelist.FileListActions.FileListTaskAction
import farjs.ui.WithSizeProps
import farjs.viewer.ViewerController._
import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.FS
import scommons.nodejs.raw.FSConstants
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.blessed._
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future

class ViewerControllerSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  ViewerController.withSizeComp = mockUiComponent("WithSize")
  ViewerController.viewerContent = mockUiComponent("ViewerContent")

  //noinspection TypeAnnotation
  class FSMocks {
    val openSyncMock = mockFunction[String, Int, Int]
    val closeSyncMock = mockFunction[Int, Unit]

    val fs = new FS {
      override def openSync(path: String, flags: Int): Int = openSyncMock(path, flags)
      override def closeSync(fd: Int): Unit = closeSyncMock(fd)
    }
  }

  it should "dispatch error task if failed to open file when mount" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val dispatch = mockFunction[Any, Any]
    val props = ViewerControllerProps(dispatch, "test/file", "utf-8")
    val expectedError = new Exception("test error")

    //then
    var openF: Future[_] = null
    fs.openSyncMock.expects(props.filePath, FSConstants.O_RDONLY).throws(expectedError)
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
  
  it should "open/close file when mount/unmount" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val dispatch = mockFunction[Any, Any]
    val props = ViewerControllerProps(dispatch, "test/file", "utf-8")
    val fd = 123

    //then
    fs.openSyncMock.expects(props.filePath, FSConstants.O_RDONLY).returning(fd)

    //when
    val renderer = createTestRenderer(<(ViewerController())(^.wrapped := props)())

    //then
    assertViewerController(renderer.root, props)
    eventually {
      assertViewerController(renderer.root, props, hasContent = true)
    }.map { _ =>
      TestRenderer.act { () =>
        //then
        fs.closeSyncMock.expects(fd)

        //when
        renderer.unmount()
      }
      Succeeded
    }
  }

  it should "not render content when width = 0 or height = 0" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val dispatch = mockFunction[Any, Any]
    val props = ViewerControllerProps(dispatch, "test/file", "utf-8")
    fs.openSyncMock.expects(props.filePath, FSConstants.O_RDONLY).returning(123)

    //when
    val renderer = createTestRenderer(<(ViewerController())(^.wrapped := props)())

    //then
    assertViewerController(renderer.root, props)
    eventually {
      assertViewerController(renderer.root, props, hasContent = true)
    }.map { _ =>
      //when & then
      assertViewerController(renderer.root, props, width = 0)
      assertViewerController(renderer.root, props, height = 0)
    }
  }

  private def assertViewerController(result: TestInstance,
                                     props: ViewerControllerProps,
                                     width: Int = 60,
                                     height: Int = 20,
                                     hasContent: Boolean = false
                                    )(implicit pos: Position): Assertion = {

    assertComponents(result.children, List(
      <(withSizeComp())(^.assertPlain[WithSizeProps](inside(_) {
        case WithSizeProps(render) =>
          val renderRes = render(width, height)
          val resComp = createTestRenderer(renderRes).root

          assertNativeComponent(resComp,
            <.box(
              ^.rbStyle := contentStyle
            )(
              if (hasContent) Some {
                <(viewerContent())(^.assertWrapped(inside(_) {
                  case ViewerContentProps(fileReader, encoding, resWidth, resHeight) =>
                    fileReader should not be null
                    encoding shouldBe props.encoding
                    resWidth shouldBe width
                    resHeight shouldBe height
                }))()
              }
              else None
            )
          )
      }))()
    ))
  }
}
