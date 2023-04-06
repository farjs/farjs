package farjs.viewer

import farjs.filelist.FileListActions.FileListTaskAction
import farjs.text.Encoding
import farjs.ui.WithSizeProps
import farjs.ui.task.FutureTask
import farjs.viewer.ViewerController._
import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.raw.FSConstants
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class ViewerControllerSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  ViewerController.withSizeComp = mockUiComponent("WithSize")
  ViewerController.viewerContent = mockUiComponent("ViewerContent")

  //noinspection TypeAnnotation
  class FSMocks {
    val openSync = mockFunction[String, Int, Int]
    val closeSync = mockFunction[Int, Unit]

    val fs = new MockFS(
      openSyncMock = openSync,
      closeSyncMock = closeSync
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
  
  it should "open/close viewport file when mount/unmount" in {
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
        case ViewerFileViewport(_, encoding, size, width, height, wrap, column, position, linesData) =>
          encoding shouldBe Encoding.platformEncoding
          size shouldBe props.size
          width shouldBe 0
          height shouldBe 0
          wrap shouldBe false
          column shouldBe 0
          position shouldBe 0
          linesData shouldBe Nil
      }
    }.map { _ =>
      //then
      fs.closeSync.expects(fd)
      
      //when
      TestRenderer.act { () =>
        renderer.unmount()
      }
      Succeeded
    }
  }

  it should "render left and right scroll indicators" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val inputRef = ReactRef.create[BlessedElement]
    val dispatch = mockFunction[Any, Any]
    val props = ViewerControllerProps(inputRef, dispatch, "test/file", 10, None)
    fs.openSync.expects(props.filePath, FSConstants.O_RDONLY).returning(123)
    val renderer = createTestRenderer(<(ViewerController())(^.wrapped := props)())

    val updatedProps = props.copy(viewport = Some(ViewerFileViewport(
      fileReader = new MockViewerFileReader,
      encoding = "win",
      size = 123,
      width = 3,
      height = 2,
      column = 1,
      linesData = List(
        "test" -> 4,
        "test content" -> 12
      )
    )))

    //when
    TestRenderer.act { () =>
      renderer.update(<(ViewerController())(^.wrapped := updatedProps)())
    }

    //then
    assertViewerController(renderer.root, updatedProps, scrollIndicators = List(1))
  }

  private def assertViewerController(result: TestInstance,
                                     props: ViewerControllerProps,
                                     scrollIndicators: List[Int] = Nil
                                    )(implicit pos: Position): Assertion = {

    assertComponents(result.children, List(
      <(withSizeComp())(^.assertPlain[WithSizeProps](inside(_) {
        case WithSizeProps(render) =>
          val width = 3
          val height = 2
          val renderRes = render(width, height)
          val resComp = createTestRenderer(renderRes).root

          assertNativeComponent(resComp,
            <.box(
              ^.rbStyle := contentStyle
            )(), { children =>
              props.viewport match {
                case Some(viewport) =>
                  val linesCount = viewport.linesData.size
                  val content = <(viewerContent())(^.assertWrapped(inside(_) {
                    case ViewerContentProps(inputRef, resViewport, setViewport, onKeypress) =>
                      inputRef shouldBe props.inputRef
                      resViewport shouldBe viewport.copy(
                        width = width,
                        height = height
                      )
                      setViewport should be theSameInstanceAs props.setViewport
                      onKeypress should be theSameInstanceAs props.onKeypress
                  }))()

                  assertComponents(js.Array(children: _*),
                    if (viewport.column > 0) {
                      List[ReactElement](
                        content,
                        <.text(
                          ^.rbStyle := scrollStyle,
                          ^.rbWidth := 1,
                          ^.rbHeight := linesCount,
                          ^.content := "<" * linesCount
                        )()
                      ) ++ scrollIndicators.map { lineIdx =>
                        <.text(
                          ^.rbStyle := scrollStyle,
                          ^.rbLeft := width - 1,
                          ^.rbTop := lineIdx,
                          ^.rbWidth := 1,
                          ^.rbHeight := 1,
                          ^.content := ">"
                        )()
                      }
                    }
                    else List[ReactElement](content)
                  )
                case None =>
                  children should be (empty)
              }
            })
      }))()
    ))
  }
}
