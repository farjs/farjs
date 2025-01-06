package farjs.filelist.popups

import farjs.filelist.FileListActions.FileListParamsChangedAction
import farjs.filelist.FileListActionsSpec.assertFileListParamsChangedAction
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.history.HistoryProviderSpec.withHistoryProvider
import farjs.filelist.history._
import farjs.filelist.popups.SelectController._
import farjs.filelist.{FileListData, FileListState, FileListUiData, MockFileListActions}
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

import scala.scalajs.js

class SelectControllerSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  SelectController.selectPopupComp = mockUiComponent("SelectPopup")

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

  it should "not select .. when onAction" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState(offset = 1, index = 2, FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem.up,
      FileListItem("file1.test"),
      FileListItem("file2.test"),
      FileListItem("file.test3")
    )), selectedNames = js.Set("file.test3"))
    val onClose = mockFunction[Unit]
    val props = FileListUiData(
      showSelectPopup = Some(true),
      data = Some(FileListData(dispatch, actions, state)),
      onClose = onClose
    )
    val historyMocks = new HistoryMocks
    val renderer = createTestRenderer(withHistoryProvider(
      <(SelectController())(^.wrapped := props)(), historyMocks.provider
    ))
    val pattern = "*"

    //then
    var saveHistory: History = null
    historyMocks.get.expects(selectPatternsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.save.expects(*).onCall { h: History =>
      saveHistory = h
      js.Promise.resolve[Unit](())
    }
    dispatch.expects(*).onCall { action: Any =>
      assertFileListParamsChangedAction(action,
        FileListParamsChangedAction(state.offset, state.index,
          js.Set("file1.test", "file2.test", "file.test3")))
      ()
    }
    onClose.expects()

    //when
    findComponentProps(renderer.root, selectPopupComp).onAction(pattern)
    
    //then
    eventually(saveHistory should not be null).map { _ =>
      inside(saveHistory) {
        case History(item, params) =>
          item shouldBe pattern
          params shouldBe js.undefined
      }
    }
  }

  it should "dispatch actions and update state when Select" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState(offset = 1, index = 2, FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem.up,
      FileListItem("file1.test"),
      FileListItem("file2.test"),
      FileListItem("file.test3")
    )), selectedNames = js.Set("file.test3"))
    val onClose = mockFunction[Unit]
    val props = FileListUiData(
      showSelectPopup = Some(true),
      data = Some(FileListData(dispatch, actions, state)),
      onClose = onClose
    )
    val historyMocks = new HistoryMocks
    val renderer = createTestRenderer(withHistoryProvider(
      <(SelectController())(^.wrapped := props)(), historyMocks.provider
    ))
    val pattern = "*.test"

    //then
    var saveHistory: History = null
    historyMocks.get.expects(selectPatternsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.save.expects(*).onCall { h: History =>
      saveHistory = h
      js.Promise.resolve[Unit](())
    }
    dispatch.expects(*).onCall { action: Any =>
      assertFileListParamsChangedAction(action,
        FileListParamsChangedAction(state.offset, state.index,
          js.Set("file1.test", "file2.test", "file.test3")))
      ()
    }
    onClose.expects()

    //when
    findComponentProps(renderer.root, selectPopupComp).onAction(pattern)

    //then
    eventually(saveHistory should not be null).map { _ =>
      inside(saveHistory) {
        case History(item, params) =>
          item shouldBe pattern
          params shouldBe js.undefined
      }
    }
  }

  it should "dispatch actions and update state when Deselect" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState(offset = 1, index = 2, FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem.up,
      FileListItem("file1.test"),
      FileListItem("file2.test"),
      FileListItem("file.test3")
    )), selectedNames = js.Set("file1.test", "file2.test", "file.test3"))
    val onClose = mockFunction[Unit]
    val props = FileListUiData(
      showSelectPopup = Some(false),
      data = Some(FileListData(dispatch, actions, state)),
      onClose = onClose
    )
    val historyMocks = new HistoryMocks
    val renderer = createTestRenderer(withHistoryProvider(
      <(SelectController())(^.wrapped := props)(), historyMocks.provider
    ))
    val pattern = "file1.test;file2.test"

    //then
    var saveHistory: History = null
    historyMocks.get.expects(selectPatternsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.save.expects(*).onCall { h: History =>
      saveHistory = h
      js.Promise.resolve[Unit](())
    }
    dispatch.expects(*).onCall { action: Any =>
      assertFileListParamsChangedAction(action,
        FileListParamsChangedAction(
          offset = state.offset,
          index = state.index,
          selectedNames = js.Set("file.test3")
        )
      )
      ()
    }
    onClose.expects()

    //when
    findComponentProps(renderer.root, selectPopupComp).onAction(pattern)

    //then
    eventually(saveHistory should not be null).map { _ =>
      inside(saveHistory) {
        case History(item, params) =>
          item shouldBe pattern
          params shouldBe js.undefined
      }
    }
  }

  it should "call onClose when Cancel" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState()
    val onClose = mockFunction[Unit]
    val props = FileListUiData(
      showSelectPopup = Some(true),
      data = Some(FileListData(dispatch, actions, state)),
      onClose = onClose
    )
    val comp = testRender(withHistoryProvider(
      <(SelectController())(^.wrapped := props)()
    ))
    val popup = findComponentProps(comp, selectPopupComp)

    //then
    onClose.expects()

    //when
    popup.onCancel()
    
    Succeeded
  }

  it should "render Select popup" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = FileListUiData(
      showSelectPopup = Some(true),
      data = Some(FileListData(dispatch, actions, state))
    )

    //when
    val result = testRender(withHistoryProvider(
      <(SelectController())(^.wrapped := props)()
    ))

    //then
    assertTestComponent(result, selectPopupComp) {
      case SelectPopupProps(showSelect, _, _) =>
        showSelect shouldBe true
    }
  }

  it should "render Deselect popup" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = FileListUiData(
      showSelectPopup = Some(false),
      data = Some(FileListData(dispatch, actions, state))
    )

    //when
    val result = testRender(withHistoryProvider(
      <(SelectController())(^.wrapped := props)()
    ))

    //then
    assertTestComponent(result, selectPopupComp) {
      case SelectPopupProps(showSelect, _, _) =>
        showSelect shouldBe false
    }
  }

  it should "render empty component when showSelectPopup is None" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = FileListUiData(data = Some(FileListData(dispatch, actions, state)))

    //when
    val renderer = createTestRenderer(withHistoryProvider(
      <(SelectController())(^.wrapped := props)()
    ))

    //then
    renderer.root.children.toList should be (empty)
  }

  it should "render empty component when data is None" in {
    //given
    val props = FileListUiData(showSelectPopup = Some(true))

    //when
    val renderer = createTestRenderer(withHistoryProvider(
      <(SelectController())(^.wrapped := props)()
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
