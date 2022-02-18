package farjs.filelist.fs

import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.fs.FSPanel._
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future

class FSPanelSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FSPanel.fileListPanelComp = mockUiComponent("FileListPanel")

  //noinspection TypeAnnotation
  class Actions {
    val openInDefaultApp = mockFunction[String, String, FileListTaskAction]

    val actions = new MockFileListActions(
      openInDefaultAppMock = openInDefaultApp
    )
  }

  it should "return false when onKeypress(unknown key)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState())
    val comp = testRender(<(FSPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(comp, fileListPanelComp)

    //when & then
    panelProps.onKeypress(null, "unknown") shouldBe false
  }

  it should "dispatch action when onKeypress(M-o)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("item 1")))
    ))
    val comp = testRender(<(FSPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(comp, fileListPanelComp)
    val action = FileListTaskAction(FutureTask("Opening item", Future.unit))

    //then
    actions.openInDefaultApp.expects("/sub-dir", "item 1").returning(action)
    dispatch.expects(action)

    //when & then
    panelProps.onKeypress(null, "M-o") shouldBe true

    action.task.future.map(_ => Succeeded)
  }

  it should "render initial component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = FileListPanelProps(dispatch, actions.actions, state)
    
    //when
    val result = testRender(<(FSPanel())(^.wrapped := props)())

    //then
    assertTestComponent(result, fileListPanelComp) {
      case FileListPanelProps(resDispatch, resActions, resState, _) =>
        resDispatch shouldBe dispatch
        resActions shouldBe actions.actions
        resState shouldBe state
    }
  }
}
