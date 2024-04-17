package farjs.fs

import farjs.filelist.api.FileListDir
import farjs.filelist.stack.WithPanelStacksSpec.withContext
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import farjs.filelist.{FileListPluginUiProps, FileListState, MockFileListActions}
import farjs.fs.FSPluginUi._
import farjs.fs.popups._
import farjs.ui.Dispatch
import farjs.ui.task.{Task, TaskAction}
import scommons.react.ReactClass
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class FSPluginUiSpec extends TestSpec with TestRendererUtils {

  FSPluginUi.drive = mockUiComponent("DriveController")
  FSPluginUi.foldersHistory = mockUiComponent("FoldersHistoryController")
  FSPluginUi.folderShortcuts = mockUiComponent("FolderShortcutsController")

  //noinspection TypeAnnotation
  class Actions {
    val changeDir = mockFunction[Dispatch, String, String, TaskAction]

    val actions = new MockFileListActions(
      changeDirMock = changeDir
    )
  }

  it should "dispatch TaskAction when onChangeDir in active panel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onClose = mockFunction[Unit]
    val actions = new Actions
    val fsPluginUi = new FSPluginUi()
    val currState = FileListState(currDir = FileListDir("C:/test", isRoot = false, js.Array()))
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

    val otherState = FileListState(currDir = FileListDir("/test2", isRoot = false, js.Array()))
    val otherStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, Some(otherState))
    ), updater = null)

    val renderer = createTestRenderer(withContext(
      <(fsPluginUi())(^.plain := FileListPluginUiProps(dispatch, onClose))(),
      leftStack = currStack,
      rightStack = otherStack
    ))
    val foldersHistoryProps = findComponentProps(renderer.root, foldersHistory)
    val action = TaskAction(Task("Changing Dir",
      Future.successful(FileListDir("/", isRoot = true, items = js.Array()))
    ))
    val dir = "test/dir"

    //then
    actions.changeDir.expects(dispatch, "", dir).returning(action)
    dispatch.expects(action)

    //when
    foldersHistoryProps.onChangeDir(dir)

    //then
    currStackState shouldBe List(currFsItem)
  }

  it should "dispatch TaskAction when onChangeDir in Drive popup" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onClose = mockFunction[Unit]
    val actions = new Actions
    val fsPluginUi = new FSPluginUi()
    val currState = FileListState(currDir = FileListDir("C:/test", isRoot = false, js.Array()))
    val currStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(currState))
    ), updater = null)
    val otherState = FileListState(currDir = FileListDir("/test2", isRoot = false, js.Array()))
    val otherStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, Some(otherState))
    ), updater = null)

    val renderer = createTestRenderer(withContext(
      <(fsPluginUi())(^.plain := FileListPluginUiProps(dispatch, onClose))(),
      leftStack = otherStack,
      rightStack = currStack
    ))
    val driveProps = findComponentProps(renderer.root, drive)
    val action = TaskAction(Task("Changing Dir",
      Future.successful(FileListDir("/", isRoot = true, items = js.Array()))
    ))
    val dir = "test/dir"

    //then
    actions.changeDir.expects(dispatch, "", dir).returning(action)
    dispatch.expects(action)

    //when
    driveProps.onChangeDir(dir, false)
  }

  it should "not dispatch TaskAction if same dir when onChangeDir" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onClose = mockFunction[Unit]
    val actions = new Actions
    val fsPluginUi = new FSPluginUi()
    val currState = FileListState(currDir = FileListDir("C:/test", isRoot = false, js.Array()))
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

    val otherState = FileListState(currDir = FileListDir("/test2", isRoot = false, js.Array()))
    val otherStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, Some(otherState))
    ), updater = null)

    val renderer = createTestRenderer(withContext(
      <(fsPluginUi())(^.plain := FileListPluginUiProps(dispatch, onClose))(),
      leftStack = otherStack,
      rightStack = currStack
    ))
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
    val onClose: js.Function0[Unit] = mockFunction[Unit]
    val fsPluginUi = new FSPluginUi()
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), None, None)
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    //when
    val result = createTestRenderer(withContext(
      <(fsPluginUi())(^.plain := FileListPluginUiProps(dispatch, onClose))(),
      leftStack,
      rightStack
    )).root

    //then
    val onChangeDirInActivePanel = findComponentProps(result, foldersHistory).onChangeDir
    assertComponents(result.children, List(
      <(drive())(^.assertWrapped(inside(_) {
        case DriveControllerProps(resDispatch, showDrivePopupOnLeft, _, resOnClose) =>
          resDispatch shouldBe dispatch
          showDrivePopupOnLeft shouldBe None
          resOnClose should be theSameInstanceAs onClose
      }))(),

      <(foldersHistory())(^.assertWrapped(inside(_) {
        case FoldersHistoryControllerProps(showPopup, onChangeDir, resOnClose) =>
          showPopup shouldBe false
          onChangeDir shouldBe onChangeDirInActivePanel
          resOnClose should be theSameInstanceAs onClose
      }))(),

      <(folderShortcuts())(^.assertWrapped(inside(_) {
        case FolderShortcutsControllerProps(showPopup, onChangeDir, resOnClose) =>
          showPopup shouldBe false
          onChangeDir shouldBe onChangeDirInActivePanel
          resOnClose should be theSameInstanceAs onClose
      }))()
    ))
  }
}
