package farjs.filelist.popups

import farjs.filelist.api.FileListDir
import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.popups.SelectController._
import farjs.filelist.{FileListState, MockFileListActions}
import org.scalatest.Succeeded
import scommons.react.test._

class SelectControllerSpec extends TestSpec with TestRendererUtils {

  SelectController.selectPopupComp = mockUiComponent("SelectPopup")

  it should "dispatch actions and update state when onAction" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val currDir = FileListDir("/sub-dir", isRoot = false, items = Seq.empty)
    val state = FileListState(isActive = true, currDir = currDir)
    val props = SelectControllerProps(dispatch, actions, state,
      FileListPopupsState(showSelectPopup = ShowSelect))
    val renderer = createTestRenderer(<(SelectController())(^.wrapped := props)())
    val popup = findComponentProps(renderer.root, selectPopupComp)
    val pattern = "test pattern"

    //then
//    dispatch.expects(FileListPopupSelectAction(SelectHidden))

    //when
    popup.onAction(pattern)

    //then
    val updated = findComponentProps(renderer.root, selectPopupComp)
    updated.pattern shouldBe pattern
  }

  it should "dispatch FileListPopupSelectAction(SelectHidden) when Cancel action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = SelectControllerProps(dispatch, actions, state,
      FileListPopupsState(showSelectPopup = ShowDeselect))
    val comp = testRender(<(SelectController())(^.wrapped := props)())
    val popup = findComponentProps(comp, selectPopupComp)
    val action = FileListPopupSelectAction(SelectHidden)

    //then
    dispatch.expects(action)

    //when
    popup.onCancel()

    Succeeded
  }

  it should "render popup component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = SelectControllerProps(dispatch, actions, state,
      FileListPopupsState(showSelectPopup = ShowSelect))

    //when
    val result = testRender(<(SelectController())(^.wrapped := props)())

    //then
    assertTestComponent(result, selectPopupComp) {
      case SelectPopupProps(pattern, action, _, _) =>
        pattern shouldBe ""
        action shouldBe ShowSelect
    }
  }

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = SelectControllerProps(dispatch, actions, state,
      FileListPopupsState(showSelectPopup = SelectHidden))

    //when
    val renderer = createTestRenderer(<(SelectController())(^.wrapped := props)())

    //then
    renderer.root.children.toList should be (empty)
  }
}
