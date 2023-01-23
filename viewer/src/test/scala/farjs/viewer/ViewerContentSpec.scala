package farjs.viewer

import farjs.filelist.FileListActions.FileListTaskAction
import farjs.ui.WithSizeProps
import farjs.viewer.ViewerContent._
import org.scalatest.Assertion
import scommons.nodejs.raw.FSConstants
import scommons.nodejs.test.AsyncTestSpec
import scommons.nodejs.{Buffer, FS}
import scommons.react.blessed._
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

class ViewerContentSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  ViewerContent.withSizeComp = mockUiComponent("WithSize")

  //noinspection TypeAnnotation
  class FSMocks {
    val openSyncMock = mockFunction[String, Int, Int]
    val readMock = mockFunction[Int, Uint8Array, Int, Int, js.UndefOr[Double], Future[Int]]
    val closeSyncMock = mockFunction[Int, Unit]

    val fs = new FS {
      override def openSync(path: String, flags: Int): Int = openSyncMock(path, flags)
      override def read(fd: Int,
                        buffer: Uint8Array,
                        offset: Int,
                        length: Int,
                        position: js.UndefOr[Double] = js.undefined): Future[Int] = {

        readMock(fd, buffer, offset, length, position)
      }
      override def closeSync(fd: Int): Unit = closeSyncMock(fd)
    }
  }

  it should "dispatch error task if failed to read file" in {
    //given
    val fs = new FSMocks
    ViewerContent.fs = fs.fs
    val dispatch = mockFunction[Any, Any]
    val props = ViewerContentProps(dispatch, "test/file", "utf-8")
    val expectedError = new Exception("test error")

    //then
    var readF: Future[_] = null
    fs.openSyncMock.expects(props.filePath, FSConstants.O_RDONLY).throws(expectedError)
    dispatch.expects(*).onCall { action: Any =>
      inside(action) { case FileListTaskAction(FutureTask("Reading File", future)) =>
        readF = future
      }
    }

    //when
    val renderer = createTestRenderer(<(ViewerContent())(^.wrapped := props)())

    //then
    eventually {
      readF should not be null
    }.flatMap(_ => readF.failed).map { ex =>
      ex shouldBe expectedError

      assertViewerContent(renderer.root, props, expectedContent = "")
    }
  }
  
  it should "render initial component" in {
    //given
    val fs = new FSMocks
    ViewerContent.fs = fs.fs
    val dispatch = mockFunction[Any, Any]
    val props = ViewerContentProps(dispatch, "test/file", "utf-8")
    val fd = 123
    val expectedContent = "test \nfile content\n"
    var readF: Future[Int] = null
    fs.openSyncMock.expects(props.filePath, FSConstants.O_RDONLY).returning(fd)
    fs.readMock.expects(fd, *, 0, 64 * 1024, *).onCall { (_, buff, _, _, position) =>
      position shouldBe 0.0
      val bytesWritten =
        buff.asInstanceOf[Buffer].write(expectedContent, 0, expectedContent.length, encoding = "utf-8")
      readF = Future.successful(bytesWritten)
      readF
    }
    fs.closeSyncMock.expects(fd)

    //when
    val renderer = createTestRenderer(<(ViewerContent())(^.wrapped := props)())

    //then
    assertViewerContent(renderer.root, props, expectedContent = "")
    eventually {
      readF should not be null
    }.flatMap(_ => readF).map { _ =>
      assertViewerContent(renderer.root, props, expectedContent)
    }
  }
  
  private def assertViewerContent(result: TestInstance,
                                  props: ViewerContentProps,
                                  expectedContent: String): Assertion = {


    assertComponents(result.children, List(
      <(withSizeComp())(^.assertPlain[WithSizeProps](inside(_) {
        case WithSizeProps(render) =>
          val renderRes = render(60, 20)
          val resComp = createTestRenderer(renderRes).root

          assertNativeComponent(resComp,
            <.box(
              ^.rbStyle := contentStyle
            )(
              <.text(
                ^.rbStyle := contentStyle,
                ^.content := expectedContent
              )()
            )
          )
      }))()
    ))
  }
}
