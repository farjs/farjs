package farjs.app.filelist.fs.popups

import farjs.app.filelist.fs.popups.FSPopups._
import farjs.app.filelist.fs.popups.FSPopupsActions.DrivePopupHidden
import farjs.filelist.FileListActions.FileListDirChangeAction
import farjs.filelist.api.FileListDir
import farjs.filelist.stack.WithPanelStacksSpec.withContext
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import farjs.filelist.{FileListState, MockFileListActions}
import scommons.react.ReactClass
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class FSPopupsSpec extends TestSpec with TestRendererUtils {

  FSPopups.drive = mockUiComponent("DriveController")
  FSPopups.foldersHistory = mockUiComponent("FoldersHistoryController")
  FSPopups.folderShortcuts = mockUiComponent("FolderShortcutsController")

  //noinspection TypeAnnotation
  class Actions {
    val changeDir = mockFunction[Dispatch, Option[String], String, FileListDirChangeAction]

    val actions = new MockFileListActions(
      changeDirMock = changeDir
    )
  }

  it should "dispatch FileListDirChangeAction when onChangeDir in active panel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FSPopupsProps(dispatch, FSPopupsState())
    val currState = FileListState(currDir = FileListDir("C:/test", isRoot = false, Nil))
    val currFsItem = PanelStackItem(
      "fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(currState)
    )
    var currStackState: List[PanelStackItem[_]] = List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None),
      currFsItem
    )
    val currStack = new PanelStack(isActive = true, currStackState, { f =>
      currStackState = f(currStackState)
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)

    val otherState = FileListState(currDir = FileListDir("/test2", isRoot = false, Nil))
    val otherStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, Some(otherState))
    ), updater = null)

    val renderer = createTestRenderer(
      withContext(<(FSPopups())(^.wrapped := props)(), leftStack = currStack, rightStack = otherStack)
    )
    val foldersHistoryProps = findComponentProps(renderer.root, foldersHistory)
    val action = FileListDirChangeAction(FutureTask("Changing Dir",
      Future.successful(FileListDir("/", isRoot = true, items = Nil))
    ))
    val dir = "test/dir"

    //then
    actions.changeDir.expects(dispatch, None, dir).returning(action)
    dispatch.expects(action)

    //when
    foldersHistoryProps.onChangeDir(dir)

    //then
    currStackState shouldBe List(currFsItem)
  }

  it should "dispatch FileListDirChangeAction when onChangeDir in Drive popup" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FSPopupsProps(dispatch, FSPopupsState())
    val currState = FileListState(currDir = FileListDir("C:/test", isRoot = false, Nil))
    val currStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(currState))
    ), updater = null)
    val otherState = FileListState(currDir = FileListDir("/test2", isRoot = false, Nil))
    val otherStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, Some(otherState))
    ), updater = null)

    val renderer = createTestRenderer(
      withContext(<(FSPopups())(^.wrapped := props)(), leftStack = otherStack, rightStack = currStack)
    )
    val driveProps = findComponentProps(renderer.root, drive)
    val action = FileListDirChangeAction(FutureTask("Changing Dir",
      Future.successful(FileListDir("/", isRoot = true, items = Nil))
    ))
    val dir = "test/dir"

    //then
    actions.changeDir.expects(dispatch, None, dir).returning(action)
    dispatch.expects(action)

    //when
    driveProps.onChangeDir(dir, false)
  }

  it should "not dispatch FileListDirChangeAction if same dir when onChangeDir" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FSPopupsProps(dispatch, FSPopupsState())
    val currState = FileListState(currDir = FileListDir("C:/test", isRoot = false, Nil))
    val currFsItem = PanelStackItem(
      "fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(currState)
    )
    var currStackState: List[PanelStackItem[_]] = List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None),
      currFsItem
    )
    val currStack = new PanelStack(isActive = true, currStackState, { f =>
      currStackState = f(currStackState)
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)

    val otherState = FileListState(currDir = FileListDir("/test2", isRoot = false, Nil))
    val otherStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, Some(otherState))
    ), updater = null)

    val renderer = createTestRenderer(
      withContext(<(FSPopups())(^.wrapped := props)(), leftStack = otherStack, rightStack = currStack)
    )
    val foldersHistoryProps = findComponentProps(renderer.root, foldersHistory)
    val dir = currState.currDir.path

    //then
    actions.changeDir.expects(*, *, *).never()
    dispatch.expects(*).never()

    //when
    foldersHistoryProps.onChangeDir(dir)

    //then
    currStackState shouldBe List(currFsItem)
  }

  it should "render component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FSPopupsProps(dispatch, FSPopupsState())
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), None, None)
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    //when
    val result = createTestRenderer(
      withContext(<(FSPopups())(^.wrapped := props)(), leftStack, rightStack)
    ).root

    //then
    val onChangeDirInActivePanel = findComponentProps(result, foldersHistory).onChangeDir
    assertComponents(result.children, List(
      <(drive())(^.assertWrapped(inside(_) {
        case DriveControllerProps(dispatch, show, _) =>
          dispatch shouldBe props.dispatch
          show shouldBe DrivePopupHidden
      }))(),

      <(foldersHistory())(^.assertWrapped(inside(_) {
        case FoldersHistoryControllerProps(dispatch, showPopup, onChangeDir) =>
          dispatch shouldBe props.dispatch
          showPopup shouldBe false
          onChangeDir shouldBe onChangeDirInActivePanel
      }))(),

      <(folderShortcuts())(^.assertWrapped(inside(_) {
        case FolderShortcutsControllerProps(dispatch, showPopup, onChangeDir) =>
          dispatch shouldBe props.dispatch
          showPopup shouldBe false
          onChangeDir shouldBe onChangeDirInActivePanel
      }))()
    ))
  }
}
