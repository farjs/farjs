package farjs.filelist.popups

import farjs.filelist.FileListActions.FileListParamsChangedAction
import farjs.filelist.FileListServicesSpec.withServicesContext
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.history.MockFileListHistoryService
import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.popups.SelectController._
import farjs.filelist.{FileListData, FileListState, MockFileListActions}
import scommons.react.test._

import scala.concurrent.Future

class SelectControllerSpec extends TestSpec with TestRendererUtils {

  SelectController.selectPopupComp = mockUiComponent("SelectPopup")

  //noinspection TypeAnnotation
  class HistoryService {
    val save = mockFunction[String, Future[Unit]]

    val service = new MockFileListHistoryService(
      saveMock = save
    )
  }

  it should "not select .. when onAction" in {
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
    val historyService = new HistoryService
    val renderer = createTestRenderer(withServicesContext(
      <(SelectController())(^.wrapped := props)(), selectPatternsHistory = historyService.service
    ))
    val pattern = "*"

    //then
    historyService.save.expects(pattern).returning(Future.unit)
    dispatch.expects(FileListParamsChangedAction(state.offset, state.index,
      Set("file1.test", "file2.test", "file.test3")))
    dispatch.expects(FileListPopupSelectAction(SelectHidden))

    //when
    findComponentProps(renderer.root, selectPopupComp).onAction(pattern)
  }

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
    val historyService = new HistoryService
    val renderer = createTestRenderer(withServicesContext(
      <(SelectController())(^.wrapped := props)(), selectPatternsHistory = historyService.service
    ))
    val pattern = "*.test"

    //then
    historyService.save.expects(pattern).returning(Future.unit)
    dispatch.expects(FileListParamsChangedAction(state.offset, state.index,
      Set("file1.test", "file2.test", "file.test3")))
    dispatch.expects(FileListPopupSelectAction(SelectHidden))

    //when
    findComponentProps(renderer.root, selectPopupComp).onAction(pattern)
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
    val historyService = new HistoryService
    val renderer = createTestRenderer(withServicesContext(
      <(SelectController())(^.wrapped := props)(), selectPatternsHistory = historyService.service
    ))
    val pattern = "file1.test;file2.test"

    //then
    historyService.save.expects(pattern).returning(Future.unit)
    dispatch.expects(FileListParamsChangedAction(
      offset = state.offset,
      index = state.index,
      selectedNames = Set("file.test3")
    ))
    dispatch.expects(FileListPopupSelectAction(SelectHidden))

    //when
    findComponentProps(renderer.root, selectPopupComp).onAction(pattern)
  }

  it should "dispatch FileListPopupSelectAction(SelectHidden) when Cancel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = PopupControllerProps(Some(FileListData(dispatch, actions, state)),
      FileListPopupsState(showSelectPopup = ShowDeselect))
    val historyService = new MockFileListHistoryService
    val comp = testRender(withServicesContext(
      <(SelectController())(^.wrapped := props)(), selectPatternsHistory = historyService
    ))
    val popup = findComponentProps(comp, selectPopupComp)
    val action = FileListPopupSelectAction(SelectHidden)

    //then
    dispatch.expects(action)

    //when
    popup.onCancel()
  }

  it should "render Select popup" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = PopupControllerProps(Some(FileListData(dispatch, actions, state)),
      FileListPopupsState(showSelectPopup = ShowSelect))
    val historyService = new MockFileListHistoryService

    //when
    val result = testRender(withServicesContext(
      <(SelectController())(^.wrapped := props)(), selectPatternsHistory = historyService
    ))

    //then
    assertTestComponent(result, selectPopupComp) {
      case SelectPopupProps(action, _, _) =>
        action shouldBe ShowSelect
    }
  }

  it should "render Deselect popup" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = PopupControllerProps(Some(FileListData(dispatch, actions, state)),
      FileListPopupsState(showSelectPopup = ShowDeselect))
    val historyService = new MockFileListHistoryService

    //when
    val result = testRender(withServicesContext(
      <(SelectController())(^.wrapped := props)(), selectPatternsHistory = historyService
    ))

    //then
    assertTestComponent(result, selectPopupComp) {
      case SelectPopupProps(action, _, _) =>
        action shouldBe ShowDeselect
    }
  }

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = PopupControllerProps(Some(FileListData(dispatch, actions, state)),
      FileListPopupsState(showSelectPopup = SelectHidden))
    val historyService = new MockFileListHistoryService

    //when
    val renderer = createTestRenderer(withServicesContext(
      <(SelectController())(^.wrapped := props)(), selectPatternsHistory = historyService
    ))

    //then
    renderer.root.children.toList should be (empty)
  }

  it should "escape special chars when fileMaskToRegex" in {
    //when & then
    fileMaskToRegex("aa()[]{}+-^$!*?bb") shouldBe "^aa\\(\\)\\[\\]\\{\\}\\+\\-\\^\\$!.*?.bb$"
  }

  it should "match against simple file mask" in {
    //when & then
    "file.name".matches(fileMaskToRegex("file.NAME")) shouldBe false
    "file.name".matches(fileMaskToRegex("file.nam")) shouldBe false
    "file.ssame".matches(fileMaskToRegex("file.?ame")) shouldBe false
    "file.ssame".matches(fileMaskToRegex("file.??ame")) shouldBe true
    "file.same".matches(fileMaskToRegex("file.?ame")) shouldBe true
    "file.name".matches(fileMaskToRegex("file.name")) shouldBe true
    "^file$.name".matches(fileMaskToRegex("^file$.name")) shouldBe true
    "file()[]{}+-!.name".matches(fileMaskToRegex("file()[]{}+-!.name")) shouldBe true
    ".name".matches(fileMaskToRegex("*.name")) shouldBe true
    "file.name".matches(fileMaskToRegex("*.name")) shouldBe true
    "file.name".matches(fileMaskToRegex("*.*")) shouldBe true
    "file.na.me".matches(fileMaskToRegex("*.na.*")) shouldBe true
    "file.na.me".matches(fileMaskToRegex("*")) shouldBe true
  }
}
