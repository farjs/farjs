package farjs.filelist.popups

import farjs.filelist.FileListActions._
import farjs.filelist.FileListServicesSpec.withServicesContext
import farjs.filelist._
import farjs.filelist.api.FileListDir
import farjs.filelist.history.MockFileListHistoryService
import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.popups.MakeFolderController._
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future

class MakeFolderControllerSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  MakeFolderController.makeFolderPopup = mockUiComponent("MakeFolderPopup")

  //noinspection TypeAnnotation
  class Actions {
    val createDir = mockFunction[Dispatch, String, String, Boolean, FileListDirCreateAction]

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
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currDir = FileListDir("/sub-dir", isRoot = false, items = Seq.empty)
    val state = FileListState(isActive = true, currDir = currDir)
    val props = PopupControllerProps(Some(FileListData(dispatch, actions.actions, state)),
      FileListPopupsState(showMkFolderPopup = true))
    val historyService = new HistoryService
    val renderer = createTestRenderer(withServicesContext(
      <(MakeFolderController())(^.wrapped := props)(), mkDirsHistory = historyService.service
    ))
    val action = FileListDirCreateAction(
      FutureTask("Creating...", Future.successful(()))
    )
    val saveF = Future.unit
    val dir = "test dir"
    val multiple = true

    //then
    actions.createDir.expects(dispatch, currDir.path, dir, multiple).returning(action)
    historyService.save.expects(dir).returning(saveF)
    dispatch.expects(action)
    dispatch.expects(FileListPopupMkFolderAction(show = false))

    //when
    findComponentProps(renderer.root, makeFolderPopup).onOk(dir, multiple)
    
    //then
    for {
      _ <- action.task.future
      _ <- saveF
    } yield {
      inside(findComponentProps(renderer.root, makeFolderPopup)) {
        case MakeFolderPopupProps(resMultiple, _, _) =>
          resMultiple shouldBe multiple
      }
    }
  }

  it should "dispatch FileListPopupMkFolderAction when Cancel action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = PopupControllerProps(Some(FileListData(dispatch, actions.actions, state)),
      FileListPopupsState(showMkFolderPopup = true))
    val historyService = new MockFileListHistoryService
    val comp = testRender(withServicesContext(
      <(MakeFolderController())(^.wrapped := props)(), mkDirsHistory = historyService
    ))
    val popup = findComponentProps(comp, makeFolderPopup)

    //then
    dispatch.expects(FileListPopupMkFolderAction(show = false))

    //when
    popup.onCancel()

    Succeeded
  }

  it should "render popup component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = PopupControllerProps(Some(FileListData(dispatch, actions.actions, state)),
      FileListPopupsState(showMkFolderPopup = true))
    val historyService = new MockFileListHistoryService

    //when
    val result = testRender(withServicesContext(
      <(MakeFolderController())(^.wrapped := props)(), mkDirsHistory = historyService
    ))

    //then
    assertTestComponent(result, makeFolderPopup) {
      case MakeFolderPopupProps(multiple, _, _) =>
        multiple shouldBe false
    }
  }

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = PopupControllerProps(Some(FileListData(dispatch, actions, state)),
      FileListPopupsState())
    val historyService = new MockFileListHistoryService

    //when
    val renderer = createTestRenderer(withServicesContext(
      <(MakeFolderController())(^.wrapped := props)(), mkDirsHistory = historyService
    ))

    //then
    renderer.root.children.toList should be (empty)
  }
}
