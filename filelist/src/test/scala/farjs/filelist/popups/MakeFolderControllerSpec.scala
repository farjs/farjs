package farjs.filelist.popups

import farjs.filelist.FileListServicesSpec.withServicesContext
import farjs.filelist._
import farjs.filelist.api.FileListDir
import farjs.filelist.history.MockFileListHistoryService
import farjs.filelist.popups.MakeFolderController._
import farjs.ui.Dispatch
import farjs.ui.task.{Task, TaskAction}
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class MakeFolderControllerSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  MakeFolderController.makeFolderPopup = mockUiComponent("MakeFolderPopup")

  //noinspection TypeAnnotation
  class Actions {
    val createDir = mockFunction[Dispatch, String, String, Boolean, TaskAction]

    val actions = new MockFileListActions(
      createDirMock = createDir
    )
  }

  //noinspection TypeAnnotation
  class HistoryService {
    val save = mockFunction[String, Future[Unit]]

    val service = new MockFileListHistoryService(
      saveMock = save
    )
  }

  it should "call api and update state when OK action" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array())
    val state = FileListState(isActive = true, currDir = currDir)
    val onClose = mockFunction[Unit]
    val props = FileListUiData(
      showMkFolderPopup = true,
      data = Some(FileListData(dispatch, actions.actions, state)),
      onClose = onClose
    )
    val historyService = new HistoryService
    val renderer = createTestRenderer(withServicesContext(
      <(MakeFolderController())(^.wrapped := props)(), mkDirsHistory = historyService.service
    ))
    findComponentProps(renderer.root, makeFolderPopup).multiple shouldBe false
    val action = TaskAction(
      Task("Creating...", Future.successful(()))
    )
    val saveF = Future.unit
    val dir = "test dir"
    val multiple = true

    //then
    actions.createDir.expects(*, currDir.path, dir, multiple).returning(action)
    historyService.save.expects(dir).returning(saveF)
    dispatch.expects(action)
    onClose.expects()

    //when
    findComponentProps(renderer.root, makeFolderPopup).onOk(dir, multiple)
    
    //then
    for {
      _ <- action.task.result.toFuture
      _ <- saveF
    } yield {
      inside(findComponentProps(renderer.root, makeFolderPopup)) {
        case MakeFolderPopupProps(resMultiple, _, _) =>
          resMultiple shouldBe multiple
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
      showMkFolderPopup = true,
      data = Some(FileListData(dispatch, actions.actions, state)),
      onClose = onClose
    )
    val historyService = new MockFileListHistoryService
    val comp = testRender(withServicesContext(
      <(MakeFolderController())(^.wrapped := props)(), mkDirsHistory = historyService
    ))
    val popup = findComponentProps(comp, makeFolderPopup)

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
      showMkFolderPopup = true,
      data = Some(FileListData(dispatch, actions.actions, state))
    )
    val historyService = new MockFileListHistoryService

    //when
    val result = testRender(withServicesContext(
      <(MakeFolderController())(^.wrapped := props)(), mkDirsHistory = historyService
    ))

    //then
    assertTestComponent(result, makeFolderPopup) {
      case MakeFolderPopupProps(initialMultiple, _, _) =>
        initialMultiple shouldBe true
    }
  }

  it should "render empty component if showMkFolderPopup=false" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = FileListUiData(data = Some(FileListData(dispatch, actions, state)))
    val historyService = new MockFileListHistoryService

    //when
    val renderer = createTestRenderer(withServicesContext(
      <(MakeFolderController())(^.wrapped := props)(), mkDirsHistory = historyService
    ))

    //then
    renderer.root.children.toList should be (empty)
  }

  it should "render empty component if data is None" in {
    //given
    val props = FileListUiData(showMkFolderPopup = true)
    val historyService = new MockFileListHistoryService

    //when
    val renderer = createTestRenderer(withServicesContext(
      <(MakeFolderController())(^.wrapped := props)(), mkDirsHistory = historyService
    ))

    //then
    renderer.root.children.toList should be (empty)
  }
}
