package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.FileListPanel._
import farjs.filelist.FileListPanelSpec._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.popups.FileListPopupsActions._
import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.path
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.blessed.BlessedScreen
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

class FileListPanelSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FileListPanel.fileListPanelView = () => "FileListPanelView".asInstanceOf[ReactClass]
  FileListPanel.fileListQuickSearch = () => "FileListQuickSearch".asInstanceOf[ReactClass]

  it should "dispatch popups actions when F-keys" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val state = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("file 1"),
        FileListItem("dir 1", isDir = true)
      ))
    )
    val props = FileListPanelProps(dispatch, actions, state)

    val renderer = createTestRenderer(<(FileListPanel())(^.wrapped := props)())

    def check(fullKey: String,
              action: Any,
              index: Int = 0,
              selectedNames: Set[String] = Set.empty,
              never: Boolean = false): Unit = {
      //given
      renderer.update(<(FileListPanel())(^.wrapped := props.copy(
        state = props.state.copy(index = index, selectedNames = selectedNames)
      ))())

      //then
      if (never) dispatch.expects(action).never()
      else dispatch.expects(action)

      //when
      findComponentProps(renderer.root, fileListPanelView).onKeypress(null, fullKey)
    }

    //when & then
    check("f1", FileListPopupHelpAction(show = true))

    //when & then
    check("f3", FileListPopupViewItemsAction(show = true), never = true)
    check("f3", FileListPopupViewItemsAction(show = true), index = 1, never = true)
    check("f3", FileListPopupViewItemsAction(show = true), index = 1, selectedNames = Set("file 1"))
    check("f3", FileListPopupViewItemsAction(show = true), index = 2)

    //when & then
    check("f5", FileListPopupCopyItemsAction(show = true), never = true)
    check("f5", FileListPopupCopyItemsAction(show = true), index = 1)
    check("f5", FileListPopupCopyItemsAction(show = true), index = 2)
    check("f5", FileListPopupCopyItemsAction(show = true), selectedNames = Set("file 1"))

    //when & then
    check("f7", FileListPopupMkFolderAction(show = true))

    //when & then
    check("f8", FileListPopupDeleteAction(show = true), never = true)
    check("f8", FileListPopupDeleteAction(show = true), index = 1)
    check("delete", FileListPopupDeleteAction(show = true), never = true)
    check("delete", FileListPopupDeleteAction(show = true), selectedNames = Set("file 1"))

    Succeeded
  }

  it should "copy parent path into clipboard when onKeypress(C-c)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("..")))
    ))
    val comp = testRender(<(FileListPanel())(^.wrapped := props)())
    val viewProps = findComponentProps(comp, fileListPanelView)
    val screenMock = mock[BlessedScreenMock]

    //then
    (screenMock.copyToClipboard _).expects("/sub-dir")

    //when
    viewProps.onKeypress(screenMock.asInstanceOf[BlessedScreen], "C-c")

    Succeeded
  }

  it should "copy item path into clipboard when onKeypress(C-c)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("item 1")))
    ))
    val comp = testRender(<(FileListPanel())(^.wrapped := props)())
    val viewProps = findComponentProps(comp, fileListPanelView)
    val screenMock = mock[BlessedScreenMock]

    //then
    (screenMock.copyToClipboard _).expects(path.join("/sub-dir", "item 1"))

    //when
    viewProps.onKeypress(screenMock.asInstanceOf[BlessedScreen], "C-c")

    Succeeded
  }

  it should "dispatch action when onKeypress(M-pagedown)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("item 1")))
    ))
    val comp = testRender(<(FileListPanel())(^.wrapped := props)())
    val viewProps = findComponentProps(comp, fileListPanelView)
    val action = FileListOpenInDefaultAppAction(
      FutureTask("Opening item", Future.successful((new js.Object, new js.Object)))
    )

    //then
    (actions.openInDefaultApp _).expects("/sub-dir", "item 1").returning(action)
    dispatch.expects(action)

    //when
    viewProps.onKeypress(null, "M-pagedown")

    action.task.future.map(_ => Succeeded)
  }

  it should "dispatch action when onKeypress(enter)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("dir 1", isDir = true)))
    ))
    val comp = testRender(<(FileListPanel())(^.wrapped := props)())
    val viewProps = findComponentProps(comp, fileListPanelView)
    val changedDir = mock[FileListDir]
    val action = FileListDirChangeAction(
      FutureTask("Changing dir", Future.successful(changedDir))
    )

    //then
    (actions.changeDir _).expects(dispatch, props.state.isRight, Some("/sub-dir"), "dir 1").returning(action)
    dispatch.expects(action)

    //when
    viewProps.onKeypress(null, "enter")

    action.task.future.map(_ => Succeeded)
  }

  it should "dispatch action when onKeypress(C-pageup)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("item 1")))
    ))
    val comp = testRender(<(FileListPanel())(^.wrapped := props)())
    val viewProps = findComponentProps(comp, fileListPanelView)
    val changedDir = mock[FileListDir]
    val action = FileListDirChangeAction(
      FutureTask("Changing dir", Future.successful(changedDir))
    )

    //then
    (actions.changeDir _).expects(dispatch, props.state.isRight, Some("/sub-dir"), "..").returning(action)
    dispatch.expects(action)

    //when
    viewProps.onKeypress(null, "C-pageup")

    action.task.future.map(_ => Succeeded)
  }

  it should "dispatch action when onKeypress(C-pagedown)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("dir 1", isDir = true)))
    ))
    val comp = testRender(<(FileListPanel())(^.wrapped := props)())
    val viewProps = findComponentProps(comp, fileListPanelView)
    val changedDir = mock[FileListDir]
    val action = FileListDirChangeAction(
      FutureTask("Changing dir", Future.successful(changedDir))
    )

    //then
    (actions.changeDir _).expects(dispatch, props.state.isRight, Some("/sub-dir"), "dir 1").returning(action)
    dispatch.expects(action)

    //when
    viewProps.onKeypress(null, "C-pagedown")

    action.task.future.map(_ => Succeeded)
  }

  it should "not dispatch action if file when onKeypress(C-pagedown)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("item 1")))
    ))
    val comp = testRender(<(FileListPanel())(^.wrapped := props)())
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
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState(isActive = true))
    val comp = createTestRenderer(<(FileListPanel())(^.wrapped := props)()).root
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
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState(isActive = true))
    val comp = createTestRenderer(<(FileListPanel())(^.wrapped := props)()).root
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
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState(isActive = true))
    val renderer = createTestRenderer(<(FileListPanel())(^.wrapped := props)())
    findComponentProps(renderer.root, fileListPanelView).onKeypress(null, "C-s")
    findProps(renderer.root, fileListQuickSearch) should not be empty

    //when
    TestRenderer.act { () =>
      renderer.update(<(FileListPanel())(^.wrapped := props.copy(
        state = props.state.copy(isActive = false)
      ))())
    }

    //then
    findProps(renderer.root, fileListQuickSearch) should be (empty)
  }

  it should "hide quick Search box when onClose" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState(isActive = true))
    val comp = createTestRenderer(<(FileListPanel())(^.wrapped := props)()).root
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
    val actions = mock[FileListActions]
    val state = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("aB 1"),
        FileListItem("aBc1"),
        FileListItem("aBc 2")
      ))
    )
    val props = FileListPanelProps(dispatch, actions, state)
    val renderer = createTestRenderer(<(FileListPanel())(^.wrapped := props)())
    findComponentProps(renderer.root, fileListPanelView).onKeypress(null, "C-s")
    findProps(renderer.root, fileListQuickSearch) should not be empty

    def check(fullKey: String, index: Int, text: String, dispatchAction: Boolean)
             (implicit pos: Position): Unit = {
      
      if (dispatchAction) {
        //then
        dispatch.expects(FileListParamsChangedAction(
          isRight = props.state.isRight,
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
    check("2", index = 3, text = "aBc 2", dispatchAction = true)

    Succeeded
  }

  it should "render initial component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState())

    //when
    val result = createTestRenderer(<(FileListPanel())(^.wrapped := props)()).root

    //then
    assertFileListPanel(result, props)
  }

  private def assertFileListPanel(result: TestInstance,
                                  props: FileListPanelProps): Assertion = {
    
    def assertComponents(view: TestInstance): Assertion = {

      assertTestComponent(view, fileListPanelView) {
        case FileListPanelViewProps(dispatch, actions, state, _) =>
          dispatch shouldBe props.dispatch
          actions shouldBe props.actions
          state shouldBe props.state
      }
    }
    
    inside(result.children.toList) {
      case List(view) => assertComponents(view)
    }
  }
}

object FileListPanelSpec {

  @JSExportAll
  trait BlessedScreenMock {

    def copyToClipboard(text: String): Boolean
  }
}
