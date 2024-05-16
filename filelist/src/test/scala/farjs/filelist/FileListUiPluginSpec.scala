package farjs.filelist

import farjs.filelist.api._
import farjs.filelist.stack._
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass

import scala.concurrent.Future
import scala.scalajs.js

class FileListUiPluginSpec extends AsyncTestSpec {

  it should "define triggerKeys" in {
    //when & then
    FileListUiPlugin.triggerKeys.toList shouldBe List(
      "f1", "f7", "f8", "delete", "f9", "f10", "M-s", "M-d"
    )
  }

  it should "return None/Some if non-/trigger key when onKeyTrigger" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem("item 1")
    )))
    val leftStack = new PanelStack(isActive = true, js.Array(
      new PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, actions, state)
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, js.Array(
      new PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, actions, state)
    ), updater = null)
    val stacks = PanelStacks(PanelStackData(leftStack, null), PanelStackData(rightStack, null))

    //when & then
    Future.sequence(Seq(
      FileListUiPlugin.onKeyTrigger("test_key", stacks).toFuture.map(_ shouldBe js.undefined),
      FileListUiPlugin.onKeyTrigger("f1", stacks).toFuture.map(_ should not be js.undefined)
    )).map(_ => Succeeded)
  }

  it should "return Some(ui) if trigger key=f1/f9/f10/Alt-S/Alt-D when createUi" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem.up
    )))
    val someData = Some(FileListData(dispatch, actions, state))

    //when & then
    inside(FileListUiPlugin.createUiData("f1", someData)) {
      case Some(FileListUiData(true, false, false, false, false, None, `someData`, _)) =>
    }
    inside(FileListUiPlugin.createUiData("f9", someData)) {
      case Some(FileListUiData(false, false, true, false, false, None, `someData`, _)) =>
    }
    inside(FileListUiPlugin.createUiData("f10", someData)) {
      case Some(FileListUiData(false, true, false, false, false, None, `someData`, _)) =>
    }
    inside(FileListUiPlugin.createUiData("M-s", someData)) {
      case Some(FileListUiData(false, false, false, false, false, Some(true), `someData`, _)) =>
    }
    inside(FileListUiPlugin.createUiData("M-d", someData)) {
      case Some(FileListUiData(false, false, false, false, false, Some(false), `someData`, _)) =>
    }
    Succeeded
  }

  it should "return None/Some if trigger key=f7 when createUi" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions(new MockFileListApi(capabilitiesMock = js.Set(FileListCapability.mkDirs)))
    val state = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem.up
    )))
    val noCapabilityData = Some(FileListData(dispatch, new MockFileListActions, state))
    val correctData = Some(FileListData(dispatch, actions, state))

    //when & then
    FileListUiPlugin.createUiData("f7", None) shouldBe None
    FileListUiPlugin.createUiData("f7", noCapabilityData) shouldBe None
    inside(FileListUiPlugin.createUiData("f7", correctData)) {
      case Some(FileListUiData(false, false, false, false, true, None, `correctData`, _)) =>
    }
    Succeeded
  }

  it should "return None/Some if trigger key=f8/delete when createUi" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions(new MockFileListApi(capabilitiesMock = js.Set(FileListCapability.delete)))
    val state = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem("item 1")
    )))
    val noCapabilityData = Some(FileListData(dispatch, new MockFileListActions, state))
    val noItemData = Some(FileListData(dispatch, actions, FileListState.copy(state)(currDir = FileListDir.copy(state.currDir)(items = js.Array(
      FileListItem.up,
      FileListItem("item 1")
    )))))
    val selectedItemsData = Some(FileListData(dispatch, actions, FileListState.copy(state)(currDir = FileListDir.copy(state.currDir)(items = js.Array(
      FileListItem.up,
      FileListItem("test")
    )), selectedNames = js.Set("test"))))
    val currItemData = Some(FileListData(dispatch, actions, state))

    //when & then
    FileListUiPlugin.createUiData("f8", None) shouldBe None
    FileListUiPlugin.createUiData("f8", noCapabilityData) shouldBe None
    FileListUiPlugin.createUiData("f8", noItemData) shouldBe None
    inside(FileListUiPlugin.createUiData("f8", selectedItemsData)) {
      case Some(FileListUiData(false, false, false, true, false, None, `selectedItemsData`, _)) =>
    }
    inside(FileListUiPlugin.createUiData("delete", currItemData)) {
      case Some(FileListUiData(false, false, false, true, false, None, `currItemData`, _)) =>
    }
    Succeeded
  }
}
