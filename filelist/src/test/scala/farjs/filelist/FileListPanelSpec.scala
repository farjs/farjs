package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.FileListPanel._
import farjs.filelist.FileListPanelSpec._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.popups.FileListPopupsActions._
import farjs.ui._
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

  FileListPanel.withSizeComp = () => "WithSize".asInstanceOf[ReactClass]
  FileListPanel.fileListPanelView = () => "FileListPanelView".asInstanceOf[ReactClass]

  private val (width, height) = (25, 15)

  it should "dispatch popups actions when F1-F10 keys" in {
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
      getRenderedProps(renderer.root, fileListPanelView).onKeypress(null, fullKey)
    }

    //when & then
    check("f1", FileListPopupHelpAction(show = true))
    check("f3", FileListPopupViewItemsAction(show = true), never = true)
    check("f3", FileListPopupViewItemsAction(show = true), index = 1, never = true)
    check("f3", FileListPopupViewItemsAction(show = true), index = 1, selectedNames = Set("file 1"))
    check("f3", FileListPopupViewItemsAction(show = true), index = 2)
    check("f7", FileListPopupMkFolderAction(show = true))
    check("f8", FileListPopupDeleteAction(show = true), never = true)
    check("f8", FileListPopupDeleteAction(show = true), index = 1)
    check("delete", FileListPopupDeleteAction(show = true), never = true)
    check("delete", FileListPopupDeleteAction(show = true), selectedNames = Set("file 1"))
    check("f10", FileListPopupExitAction(show = true))

    Succeeded
  }

  it should "focus next element when onKeypress(tab)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState())
    val comp = testRender(<(FileListPanel())(^.wrapped := props)())
    val viewProps = getRenderedProps(comp, fileListPanelView)
    val screenMock = mock[BlessedScreenMock]

    //then
    (screenMock.focusNext _).expects()

    //when
    viewProps.onKeypress(screenMock.asInstanceOf[BlessedScreen], "tab")

    Succeeded
  }

  it should "focus previous element when onKeypress(S-tab)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState())
    val comp = testRender(<(FileListPanel())(^.wrapped := props)())
    val viewProps = getRenderedProps(comp, fileListPanelView)
    val screenMock = mock[BlessedScreenMock]

    //then
    (screenMock.focusPrevious _).expects()

    //when
    viewProps.onKeypress(screenMock.asInstanceOf[BlessedScreen], "S-tab")

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
    val viewProps = getRenderedProps(comp, fileListPanelView)
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
    val viewProps = getRenderedProps(comp, fileListPanelView)
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
    val viewProps = getRenderedProps(comp, fileListPanelView)
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
    val viewProps = getRenderedProps(comp, fileListPanelView)
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
    val viewProps = getRenderedProps(comp, fileListPanelView)
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
    val viewProps = getRenderedProps(comp, fileListPanelView)
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
    val viewProps = getRenderedProps(comp, fileListPanelView)

    //then
    dispatch.expects(*).never()

    //when
    viewProps.onKeypress(null, "C-pagedown")

    Succeeded
  }

  it should "do nothing if unknown key when onKeypress" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("item 1")))
    ))
    val comp = testRender(<(FileListPanel())(^.wrapped := props)())
    val viewProps = getRenderedProps(comp, fileListPanelView)

    //then
    dispatch.expects(*).never()

    //when
    viewProps.onKeypress(null, "unknown")

    Succeeded
  }

  it should "render component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPanelProps(dispatch, actions, FileListState())

    //when
    val result = testRender(<(FileListPanel())(^.wrapped := props)())

    //then
    assertFileListPanel(result, props)
  }

  private def getRenderedProps[T](result: TestInstance,
                                  searchComp: UiComponent[T])(implicit pos: Position): T = {

    val withSizeProps = findComponentProps(result, withSizeComp)
    val rendered = createTestRenderer(withSizeProps.render(width, height)).root
    findComponentProps(rendered, searchComp)
  }
  
  private def assertFileListPanel(result: TestInstance,
                                  props: FileListPanelProps): Assertion = {
    
    def assertComponents(view: TestInstance): Assertion = {

      assertTestComponent(view, fileListPanelView) {
        case FileListPanelViewProps(dispatch, actions, state, rewWidth, resHeight, _) =>
          dispatch shouldBe props.dispatch
          actions shouldBe props.actions
          state shouldBe props.state
          rewWidth shouldBe width
          resHeight shouldBe height
      }
    }
    
    assertTestComponent(result, withSizeComp) { case WithSizeProps(render) =>
      val result = createTestRenderer(render(width, height)).root
      
      assertComponents(result)
    }
  }
}

object FileListPanelSpec {

  @JSExportAll
  trait BlessedScreenMock {

    def focusPrevious(): Unit
    def focusNext(): Unit

    def copyToClipboard(text: String): Boolean
  }
}
