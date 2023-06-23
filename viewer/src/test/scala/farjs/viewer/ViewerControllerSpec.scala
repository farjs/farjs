package farjs.viewer

import farjs.file.FileServicesSpec.withServicesContext
import farjs.file.{Encoding, FileViewHistory, MockFileViewHistoryService}
import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist.theme.FileListTheme
import farjs.filelist.theme.FileListThemeSpec.withThemeContext
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

  //noinspection TypeAnnotation
  class FileViewHistoryService {
    val getOne = mockFunction[String, Boolean, Future[Option[FileViewHistory]]]
    val save = mockFunction[FileViewHistory, Future[Unit]]

    val service = new MockFileViewHistoryService(
      getOneMock = getOne,
      saveMock = save
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
    val historyService = new FileViewHistoryService
    historyService.getOne.expects(props.filePath, false).returning(Future.successful(None))

    //then
    var openF: Future[_] = null
    fs.openSync.expects(props.filePath, FSConstants.O_RDONLY).throws(expectedError)
    dispatch.expects(*).onCall { action: Any =>
      inside(action) { case FileListTaskAction(FutureTask("Opening file", future)) =>
        openF = future
      }
    }

    //when
    val renderer = createTestRenderer(withThemeContext(withServicesContext(
      <(ViewerController())(^.wrapped := props)(), historyService.service
    )))

    //then
    eventually {
      openF should not be null
    }.flatMap(_ => openF.failed).map { ex =>
      ex shouldBe expectedError

      assertViewerController(renderer.root, props)
    }
  }
  
  it should "open/close file and use default viewport params if no history when mount/unmount" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val inputRef = ReactRef.create[BlessedElement]
    val dispatch = mockFunction[Any, Any]
    val setViewport = mockFunction[Option[ViewerFileViewport], Unit]
    val props = ViewerControllerProps(inputRef, dispatch, "test/file", 10, None, setViewport)
    val fd = 123
    var resViewport: ViewerFileViewport = null
    val historyService = new FileViewHistoryService
    historyService.getOne.expects(props.filePath, false).returning(Future.successful(None))

    //then
    fs.openSync.expects(props.filePath, FSConstants.O_RDONLY).returning(fd)
    setViewport.expects(*).onCall { viewport: Option[ViewerFileViewport] =>
      inside(viewport) {
        case Some(vp) => resViewport = vp
      }
    }

    //when
    val renderer = createTestRenderer(withThemeContext(withServicesContext(
      <(ViewerController())(^.wrapped := props)(), historyService.service
    )))

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

  it should "open/close file and use viewport params from history when mount/unmount" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val inputRef = ReactRef.create[BlessedElement]
    val dispatch = mockFunction[Any, Any]
    val setViewport = mockFunction[Option[ViewerFileViewport], Unit]
    val props = ViewerControllerProps(inputRef, dispatch, "test/file", 10, None, setViewport)
    val fd = 123
    var resViewport: ViewerFileViewport = null
    val historyService = new FileViewHistoryService
    val history = FileViewHistory(
      path = props.filePath,
      isEdit = false,
      encoding = "test-enc",
      position = 456,
      wrap = Some(true),
      column = Some(7)
    )
    historyService.getOne.expects(props.filePath, false).returning(Future.successful(Some(history)))

    //then
    fs.openSync.expects(props.filePath, FSConstants.O_RDONLY).returning(fd)
    setViewport.expects(*).onCall { viewport: Option[ViewerFileViewport] =>
      inside(viewport) {
        case Some(vp) => resViewport = vp
      }
    }

    //when
    val renderer = createTestRenderer(withThemeContext(withServicesContext(
      <(ViewerController())(^.wrapped := props)(), historyService.service
    )))

    //then
    assertViewerController(renderer.root, props)
    eventually {
      inside(resViewport) {
        case ViewerFileViewport(_, encoding, size, width, height, wrap, column, position, linesData) =>
          encoding shouldBe history.encoding
          size shouldBe props.size
          width shouldBe 0
          height shouldBe 0
          wrap shouldBe history.wrap.get
          column shouldBe history.column.get
          position shouldBe history.position
          linesData shouldBe Nil
      }
    }.map { _ =>
      //when
      TestRenderer.act { () =>
        renderer.update(withThemeContext(withServicesContext(
          <(ViewerController())(^.wrapped := props.copy(viewport = Some(resViewport)))(), historyService.service
        )))
      }

      //then
      fs.closeSync.expects(fd)
      historyService.save.expects(history).returning(Future.unit)

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
    val historyService = new FileViewHistoryService
    historyService.getOne.expects(props.filePath, false).returning(Future.successful(None))

    val renderer = createTestRenderer(withThemeContext(withServicesContext(
      <(ViewerController())(^.wrapped := props)(), historyService.service
    )))

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
      renderer.update(withThemeContext(withServicesContext(
        <(ViewerController())(^.wrapped := updatedProps)(), historyService.service
      )))
    }

    //then
    assertViewerController(renderer.root, updatedProps, scrollIndicators = List(1))
  }

  private def assertViewerController(result: TestInstance,
                                     props: ViewerControllerProps,
                                     scrollIndicators: List[Int] = Nil
                                    )(implicit pos: Position): Assertion = {

    val theme = FileListTheme.defaultTheme

    assertComponents(result.children, List(
      <(withSizeComp())(^.assertPlain[WithSizeProps](inside(_) {
        case WithSizeProps(render) =>
          val width = 3
          val height = 2
          val renderRes = render(width, height)
          val resComp = createTestRenderer(renderRes).root

          assertNativeComponent(resComp,
            <.box(
              ^.rbStyle := contentStyle(theme)
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
                          ^.rbStyle := scrollStyle(theme),
                          ^.rbWidth := 1,
                          ^.rbHeight := linesCount,
                          ^.content := "<" * linesCount
                        )()
                      ) ++ scrollIndicators.map { lineIdx =>
                        <.text(
                          ^.rbStyle := scrollStyle(theme),
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
