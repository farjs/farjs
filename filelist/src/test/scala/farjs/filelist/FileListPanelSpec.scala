package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.FileListActionsSpec.{assertFileListParamsChangedAction, assertFileListSortAction}
import farjs.filelist.FileListPanel._
import farjs.filelist.api.{FileListCapability, FileListDir, FileListItem, MockFileListApi}
import farjs.filelist.sort.{FileListSort, SortMode, SortModesPopupProps}
import farjs.filelist.stack.PanelStackCompSpec.withContext
import farjs.ui.Dispatch
import farjs.ui.task.{Task, TaskAction}
import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.blessed.{BlessedScreen, KeyboardKey}
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
    val changeDir = mockFunction[Dispatch, String, String, TaskAction]
    val updateDir = mockFunction[Dispatch, String, TaskAction]

    val actions = new MockFileListActions(
      new MockFileListApi(capabilitiesMock = js.Set(
        FileListCapability.delete,
        FileListCapability.mkDirs,
        FileListCapability.copyInplace,
        FileListCapability.moveInplace
      )),
      changeDirMock = changeDir,
      updateDirMock = updateDir
    )
  }

  it should "dispatch actions when onKeypress" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onKeypress = mockFunction[BlessedScreen, String, Boolean]
    val actions = new Actions
    val state = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("file 1"),
        FileListItem("dir 1", isDir = true)
      ))
    )
    val props = FileListPanelProps(dispatch, actions.actions, state, onKeypress)
    val screen = js.Dynamic.literal().asInstanceOf[BlessedScreen]

    val renderer = createTestRenderer(withContext(<(FileListPanel())(^.wrapped := props)()))

    def check(fullKey: String, expected: FileListSortAction): Unit = {
      //then
      onKeypress.expects(screen, fullKey).returning(false)
      dispatch.expects(*).onCall { action: Any =>
        assertFileListSortAction(action, expected)
        ()
      }

      //when
      findComponentProps(renderer.root, fileListPanelView).onKeypress(screen, fullKey)
    }

    //when & then
    check("C-f3", FileListSortAction(SortMode.Name))
    check("C-f4", FileListSortAction(SortMode.Extension))
    check("C-f5", FileListSortAction(SortMode.ModificationTime))
    check("C-f6", FileListSortAction(SortMode.Size))
    check("C-f7", FileListSortAction(SortMode.Unsorted))
    check("C-f8", FileListSortAction(SortMode.CreationTime))
    check("C-f9", FileListSortAction(SortMode.AccessTime))

    Succeeded
  }

  it should "show SortModesPopup when onKeypress(C-f12)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(FileListItem("..")))
    ))
    val renderer = createTestRenderer(withContext(<(FileListPanel())(^.wrapped := props)()))
    val viewProps = findComponentProps(renderer.root, fileListPanelView)
    val screen = js.Dynamic.literal().asInstanceOf[BlessedScreen]

    //when
    viewProps.onKeypress(screen, "C-f12")

    //then
    inside(findComponentProps(renderer.root, sortModesPopup)) {
      case SortModesPopupProps(FileListSort(mode, asc), onClose) =>
        mode shouldBe SortMode.Name
        asc shouldBe true
        
        //when
        onClose()

        //then
        findProps(renderer.root, sortModesPopup) should be (empty)
    }
  }

  it should "copy parent path into clipboard when onKeypress(C-c)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(FileListItem("..")))
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
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(FileListItem("item 1")))
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
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(FileListItem("item 1")))
    ))
    val comp = testRender(withContext(<(FileListPanel())(^.wrapped := props)()))
    val viewProps = findComponentProps(comp, fileListPanelView)
    val updatedDir = FileListDir("/updated/dir", isRoot = false, js.Array(
      FileListItem("file 1")
    ))
    val action = TaskAction(Task("Updating", Future.successful(updatedDir)))

    //then
    actions.updateDir.expects(*, "/sub-dir").returning(action)
    dispatch.expects(action)

    //when
    viewProps.onKeypress(null, "C-r")

    action.task.result.toFuture.map(_ => Succeeded)
  }

  it should "dispatch action when onKeypress(enter)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(FileListItem("dir 1", isDir = true)))
    ))
    val comp = testRender(withContext(<(FileListPanel())(^.wrapped := props)()))
    val viewProps = findComponentProps(comp, fileListPanelView)
    val changedDir = FileListDir(path = "/test", isRoot = false, js.Array())
    val action = TaskAction(
      Task("Changing dir", Future.successful(changedDir))
    )

    //then
    actions.changeDir.expects(*, "/sub-dir", "dir 1").returning(action)
    dispatch.expects(action)

    //when
    viewProps.onKeypress(null, "enter")

    action.task.result.toFuture.map(_ => Succeeded)
  }

  it should "dispatch action when onKeypress(C-pageup)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(FileListItem("item 1")))
    ))
    val comp = testRender(withContext(<(FileListPanel())(^.wrapped := props)()))
    val viewProps = findComponentProps(comp, fileListPanelView)
    val changedDir = FileListDir(path = "/test", isRoot = false, js.Array())
    val action = TaskAction(
      Task("Changing dir", Future.successful(changedDir))
    )

    //then
    actions.changeDir.expects(*, "/sub-dir", "..").returning(action)
    dispatch.expects(action)

    //when
    viewProps.onKeypress(null, "C-pageup")

    action.task.result.toFuture.map(_ => Succeeded)
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

    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions()
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = true, items = js.Array(FileListItem("item 1")))
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

    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions()
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = true, items = js.Array(FileListItem("item 1")))
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
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(FileListItem("dir 1", isDir = true)))
    ))
    val comp = testRender(withContext(<(FileListPanel())(^.wrapped := props)()))
    val viewProps = findComponentProps(comp, fileListPanelView)
    val changedDir = FileListDir(path = "/test", isRoot = false, js.Array())
    val action = TaskAction(
      Task("Changing dir", Future.successful(changedDir))
    )

    //then
    actions.changeDir.expects(*, "/sub-dir", "dir 1").returning(action)
    dispatch.expects(action)

    //when
    viewProps.onKeypress(null, "C-pagedown")

    action.task.result.toFuture.map(_ => Succeeded)
  }

  it should "not dispatch action if file when onKeypress(C-pagedown)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(FileListItem("item 1")))
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
    val dispatch = mockFunction[js.Any, Unit]
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
    val dispatch = mockFunction[js.Any, Unit]
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
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(isActive = true))
    val renderer = createTestRenderer(withContext(<(FileListPanel())(^.wrapped := props)()))
    findComponentProps(renderer.root, fileListPanelView).onKeypress(null, "C-s")
    findProps(renderer.root, fileListQuickSearch) should not be empty

    //when
    TestRenderer.act { () =>
      renderer.update(withContext(
        <(FileListPanel())(^.wrapped := props.copy(
          state = FileListState.copy(props.state)(isActive = false)
        ))()
      ))
    }

    //then
    findProps(renderer.root, fileListQuickSearch) should be (empty)
  }

  it should "hide quick Search box when onClose" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
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
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val state = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
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
        dispatch.expects(*).onCall { action: Any =>
          assertFileListParamsChangedAction(action,
            FileListParamsChangedAction(
              offset = 0,
              index = index,
              selectedNames = props.state.selectedNames
            )
          )
          ()
        }
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
    val dispatch = mockFunction[js.Any, Unit]
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
