package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.FileListActionsSpec.{assertFileListParamsChangedAction, assertFileListSortAction}
import farjs.filelist.FileListPanel._
import farjs.filelist.api.{FileListCapability, FileListDir, FileListItem, MockFileListApi}
import farjs.filelist.sort.{FileListSort, SortMode, SortModesPopupProps}
import farjs.filelist.stack.{PanelStack, PanelStackItem, WithStackSpec}
import farjs.ui.Dispatch
import farjs.ui.task.{Task, TaskAction}
import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.{ReactClass, ReactElement}
import scommons.react.blessed.{BlessedScreen, KeyboardKey}
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class FileListPanelSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FileListPanel.fileListPanelView = "FileListPanelView".asInstanceOf[ReactClass]
  FileListPanel.fileListQuickSearch = "FileListQuickSearch".asInstanceOf[ReactClass]
  FileListPanel.sortModesPopup = "SortModesPopup".asInstanceOf[ReactClass]

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
    val props = FileListPanelProps(dispatch, actions.actions, state, onKeypress: js.Function2[BlessedScreen, String, Boolean])
    val screen = js.Dynamic.literal().asInstanceOf[BlessedScreen]

    val renderer = createTestRenderer(withContext(<(FileListPanel())(^.plain := props)()))

    def check(fullKey: String, expected: FileListSortAction): Unit = {
      //then
      onKeypress.expects(screen, fullKey).returning(false)
      dispatch.expects(*).onCall { action: Any =>
        assertFileListSortAction(action, expected)
        ()
      }

      //when
      inside(findComponents(renderer.root, fileListPanelView)) {
        case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
      }.onKeypress(screen, fullKey)
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
    val renderer = createTestRenderer(withContext(<(FileListPanel())(^.plain := props)()))
    val viewProps = inside(findComponents(renderer.root, fileListPanelView)) {
      case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
    }
    val screen = js.Dynamic.literal().asInstanceOf[BlessedScreen]

    //when
    viewProps.onKeypress(screen, "C-f12")

    //then
    inside(findComponents(renderer.root, sortModesPopup).map(_.props.asInstanceOf[SortModesPopupProps])) {
      case List(SortModesPopupProps(FileListSort(mode, asc), onClose)) =>
        mode shouldBe SortMode.Name
        asc shouldBe true
        
        //when
        onClose()

        //then
        findComponents(renderer.root, sortModesPopup) should be (empty)
    }
  }

  it should "copy parent path into clipboard when onKeypress(C-c)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(FileListItem("..")))
    ))
    val comp = testRender(withContext(<(FileListPanel())(^.plain := props)()))
    val viewProps = inside(findComponents(comp, fileListPanelView)) {
      case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
    }
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
    val comp = testRender(withContext(<(FileListPanel())(^.plain := props)()))
    val viewProps = inside(findComponents(comp, fileListPanelView)) {
      case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
    }
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
    val comp = testRender(withContext(<(FileListPanel())(^.plain := props)()))
    val viewProps = inside(findComponents(comp, fileListPanelView)) {
      case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
    }
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
    val comp = testRender(withContext(<(FileListPanel())(^.plain := props)()))
    val viewProps = inside(findComponents(comp, fileListPanelView)) {
      case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
    }
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
    val comp = testRender(withContext(<(FileListPanel())(^.plain := props)()))
    val viewProps = inside(findComponents(comp, fileListPanelView)) {
      case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
    }
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
      <(FileListPanel())(^.plain := props)(), isRight = isRight
    ))
    val viewProps = inside(findComponents(comp, fileListPanelView)) {
      case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
    }

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
      <(FileListPanel())(^.plain := props)(), isRight = isRight
    ))
    val viewProps = inside(findComponents(comp, fileListPanelView)) {
      case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
    }

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
    val comp = testRender(withContext(<(FileListPanel())(^.plain := props)()))
    val viewProps = inside(findComponents(comp, fileListPanelView)) {
      case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
    }
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
    val comp = testRender(withContext(<(FileListPanel())(^.plain := props)()))
    val viewProps = inside(findComponents(comp, fileListPanelView)) {
      case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
    }

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
    val props = FileListPanelProps(dispatch, actions.actions, FileListState())
    val comp = createTestRenderer(withContext(<(FileListPanel())(^.plain := props)())).root
    val viewProps = inside(findComponents(comp, fileListPanelView)) {
      case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
    }

    //when
    viewProps.onKeypress(null, "C-s")

    //then
    inside(findComponents(comp, fileListQuickSearch).head.props.asInstanceOf[FileListQuickSearchProps]) {
      case FileListQuickSearchProps(text, _) =>
        text shouldBe ""
    }
  }

  it should "hide quick Search box when onKeypress(key.length > 1)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState())
    val comp = createTestRenderer(withContext(<(FileListPanel())(^.plain := props)())).root
    inside(findComponents(comp, fileListPanelView)) {
      case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
    }.onKeypress(null, "C-s")
    findComponents(comp, fileListQuickSearch) should not be empty

    //when
    inside(findComponents(comp, fileListPanelView)) {
      case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
    }.onKeypress(null, "unknown")

    //then
    findComponents(comp, fileListQuickSearch) should be (empty)
  }

  it should "hide quick Search box when panel is deactivated" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState())
    val renderer = createTestRenderer(withContext(<(FileListPanel())(^.plain := props)(), isActive = true))
    inside(findComponents(renderer.root, fileListPanelView)) {
      case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
    }.onKeypress(null, "C-s")
    findComponents(renderer.root, fileListQuickSearch) should not be empty

    //when
    TestRenderer.act { () =>
      renderer.update(withContext(
        <(FileListPanel())(^.plain := FileListPanelProps.copy(props)(
          state = FileListState.copy(props.state)()
        ))()
      ))
    }

    //then
    findComponents(renderer.root, fileListQuickSearch) should be (empty)
  }

  it should "hide quick Search box when onClose" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState())
    val comp = createTestRenderer(withContext(<(FileListPanel())(^.plain := props)(), isActive = true)).root
    inside(findComponents(comp, fileListPanelView)) {
      case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
    }.onKeypress(null, "C-s")
    val searchProps = inside(findComponents(comp, fileListQuickSearch)) {
      case List(c) => c.props.asInstanceOf[FileListQuickSearchProps]
    }

    //when
    searchProps.onClose()

    //then
    findComponents(comp, fileListQuickSearch) should be (empty)
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
    val renderer = createTestRenderer(withContext(<(FileListPanel())(^.plain := props)()))
    inside(findComponents(renderer.root, fileListPanelView)) {
      case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
    }.onKeypress(null, "C-s")
    findComponents(renderer.root, fileListQuickSearch) should not be empty

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
      inside(findComponents(renderer.root, fileListPanelView)) {
        case List(panelView) => panelView.props.asInstanceOf[FileListPanelViewProps]
      }.onKeypress(null, fullKey)

      //then
      findComponents(renderer.root, fileListQuickSearch).head.props.asInstanceOf[FileListQuickSearchProps].text shouldBe text
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
    val result = createTestRenderer(withContext(<(FileListPanel())(^.plain := props)())).root

    //then
    assertFileListPanel(result, props)
  }

  private def withContext(element: ReactElement, isRight: Boolean = false, isActive: Boolean = false): ReactElement = {
    WithStackSpec.withContext(element, isRight, stack = new PanelStack(isActive, js.Array[PanelStackItem[_]](), _ => ()))
  }

  private def assertFileListPanel(result: TestInstance,
                                  props: FileListPanelProps): Assertion = {
    
    assertComponents(result.children, List(
      <(fileListPanelView)(^.assertPlain[FileListPanelViewProps](inside(_) {
        case FileListPanelViewProps(dispatch, actions, state, _) =>
          dispatch shouldBe props.dispatch
          actions shouldBe props.actions
          state shouldBe props.state
      }))()
    ))
  }
}
