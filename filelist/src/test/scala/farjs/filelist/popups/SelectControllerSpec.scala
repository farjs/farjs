package farjs.filelist.popups

import farjs.filelist.FileListActions.FileListParamsChangedAction
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.popups.SelectController._
import farjs.filelist.{FileListData, FileListState, MockFileListActions}
import org.scalatest.Succeeded
import scommons.react.test._

class SelectControllerSpec extends TestSpec with TestRendererUtils {

  SelectController.selectPopupComp = mockUiComponent("SelectPopup")

  it should "dispatch actions and update state when Select" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState(offset = 1, index = 2, FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem.up,
      FileListItem("file1.test"),
      FileListItem("file2.test"),
      FileListItem("file.test3")
    )), selectedNames = Set("file.test3"), isActive = true)
    val props = PopupControllerProps(Some(FileListData(dispatch, actions, state)),
      FileListPopupsState(showSelectPopup = ShowSelect))
    val renderer = createTestRenderer(<(SelectController())(^.wrapped := props)())
    val popup = findComponentProps(renderer.root, selectPopupComp)
    val pattern = "*.test"

    //then
    dispatch.expects(FileListParamsChangedAction(
      offset = state.offset,
      index = state.index,
      selectedNames = Set("file1.test", "file2.test", "file.test3")
    ))
    dispatch.expects(FileListPopupSelectAction(SelectHidden))

    //when
    popup.onAction(pattern)

    //then
    val updated = findComponentProps(renderer.root, selectPopupComp)
    updated.pattern shouldBe pattern
  }

  it should "dispatch actions and update state when Deselect" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState(offset = 1, index = 2, FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem.up,
      FileListItem("file1.test"),
      FileListItem("file2.test"),
      FileListItem("file.test3")
    )), selectedNames = Set("file1.test", "file2.test", "file.test3"), isActive = true)
    val props = PopupControllerProps(Some(FileListData(dispatch, actions, state)),
      FileListPopupsState(showSelectPopup = ShowDeselect))
    val renderer = createTestRenderer(<(SelectController())(^.wrapped := props)())
    val popup = findComponentProps(renderer.root, selectPopupComp)
    val pattern = "file1.test;file2.test"

    //then
    dispatch.expects(FileListParamsChangedAction(
      offset = state.offset,
      index = state.index,
      selectedNames = Set("file.test3")
    ))
    dispatch.expects(FileListPopupSelectAction(SelectHidden))

    //when
    popup.onAction(pattern)

    //then
    val updated = findComponentProps(renderer.root, selectPopupComp)
    updated.pattern shouldBe pattern
  }

  it should "dispatch FileListPopupSelectAction(SelectHidden) when Cancel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = PopupControllerProps(Some(FileListData(dispatch, actions, state)),
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
    val props = PopupControllerProps(Some(FileListData(dispatch, actions, state)),
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
    val props = PopupControllerProps(Some(FileListData(dispatch, actions, state)),
      FileListPopupsState(showSelectPopup = SelectHidden))

    //when
    val renderer = createTestRenderer(<(SelectController())(^.wrapped := props)())

    //then
    renderer.root.children.toList should be (empty)
  }

  it should "escape special chars when fileMaskToRegex" in {
    //when & then
    fileMaskToRegex("()[]{}+-!*?") shouldBe "^\\(\\)\\[\\]\\{\\}\\+-!.*?.$"
  }

  it should "match against simple file mask" in {
    //when & then
    "file.name".matches(fileMaskToRegex("file.NAME")) shouldBe false
    "file.name".matches(fileMaskToRegex("file.nam")) shouldBe false
    "file.ssame".matches(fileMaskToRegex("file.?ame")) shouldBe false
    "file.ssame".matches(fileMaskToRegex("file.??ame")) shouldBe true
    "file.same".matches(fileMaskToRegex("file.?ame")) shouldBe true
    "file.name".matches(fileMaskToRegex("file.name")) shouldBe true
    "file()[]{}+-!.name".matches(fileMaskToRegex("file()[]{}+-!.name")) shouldBe true
    ".name".matches(fileMaskToRegex("*.name")) shouldBe true
    "file.name".matches(fileMaskToRegex("*.name")) shouldBe true
    "file.name".matches(fileMaskToRegex("*.*")) shouldBe true
    "file.na.me".matches(fileMaskToRegex("*.na.*")) shouldBe true
    "file.na.me".matches(fileMaskToRegex("*")) shouldBe true
  }
}
