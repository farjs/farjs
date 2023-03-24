package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.FileListPanel._
import farjs.filelist.api.{FileListCapability, FileListDir, FileListItem}
import farjs.filelist.sort.{SortMode, SortModesPopupProps}
import farjs.filelist.stack.PanelStackSpec.withContext
import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.blessed.{BlessedScreen, KeyboardKey}
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class FileListPanelSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FileListPanel.fileListPanelView = mockUiComponent("FileListPanelView")
  FileListPanel.fileListQuickSearch = mockUiComponent("FileListQuickSearch")
  FileListPanel.sortModesPopup = mockUiComponent("SortModesPopup")

  //noinspection TypeAnnotation
  class Actions {
    val changeDir = mockFunction[Dispatch, Option[String], String, FileListDirChangeAction]
    val updateDir = mockFunction[Dispatch, String, FileListDirUpdateAction]

    val actions = new MockFileListActions(
      capabilitiesMock = Set(
        FileListCapability.delete,
        FileListCapability.mkDirs,
        FileListCapability.copyInplace,
        FileListCapability.moveInplace
      ),
      changeDirMock = changeDir,
      updateDirMock = updateDir
    )
  }

  it should "dispatch actions when onKeypress" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onKeypress = mockFunction[BlessedScreen, String, Boolean]
    val actions = new Actions
    val state = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("file 1"),
        FileListItem("dir 1", isDir = true)
      ))
    )
    val props = FileListPanelProps(dispatch, actions.actions, state, onKeypress)
    val screen = js.Dynamic.literal().asInstanceOf[BlessedScreen]

    val renderer = createTestRenderer(withContext(<(FileListPanel())(^.wrapped := props)()))

    def check(fullKey: String, action: Any): Unit = {
      //then
      onKeypress.expects(screen, fullKey).returning(false)
      dispatch.expects(action)

      //when
      findComponentProps(renderer.root, fileListPanelView).onKeypress(screen, fullKey)
    }

    //when & then
    check("C-f3", FileListSortByAction(SortMode.Name))
    check("C-f4", FileListSortByAction(SortMode.Extension))
    check("C-f5", FileListSortByAction(SortMode.ModificationTime))
    check("C-f6", FileListSortByAction(SortMode.Size))
    check("C-f7", FileListSortByAction(SortMode.Unsorted))
    check("C-f8", FileListSortByAction(SortMode.CreationTime))
    check("C-f9", FileListSortByAction(SortMode.AccessTime))

    Succeeded
  }

  it should "show SortModesPopup when onKeypress(C-f12)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("..")))
    ))
    val renderer = createTestRenderer(withContext(<(FileListPanel())(^.wrapped := props)()))
    val viewProps = findComponentProps(renderer.root, fileListPanelView)
    val screen = js.Dynamic.literal().asInstanceOf[BlessedScreen]

    //when
    viewProps.onKeypress(screen, "C-f12")

    //then
    inside(findComponentProps(renderer.root, sortModesPopup)) {
      case SortModesPopupProps(mode, ascending, onClose) =>
        mode shouldBe SortMode.Name
        ascending shouldBe true
        
        //when
        onClose()

        //then
        findProps(renderer.root, sortModesPopup) should be (empty)
    }
  }

  it should "copy parent path into clipboard when onKeypress(C-c)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("..")))
    ))
    val comp = testRender(withContext(<(FileListPanel())(^.wrapped := props)()))
    val viewProps = findComponentProps(comp, fileListPanelView)
    val copyToClipboardMock = mockFunction[String, Boolean]
    val screenMock = literal("copyToClipboard" -> copyToClipboardMock)

    //then
    copyToClipboardMock.expects("/sub-dir")

    //when
    viewProps.onKeypress(screenMock.asInstanceOf[BlessedScreen], "C-c")

    Succeeded
  }

  it should "copy item path into clipboard when onKeypress(C-c)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("item 1")))
    ))
    val comp = testRender(withContext(<(FileListPanel())(^.wrapped := props)()))
    val viewProps = findComponentProps(comp, fileListPanelView)
    val copyToClipboardMock = mockFunction[String, Boolean]
    val screenMock = literal("copyToClipboard" -> copyToClipboardMock)

    //then
    copyToClipboardMock.expects(path.join("/sub-dir", "item 1"))

    //when
    viewProps.onKeypress(screenMock.asInstanceOf[BlessedScreen], "C-c")

    Succeeded
  }

  it should "dispatch action when onKeypress(C-r)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("item 1")))
    ))
    val comp = testRender(withContext(<(FileListPanel())(^.wrapped := props)()))
    val viewProps = findComponentProps(comp, fileListPanelView)
    val updatedDir = FileListDir("/updated/dir", isRoot = false, List(
      FileListItem("file 1")
    ))
    val action = FileListDirUpdateAction(FutureTask("Updating", Future.successful(updatedDir)))

    //then
    actions.updateDir.expects(dispatch, "/sub-dir").returning(action)
    dispatch.expects(action)

    //when
    viewProps.onKeypress(null, "C-r")

    action.task.future.map(_ => Succeeded)
  }

  it should "dispatch action when onKeypress(enter)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("dir 1", isDir = true)))
    ))
    val comp = testRender(withContext(<(FileListPanel())(^.wrapped := props)()))
    val viewProps = findComponentProps(comp, fileListPanelView)
    val changedDir = mock[FileListDir]
    val action = FileListDirChangeAction(
      FutureTask("Changing dir", Future.successful(changedDir))
    )

    //then
    actions.changeDir.expects(dispatch, Some("/sub-dir"), "dir 1").returning(action)
    dispatch.expects(action)

    //when
    viewProps.onKeypress(null, "enter")

    action.task.future.map(_ => Succeeded)
  }

  it should "dispatch action when onKeypress(C-pageup)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("item 1")))
    ))
    val comp = testRender(withContext(<(FileListPanel())(^.wrapped := props)()))
    val viewProps = findComponentProps(comp, fileListPanelView)
    val changedDir = mock[FileListDir]
    val action = FileListDirChangeAction(
      FutureTask("Changing dir", Future.successful(changedDir))
    )

    //then
    actions.changeDir.expects(dispatch, Some("/sub-dir"), "..").returning(action)
    dispatch.expects(action)

    //when
    viewProps.onKeypress(null, "C-pageup")

    action.task.future.map(_ => Succeeded)
  }

  it should "emit keypress(Alt-l) if root dir when onKeypress(C-pageup) in Left panel" in {
    //given
    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)

    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions()
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = true, items = List(FileListItem("item 1")))
    ))
    val isRight = false
    val comp = testRender(withContext(
      <(FileListPanel())(^.wrapped := props)(), isRight = isRight
    ))
    val viewProps = findComponentProps(comp, fileListPanelView)

    //then
    onKey.expects("l", false, true, false)
    dispatch.expects(*).never()

    //when
    viewProps.onKeypress(null, "C-pageup")

    //cleanup
    process.stdin.removeListener("keypress", listener)
    Succeeded
  }

  it should "emit keypress(Alt-r) if root dir when onKeypress(C-pageup) in Right panel" in {
    //given
    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)

    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions()
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = true, items = List(FileListItem("item 1")))
    ))
    val isRight = true
    val comp = testRender(withContext(
      <(FileListPanel())(^.wrapped := props)(), isRight = isRight
    ))
    val viewProps = findComponentProps(comp, fileListPanelView)

    //then
    onKey.expects("r", false, true, false)
    dispatch.expects(*).never()

    //when
    viewProps.onKeypress(null, "C-pageup")

    //cleanup
    process.stdin.removeListener("keypress", listener)
    Succeeded
  }

  it should "dispatch action when onKeypress(C-pagedown)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("dir 1", isDir = true)))
    ))
    val comp = testRender(withContext(<(FileListPanel())(^.wrapped := props)()))
    val viewProps = findComponentProps(comp, fileListPanelView)
    val changedDir = mock[FileListDir]
    val action = FileListDirChangeAction(
      FutureTask("Changing dir", Future.successful(changedDir))
    )

    //then
    actions.changeDir.expects(dispatch, Some("/sub-dir"), "dir 1").returning(action)
    dispatch.expects(action)

    //when
    viewProps.onKeypress(null, "C-pagedown")

    action.task.future.map(_ => Succeeded)
  }

  it should "not dispatch action if file when onKeypress(C-pagedown)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("item 1")))
    ))
    val comp = testRender(withContext(<(FileListPanel())(^.wrapped := props)()))
    val viewProps = findComponentProps(comp, fileListPanelView)

    //then
    dispatch.expects(*).never()

    //when
    viewProps.onKeypress(null, "C-pagedown")

    Succeeded
  }

  it should "show quick Search box when onKeypress(C-s)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(isActive = true))
    val comp = createTestRenderer(withContext(<(FileListPanel())(^.wrapped := props)())).root
    val viewProps = findComponentProps(comp, fileListPanelView)

    //when
    viewProps.onKeypress(null, "C-s")

    //then
    inside(findComponentProps(comp, fileListQuickSearch)) {
      case FileListQuickSearchProps(text, _) =>
        text shouldBe ""
    }
  }

  it should "hide quick Search box when onKeypress(key.length > 1)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(isActive = true))
    val comp = createTestRenderer(withContext(<(FileListPanel())(^.wrapped := props)())).root
    findComponentProps(comp, fileListPanelView).onKeypress(null, "C-s")
    findProps(comp, fileListQuickSearch) should not be empty

    //when
    findComponentProps(comp, fileListPanelView).onKeypress(null, "unknown")

    //then
    findProps(comp, fileListQuickSearch) should be (empty)
  }

  it should "hide quick Search box when panel is deactivated" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(isActive = true))
    val renderer = createTestRenderer(withContext(<(FileListPanel())(^.wrapped := props)()))
    findComponentProps(renderer.root, fileListPanelView).onKeypress(null, "C-s")
    findProps(renderer.root, fileListQuickSearch) should not be empty

    //when
    TestRenderer.act { () =>
      renderer.update(withContext(
        <(FileListPanel())(^.wrapped := props.copy(
          state = props.state.copy(isActive = false)
        ))()
      ))
    }

    //then
    findProps(renderer.root, fileListQuickSearch) should be (empty)
  }

  it should "hide quick Search box when onClose" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(isActive = true))
    val comp = createTestRenderer(withContext(<(FileListPanel())(^.wrapped := props)())).root
    findComponentProps(comp, fileListPanelView).onKeypress(null, "C-s")
    val searchProps = findComponentProps(comp, fileListQuickSearch)

    //when
    searchProps.onClose()

    //then
    findProps(comp, fileListQuickSearch) should be (empty)
  }

  it should "dispatch actions when quick Search" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("aB 1"),
        FileListItem("aBc1"),
        FileListItem("aBc 2"),
        FileListItem("aBc+3"),
        FileListItem("aBc-4")
      ))
    )
    val props = FileListPanelProps(dispatch, actions.actions, state)
    val renderer = createTestRenderer(withContext(<(FileListPanel())(^.wrapped := props)()))
    findComponentProps(renderer.root, fileListPanelView).onKeypress(null, "C-s")
    findProps(renderer.root, fileListQuickSearch) should not be empty

    def check(fullKey: String, index: Int, text: String, dispatchAction: Boolean)
             (implicit pos: Position): Unit = {
      
      if (dispatchAction) {
        //then
        dispatch.expects(FileListParamsChangedAction(
          offset = 0,
          index = index,
          selectedNames = props.state.selectedNames
        ))
      }

      //when
      findComponentProps(renderer.root, fileListPanelView).onKeypress(null, fullKey)

      //then
      findComponentProps(renderer.root, fileListQuickSearch).text shouldBe text
    }

    //when & then
    check("a", index = 1, text = "a", dispatchAction = true)
    check("S-b", index = 1, text = "aB", dispatchAction = true)
    check("c", index = 2, text = "aBc", dispatchAction = true)
    check("backspace", index = 2, text = "aB", dispatchAction = false)
    check("d", index = 2, text = "aB", dispatchAction = false)
    check("c", index = 2, text = "aBc", dispatchAction = true)
    check("space", index = 3, text = "aBc ", dispatchAction = true)
    check("backspace", index = 3, text = "aBc", dispatchAction = false)
    check("1", index = 2, text = "aBc1", dispatchAction = true)
    check("backspace", index = 2, text = "aBc", dispatchAction = false)
    check("+", index = 4, text = "aBc+", dispatchAction = true)
    check("backspace", index = 4, text = "aBc", dispatchAction = false)
    check("-", index = 5, text = "aBc-", dispatchAction = true)
    check("4", index = 5, text = "aBc-4", dispatchAction = true)

    Succeeded
  }

  it should "render initial component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState())

    //when
    val result = createTestRenderer(withContext(<(FileListPanel())(^.wrapped := props)())).root

    //then
    assertFileListPanel(result, props)
  }

  private def assertFileListPanel(result: TestInstance,
                                  props: FileListPanelProps): Assertion = {
    
    assertComponents(result.children, List(
      <(fileListPanelView())(^.assertWrapped(inside(_) {
        case FileListPanelViewProps(dispatch, actions, state, _) =>
          dispatch shouldBe props.dispatch
          actions shouldBe props.actions
          state shouldBe props.state
      }))()
    ))
  }
}
