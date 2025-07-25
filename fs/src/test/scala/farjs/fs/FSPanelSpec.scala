package farjs.fs

import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.fs.FSPanel._
import farjs.ui.task.{Task, TaskAction}
import org.scalatest.OptionValues
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js

class FSPanelSpec extends TestSpec with TestRendererUtils with OptionValues {

  FSPanel.fileListPanelComp = "FileListPanel".asInstanceOf[ReactClass]
  FSPanel.fsFreeSpaceComp = mockUiComponent("FSFreeSpace")
  FSPanel.fsService = new FsService().fsService
  FSPanel.fsFoldersHistory = "FSFoldersHistory".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class FsService {
    val openItem = mockFunction[String, String, js.Promise[Unit]]

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
    val comp = testRender(<(FSPanel())(^.plain := props)())
    val panelProps = inside(findComponents(comp, fileListPanelComp)) {
      case List(c) => c.props.asInstanceOf[FileListPanelProps]
    }

    //when & then
    panelProps.onKeypress.toOption.value(null, "unknown") shouldBe false
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
    val comp = testRender(<(FSPanel())(^.plain := props)())
    val panelProps = inside(findComponents(comp, fileListPanelComp)) {
      case List(c) => c.props.asInstanceOf[FileListPanelProps]
    }
    val taskFuture = js.Promise.resolve[Unit](js.undefined: Unit)

    //then
    fsService.openItem.expects("/sub-dir", "item 1").returning(taskFuture)
    var resultAction: Any = null
    dispatch.expects(*).onCall { action: Any =>
      resultAction = action
    }

    //when & then
    panelProps.onKeypress.toOption.value(null, "M-o") shouldBe true

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
    val result = createTestRenderer(<(FSPanel())(^.plain := props)()).root

    //then
    assertComponents(result.children, List(
      <(fileListPanelComp)(^.assertPlain[FileListPanelProps](inside(_) {
        case FileListPanelProps(resDispatch, resActions, resState, _) =>
          resDispatch shouldBe dispatch
          resActions shouldBe actions
          resState shouldBe state
      }))(),

      <(fsFreeSpaceComp())(^.assertPlain[FSFreeSpaceProps](inside(_) {
        case FSFreeSpaceProps(resDispatch, currDir) =>
          resDispatch shouldBe dispatch
          currDir shouldBe props.state.currDir
      }))(),

      <(fsFoldersHistory)(^.assertPlain[FSFoldersHistoryProps](inside(_) {
        case FSFoldersHistoryProps(currDirPath) =>
          currDirPath shouldBe props.state.currDir.path
      }))()
    ))
  }
}
