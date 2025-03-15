package farjs.fs

import farjs.filelist.api.FileListDir
import farjs.filelist.stack.WithStacksSpec.withContext
import farjs.filelist.stack.{PanelStack, WithStacksData, PanelStackItem}
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
    val dispatch = mockFunction[js.Any, Unit]
    val onClose = mockFunction[Unit]
    val actions = new Actions
    val fsPluginUi = new FSPluginUi()
    val currState = FileListState(currDir = FileListDir("C:/test", isRoot = false, js.Array()))
    val currFsItem = PanelStackItem(
      "fsComp".asInstanceOf[ReactClass], dispatch: js.Function1[js.Any, Unit], actions.actions, currState
    )
    var currStackState: js.Array[PanelStackItem[_]] = js.Array(
      PanelStackItem("otherComp".asInstanceOf[ReactClass]),
      currFsItem
    )
    val currStack = new PanelStack(isActive = true, currStackState, { f =>
      currStackState = f(currStackState)
    }: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] => Unit)

    val otherState = FileListState(currDir = FileListDir("/test2", isRoot = false, js.Array()))
    val otherStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], js.undefined, js.undefined, otherState)
    ), updater = null)

    val renderer = createTestRenderer(withContext(
      <(fsPluginUi())(^.plain := FileListPluginUiProps(dispatch, onClose))(),
      left = WithStacksData(currStack, null),
      right = WithStacksData(otherStack, null)
    ))
    val foldersHistoryProps = findComponentProps(renderer.root, foldersHistory, plain = true)
    val action = TaskAction(Task("Changing Dir",
      Future.successful(FileListDir("/", isRoot = true, items = js.Array()))
    ))
    val dir = "test/dir"

    //then
    actions.changeDir.expects(*, "", dir).returning(action)
    dispatch.expects(action)

    //when
    foldersHistoryProps.onChangeDir(dir)

    //then
    currStackState.toList shouldBe List(currFsItem)
  }

  it should "dispatch TaskAction when onChangeDir in Drive popup" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onClose = mockFunction[Unit]
    val actions = new Actions
    val fsPluginUi = new FSPluginUi()
    val currState = FileListState(currDir = FileListDir("C:/test", isRoot = false, js.Array()))
    val currStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch: js.Function1[js.Any, Unit], actions.actions, currState)
    ), updater = null)
    val otherState = FileListState(currDir = FileListDir("/test2", isRoot = false, js.Array()))
    val otherStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], js.undefined, js.undefined, otherState)
    ), updater = null)

    val renderer = createTestRenderer(withContext(
      <(fsPluginUi())(^.plain := FileListPluginUiProps(dispatch, onClose))(),
      left = WithStacksData(otherStack, null),
      right = WithStacksData(currStack, null)
    ))
    val driveProps = findComponentProps(renderer.root, drive, plain = true)
    val action = TaskAction(Task("Changing Dir",
      Future.successful(FileListDir("/", isRoot = true, items = js.Array()))
    ))
    val dir = "test/dir"

    //then
    actions.changeDir.expects(*, "", dir).returning(action)
    dispatch.expects(action)

    //when
    driveProps.onChangeDir(dir, false)
  }

  it should "not dispatch TaskAction if same dir when onChangeDir" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onClose = mockFunction[Unit]
    val actions = new Actions
    val fsPluginUi = new FSPluginUi()
    val currState = FileListState(currDir = FileListDir("C:/test", isRoot = false, js.Array()))
    val currFsItem = PanelStackItem(
      "fsComp".asInstanceOf[ReactClass], dispatch: js.Function1[js.Any, Unit], actions.actions, currState
    )
    var currStackState = js.Array[PanelStackItem[_]](
      PanelStackItem("otherComp".asInstanceOf[ReactClass]),
      currFsItem
    )
    val currStack = new PanelStack(isActive = true, currStackState, { f =>
      currStackState = f(currStackState)
    }: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] => Unit)

    val otherState = FileListState(currDir = FileListDir("/test2", isRoot = false, js.Array()))
    val otherStack = new PanelStack(isActive = false, js.Array[PanelStackItem[_]](
      PanelStackItem("fsComp".asInstanceOf[ReactClass], js.undefined, js.undefined, otherState)
    ), updater = null)

    val renderer = createTestRenderer(withContext(
      <(fsPluginUi())(^.plain := FileListPluginUiProps(dispatch, onClose))(),
      left = WithStacksData(otherStack, null),
      right = WithStacksData(currStack, null)
    ))
    val foldersHistoryProps = findComponentProps(renderer.root, foldersHistory, plain = true)
    val dir = currState.currDir.path

    //then
    actions.changeDir.expects(*, *, *).never()
    dispatch.expects(*).never()

    //when
    foldersHistoryProps.onChangeDir(dir)

    //then
    currStackState.toList shouldBe List(currFsItem)
  }

  it should "render component" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val onClose: js.Function0[Unit] = mockFunction[Unit]
    val fsPluginUi = new FSPluginUi()
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, js.undefined, js.undefined)
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("otherComp".asInstanceOf[ReactClass])
    ), null)

    //when
    val result = createTestRenderer(withContext(
      <(fsPluginUi())(^.plain := FileListPluginUiProps(dispatch, onClose))(),
      left = WithStacksData(leftStack, null),
      right = WithStacksData(rightStack, null)
    )).root

    //then
    val onChangeDirInActivePanel = findComponentProps(result, foldersHistory, plain = true).onChangeDir
    assertComponents(result.children, List(
      <(drive())(^.assertPlain[DriveControllerProps](inside(_) {
        case DriveControllerProps(resDispatch, showDrivePopupOnLeft, _, resOnClose) =>
          resDispatch shouldBe dispatch
          showDrivePopupOnLeft shouldBe js.undefined
          resOnClose should be theSameInstanceAs onClose
      }))(),

      <(foldersHistory())(^.assertPlain[FoldersHistoryControllerProps](inside(_) {
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
