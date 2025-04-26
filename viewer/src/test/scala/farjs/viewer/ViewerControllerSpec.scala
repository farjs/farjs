package farjs.viewer

import farjs.file.FileViewHistory.fileViewsHistoryKind
import farjs.file.FileViewHistorySpec.assertFileViewHistory
import farjs.file.{Encoding, FileViewHistory, FileViewHistoryParams}
import farjs.filelist.history.HistoryProviderSpec.withHistoryProvider
import farjs.filelist.history._
import farjs.filelist.theme.FileListTheme
import farjs.filelist.theme.FileListThemeSpec.withThemeContext
import farjs.ui.WithSizeProps
import farjs.ui.task.{Task, TaskAction}
import farjs.viewer.ViewerController._
import org.scalactic.source.Position
import org.scalatest.{Assertion, OptionValues, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.blessed._
import scommons.react.{raw, _}
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class ViewerControllerSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils with OptionValues {

  ViewerController.withSizeComp = "WithSize".asInstanceOf[ReactClass]
  ViewerController.viewerContent = mockUiComponent("ViewerContent")

  //noinspection TypeAnnotation
  class ViewerFileReader {
    val open = mockFunction[String, js.Promise[Unit]]
    val close = mockFunction[js.Promise[Unit]]

    val fileReader = new MockViewerFileReader(
      openMock = open,
      closeMock = close
    )
  }

  //noinspection TypeAnnotation
  class HistoryMocks {
    val get = mockFunction[HistoryKind, js.Promise[HistoryService]]
    val getOne = mockFunction[String, js.Promise[js.UndefOr[History]]]
    val save = mockFunction[History, js.Promise[Unit]]

    val service = new MockHistoryService(
      getOneMock = getOne,
      saveMock = save
    )
    val provider = new MockHistoryProvider(
      getMock = get
    )
  }

  it should "dispatch error task if failed to open file when mount" in {
    //given
    val fileReader = new ViewerFileReader
    ViewerController.createFileReader = () => fileReader.fileReader
    val inputRef = raw.React.createRef()
    val dispatch = mockFunction[js.Any, Unit]
    val props = ViewerControllerProps(inputRef, dispatch, "test/file", 10, js.undefined)
    val expectedError = new Exception("test error")
    val historyMocks = new HistoryMocks
    historyMocks.get.expects(fileViewsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getOne.expects(FileViewHistory.pathToItem(props.filePath, isEdit = false))
      .returning(js.Promise.resolve[js.UndefOr[History]](js.undefined: js.UndefOr[History]))

    //then
    var openF: Future[_] = null
    fileReader.open.expects(props.filePath).throws(expectedError)
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) { case TaskAction(Task("Opening file", future)) =>
        openF = future
      }
    }

    //when
    val renderer = createTestRenderer(withThemeContext(withHistoryProvider(
      <(ViewerController())(^.plain := props)(), historyMocks.provider
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
    val fileReader = new ViewerFileReader
    ViewerController.createFileReader = () => fileReader.fileReader
    val inputRef = raw.React.createRef()
    val dispatch = mockFunction[js.Any, Unit]
    val setViewport = mockFunction[js.UndefOr[ViewerFileViewport], Unit]
    val props = ViewerControllerProps(inputRef, dispatch, "test/file", 10, js.undefined, setViewport)
    var resViewport: ViewerFileViewport = null
    val historyMocks = new HistoryMocks
    historyMocks.get.expects(fileViewsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getOne.expects(FileViewHistory.pathToItem(props.filePath, isEdit = false))
      .returning(js.Promise.resolve[js.UndefOr[History]](js.undefined: js.UndefOr[History]))

    //then
    fileReader.open.expects(props.filePath).returning(js.Promise.resolve[Unit](()))
    setViewport.expects(*).onCall { viewport: js.UndefOr[ViewerFileViewport] =>
      inside(viewport.toOption) {
        case Some(vp) => resViewport = vp
      }
    }

    //when
    val renderer = createTestRenderer(withThemeContext(withHistoryProvider(
      <(ViewerController())(^.plain := props)(), historyMocks.provider
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
          linesData.toList shouldBe Nil
      }
    }.map { _ =>
      //then
      fileReader.close.expects().returning(js.Promise.resolve[Unit](()))
      
      //when
      TestRenderer.act { () =>
        renderer.unmount()
      }
      Succeeded
    }
  }

  it should "open/close file and use viewport params from history when mount/unmount" in {
    //given
    val fileReader = new ViewerFileReader
    ViewerController.createFileReader = () => fileReader.fileReader
    val inputRef = raw.React.createRef()
    val dispatch = mockFunction[js.Any, Unit]
    val setViewport = mockFunction[js.UndefOr[ViewerFileViewport], Unit]
    val props = ViewerControllerProps(inputRef, dispatch, "test/file", 10, js.undefined, setViewport)
    var resViewport: ViewerFileViewport = null
    val historyMocks = new HistoryMocks
    val history = FileViewHistory(
      path = props.filePath,
      params = FileViewHistoryParams(
        isEdit = false,
        encoding = "test-enc",
        position = 456,
        wrap = true,
        column = 7
      )
    )
    historyMocks.get.expects(fileViewsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getOne.expects(FileViewHistory.pathToItem(props.filePath, isEdit = false))
      .returning(js.Promise.resolve[js.UndefOr[History]](FileViewHistory.toHistory(history)))

    //then
    fileReader.open.expects(props.filePath).returning(js.Promise.resolve[Unit](()))
    setViewport.expects(*).onCall { viewport: js.UndefOr[ViewerFileViewport] =>
      inside(viewport.toOption) {
        case Some(vp) => resViewport = vp
      }
    }

    //when
    val renderer = createTestRenderer(withThemeContext(withHistoryProvider(
      <(ViewerController())(^.plain := props)(), historyMocks.provider
    )))

    //then
    assertViewerController(renderer.root, props)
    eventually {
      inside(resViewport) {
        case ViewerFileViewport(_, encoding, size, width, height, wrap, column, position, linesData) =>
          encoding shouldBe history.params.encoding
          size shouldBe props.size
          width shouldBe 0
          height shouldBe 0
          wrap shouldBe history.params.wrap
          column shouldBe history.params.column
          position shouldBe history.params.position
          linesData.toList shouldBe Nil
      }
    }.flatMap { _ =>
      //when
      TestRenderer.act { () =>
        renderer.update(withThemeContext(withHistoryProvider(
          <(ViewerController())(^.plain := ViewerControllerProps.copy(props)(viewport = resViewport))(), historyMocks.provider
        )))
      }

      //then
      fileReader.close.expects().returning(js.Promise.resolve[Unit](()))
      var saveHistory: History = null
      historyMocks.get.expects(fileViewsHistoryKind)
        .returning(js.Promise.resolve[HistoryService](historyMocks.service))
      historyMocks.save.expects(*).onCall { h: History =>
        saveHistory = h
        js.Promise.resolve[Unit](())
      }

      //when
      TestRenderer.act { () =>
        renderer.unmount()
      }

      //then
      eventually(saveHistory should not be null).map { _ =>
        assertFileViewHistory(FileViewHistory.fromHistory(saveHistory).toOption.value, history)
      }
    }
  }

  it should "render left and right scroll indicators" in {
    //given
    val fileReader = new ViewerFileReader
    ViewerController.createFileReader = () => fileReader.fileReader
    val inputRef = raw.React.createRef()
    val dispatch = mockFunction[js.Any, Unit]
    val props = ViewerControllerProps(inputRef, dispatch, "test/file", 10, js.undefined)
    fileReader.open.expects(props.filePath).returning(js.Promise.resolve[Unit](()))
    val historyMocks = new HistoryMocks
    historyMocks.get.expects(fileViewsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getOne.expects(FileViewHistory.pathToItem(props.filePath, isEdit = false))
      .returning(js.Promise.resolve[js.UndefOr[History]](js.undefined: js.UndefOr[History]))

    val renderer = createTestRenderer(withThemeContext(withHistoryProvider(
      <(ViewerController())(^.plain := props)(), historyMocks.provider
    )))
    
    eventually(assertViewerController(renderer.root, props)).map { _ =>
      val updatedProps = ViewerControllerProps.copy(props)(viewport = ViewerFileViewport(
        fileReader = new MockViewerFileReader,
        encoding = "win",
        size = 123,
        width = 3,
        height = 2,
        column = 1,
        linesData = js.Array(
          ViewerFileLine("test", 4),
          ViewerFileLine("test content", 12)
        )
      ))

      //when
      TestRenderer.act { () =>
        renderer.update(withThemeContext(withHistoryProvider(
          <(ViewerController())(^.plain := updatedProps)(), historyMocks.provider
        )))
      }

      //then
      assertViewerController(renderer.root, updatedProps, scrollIndicators = List(1))
    }
  }

  private def assertViewerController(result: TestInstance,
                                     props: ViewerControllerProps,
                                     scrollIndicators: List[Int] = Nil
                                    )(implicit pos: Position): Assertion = {

    val theme = FileListTheme.defaultTheme

    assertComponents(result.children, List(
      <(withSizeComp)(^.assertPlain[WithSizeProps](inside(_) {
        case WithSizeProps(render) =>
          val width = 3
          val height = 2
          val renderRes = render(width, height)
          val resComp = createTestRenderer(renderRes).root

          assertNativeComponent(resComp,
            <.box(
              ^.rbStyle := contentStyle(theme)
            )(), { children =>
              props.viewport.toOption match {
                case Some(viewport) =>
                  val linesCount = viewport.linesData.size
                  val content = <(viewerContent())(^.assertPlain[ViewerContentProps](inside(_) {
                    case ViewerContentProps(inputRef, resViewport, setViewport, onKeypress) =>
                      inputRef shouldBe props.inputRef
                      
                      resViewport.width shouldBe width
                      resViewport.height shouldBe height
                      
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
