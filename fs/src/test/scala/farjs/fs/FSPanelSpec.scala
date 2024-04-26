package farjs.fs

import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.fs.FSPanel._
import farjs.ui.task.{Task, TaskAction}
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class FSPanelSpec extends TestSpec with TestRendererUtils {

  FSPanel.fileListPanelComp = mockUiComponent("FileListPanel")
  FSPanel.fsFreeSpaceComp = mockUiComponent("FSFreeSpace")
  FSPanel.fsService = new FsService().fsService
  FSPanel.fsFoldersHistory = mockUiComponent("FSFoldersHistory")

  //noinspection TypeAnnotation
  class FsService {
    val openItem = mockFunction[String, String, Future[Unit]]

    val fsService = new MockFSService(
      openItemMock = openItem
    )
  }

  it should "return false when onKeypress(unknown key)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    FSPanel.fsService = new FsService().fsService
    val props = FileListPanelProps(dispatch, actions, FileListState())
    val comp = testRender(<(FSPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(comp, fileListPanelComp)

    //when & then
    panelProps.onKeypress(null, "unknown") shouldBe false
  }

  it should "dispatch action when onKeypress(M-o)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val fsService = new FsService()
    FSPanel.fsService = fsService.fsService
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(FileListItem("item 1")))
    ))
    val comp = testRender(<(FSPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(comp, fileListPanelComp)
    val taskFuture = Future.unit

    //then
    fsService.openItem.expects("/sub-dir", "item 1").returning(taskFuture)
    var resultAction: Any = null
    dispatch.expects(*).onCall { action: Any =>
      resultAction = action
    }

    //when & then
    panelProps.onKeypress(null, "M-o") shouldBe true

    inside(resultAction.asInstanceOf[TaskAction]) {
      case TaskAction(Task("Opening default app", _)) =>
    }
  }

  it should "render initial component" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = _ => ()
    val actions = new MockFileListActions
    val state = FileListState()
    val props = FileListPanelProps(dispatch, actions, state)
    
    //when
    val result = createTestRenderer(<(FSPanel())(^.wrapped := props)()).root

    //then
    assertComponents(result.children, List(
      <(fileListPanelComp())(^.assertWrapped(inside(_) {
        case FileListPanelProps(resDispatch, resActions, resState, _) =>
          resDispatch shouldBe dispatch
          resActions shouldBe actions
          resState shouldBe state
      }))(),

      <(fsFreeSpaceComp())(^.assertWrapped(inside(_) {
        case FSFreeSpaceProps(resDispatch, currDir) =>
          resDispatch shouldBe dispatch
          currDir shouldBe props.state.currDir
      }))(),

      <(fsFoldersHistory())(^.assertWrapped(inside(_) {
        case FSFoldersHistoryProps(currDirPath) =>
          currDirPath shouldBe props.state.currDir.path
      }))()
    ))
  }
}
