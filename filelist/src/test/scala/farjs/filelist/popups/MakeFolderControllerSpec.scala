package farjs.filelist.popups

import farjs.filelist._
import farjs.filelist.api.FileListDir
import farjs.filelist.history.HistoryProviderSpec.withHistoryProvider
import farjs.filelist.history._
import farjs.filelist.popups.MakeFolderController._
import farjs.ui.Dispatch
import farjs.ui.task.{Task, TaskAction}
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class MakeFolderControllerSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  MakeFolderController.makeFolderPopup = "MakeFolderPopup".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class Actions {
    val createDir = mockFunction[Dispatch, String, String, Boolean, TaskAction]

    val actions = new MockFileListActions(
      createDirMock = createDir
    )
  }

  //noinspection TypeAnnotation
  class HistoryMocks {
    val get = mockFunction[HistoryKind, js.Promise[HistoryService]]
    val save = mockFunction[History, js.Promise[Unit]]

    val service = new MockHistoryService(
      saveMock = save
    )
    val provider = new MockHistoryProvider(
      getMock = get
    )
  }

  it should "call api and update state when OK action" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array())
    val state = FileListState(currDir = currDir)
    val onClose = mockFunction[Unit]
    val props = FileListUiData(
      onClose = onClose,
      data = FileListData(dispatch, actions.actions, state),
      showMkFolderPopup = true
    )
    val historyMocks = new HistoryMocks
    val renderer = createTestRenderer(withHistoryProvider(
      <(MakeFolderController())(^.plain := props)(), historyMocks.provider
    ))
    inside(findComponents(renderer.root, makeFolderPopup)) {
      case List(c) => c.props.asInstanceOf[MakeFolderPopupProps].multiple shouldBe false
    }
    val action = TaskAction(
      Task("Creating...", Future.successful(()))
    )
    val saveF = js.Promise.resolve[Unit](())
    val dir = "test dir"
    val multiple = true

    //then
    var saveHistory: History = null
    actions.createDir.expects(*, currDir.path, dir, multiple).returning(action)
    historyMocks.get.expects(mkDirsHistoryKind).returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.save.expects(*).onCall { h: History =>
      saveHistory = h
      saveF
    }
    dispatch.expects(action)
    onClose.expects()

    //when
    inside(findComponents(renderer.root, makeFolderPopup)) {
      case List(c) => c.props.asInstanceOf[MakeFolderPopupProps].onOk(dir, multiple)
    }
    
    //then
    for {
      _ <- action.task.result.toFuture
      _ <- eventually(saveHistory should not be null)
    } yield {
      inside(saveHistory) {
        case History(item, params) =>
          item shouldBe dir
          params shouldBe js.undefined
      }
      inside(findComponents(renderer.root, makeFolderPopup)) {
        case List(c) =>
          inside(c.props.asInstanceOf[MakeFolderPopupProps]) {
            case MakeFolderPopupProps(resMultiple, _, _) =>
              resMultiple shouldBe multiple
          }
      }
    }
  }

  it should "call onClose when Cancel action" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val state = FileListState()
    val onClose = mockFunction[Unit]
    val props = FileListUiData(
      onClose = onClose,
      data = FileListData(dispatch, actions.actions, state),
      showMkFolderPopup = true
    )
    val historyProvider = new MockHistoryProvider
    val comp = testRender(withHistoryProvider(
      <(MakeFolderController())(^.plain := props)(), historyProvider
    ))
    val popup = inside(findComponents(comp, makeFolderPopup)) {
      case List(c) => c.props.asInstanceOf[MakeFolderPopupProps]
    }

    //then
    onClose.expects()

    //when
    popup.onCancel()

    Succeeded
  }

  it should "render popup component" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val state = FileListState()
    val props = FileListUiData(
      data = FileListData(dispatch, actions.actions, state),
      showMkFolderPopup = true
    )
    val historyProvider = new MockHistoryProvider

    //when
    val result = testRender(withHistoryProvider(
      <(MakeFolderController())(^.plain := props)(), historyProvider
    ))

    //then
    assertNativeComponent(result, <(makeFolderPopup)(^.assertPlain[MakeFolderPopupProps](inside(_) {
      case MakeFolderPopupProps(initialMultiple, _, _) =>
        initialMultiple shouldBe true
    }))())
  }

  it should "render empty component if showMkFolderPopup=false" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = FileListUiData(data = FileListData(dispatch, actions, state))
    val historyProvider = new MockHistoryProvider

    //when
    val renderer = createTestRenderer(withHistoryProvider(
      <(MakeFolderController())(^.plain := props)(), historyProvider
    ))

    //then
    renderer.root.children.toList should be (empty)
  }

  it should "render empty component if data is None" in {
    //given
    val props = FileListUiData(showMkFolderPopup = true)
    val historyProvider = new MockHistoryProvider

    //when
    val renderer = createTestRenderer(withHistoryProvider(
      <(MakeFolderController())(^.plain := props)(), historyProvider
    ))

    //then
    renderer.root.children.toList should be (empty)
  }
}
