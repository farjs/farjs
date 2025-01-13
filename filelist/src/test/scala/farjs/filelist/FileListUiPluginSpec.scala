package farjs.filelist

import farjs.filelist.api._
import farjs.filelist.stack._
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass

import scala.concurrent.Future
import scala.scalajs.js

class FileListUiPluginSpec extends AsyncTestSpec {
  
  private val jsUndefined: js.UndefOr[Boolean] = js.undefined
  private val jsFalse: js.UndefOr[Boolean] = false
  private val jsTrue: js.UndefOr[Boolean] = true

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
    val stacks = WithStacksProps(WithStacksData(leftStack, null), WithStacksData(rightStack, null))

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
    val someData = FileListData(dispatch, actions, state)

    //when & then
    inside(FileListUiPlugin.createUiData("f1", someData).toOption) {
      case Some(FileListUiData(_, `someData`, `jsTrue`, `jsUndefined`, `jsUndefined`, `jsUndefined`, `jsUndefined`, `jsUndefined`)) =>
    }
    inside(FileListUiPlugin.createUiData("f9", someData).toOption) {
      case Some(FileListUiData(_, `someData`, `jsUndefined`, `jsUndefined`, `jsTrue`, `jsUndefined`, `jsUndefined`, `jsUndefined`)) =>
    }
    inside(FileListUiPlugin.createUiData("f10", someData).toOption) {
      case Some(FileListUiData(_, `someData`, `jsUndefined`, `jsTrue`, `jsUndefined`, `jsUndefined`, `jsUndefined`, `jsUndefined`)) =>
    }
    inside(FileListUiPlugin.createUiData("M-s", someData).toOption) {
      case Some(FileListUiData(_, `someData`, `jsUndefined`, `jsUndefined`, `jsUndefined`, `jsUndefined`, `jsUndefined`, `jsTrue`)) =>
    }
    inside(FileListUiPlugin.createUiData("M-d", someData).toOption) {
      case Some(FileListUiData(_, `someData`, `jsUndefined`, `jsUndefined`, `jsUndefined`, `jsUndefined`, `jsUndefined`, `jsFalse`)) =>
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
    val noCapabilityData = FileListData(dispatch, new MockFileListActions, state)
    val correctData = FileListData(dispatch, actions, state)

    //when & then
    FileListUiPlugin.createUiData("f7", js.undefined).toOption shouldBe None
    FileListUiPlugin.createUiData("f7", noCapabilityData).toOption shouldBe None
    inside(FileListUiPlugin.createUiData("f7", correctData).toOption) {
      case Some(FileListUiData(_, `correctData`, `jsUndefined`, `jsUndefined`, `jsUndefined`, `jsUndefined`, `jsTrue`, `jsUndefined`)) =>
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
    val noCapabilityData = FileListData(dispatch, new MockFileListActions, state)
    val noItemData = FileListData(dispatch, actions, FileListState.copy(state)(currDir = FileListDir.copy(state.currDir)(items = js.Array(
      FileListItem.up,
      FileListItem("item 1")
    ))))
    val selectedItemsData = FileListData(dispatch, actions, FileListState.copy(state)(currDir = FileListDir.copy(state.currDir)(items = js.Array(
      FileListItem.up,
      FileListItem("test")
    )), selectedNames = js.Set("test")))
    val currItemData = FileListData(dispatch, actions, state)

    //when & then
    FileListUiPlugin.createUiData("f8", js.undefined).toOption shouldBe None
    FileListUiPlugin.createUiData("f8", noCapabilityData).toOption shouldBe None
    FileListUiPlugin.createUiData("f8", noItemData).toOption shouldBe None
    inside(FileListUiPlugin.createUiData("f8", selectedItemsData).toOption) {
      case Some(FileListUiData(_, `selectedItemsData`, `jsUndefined`, `jsUndefined`, `jsUndefined`, `jsTrue`, `jsUndefined`, `jsUndefined`)) =>
    }
    inside(FileListUiPlugin.createUiData("delete", currItemData).toOption) {
      case Some(FileListUiData(_, `currItemData`, `jsUndefined`, `jsUndefined`, `jsUndefined`, `jsTrue`, `jsUndefined`, `jsUndefined`)) =>
    }
    Succeeded
  }
}
