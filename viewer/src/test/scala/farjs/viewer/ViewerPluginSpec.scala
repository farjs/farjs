package farjs.viewer

import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack._
import farjs.filelist.{FileListState, MockFileListActions}
import farjs.viewer.ViewerEvent._
import scommons.nodejs.test.TestSpec
import scommons.react.ReactClass

class ViewerPluginSpec extends TestSpec {

  it should "define triggerKeys" in {
    //when & then
    ViewerPlugin.triggerKeys.toList shouldBe List("f3", onViewerOpenLeft, onViewerOpenRight)
  }

  it should "return None if .. when onKeyTrigger" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem.up,
      FileListItem("item 1")
    )))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(leftState))
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), updater = null)
    val stacks = WithPanelStacksProps(leftStack, null, rightStack, null)

    //when & then
    ViewerPlugin.onKeyTrigger("f3", stacks) shouldBe None
  }

  it should "return None if non-local fs when onKeyTrigger" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(isLocalFSMock = false)
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem("item 1")
    )))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(leftState))
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), updater = null)
    val stacks = WithPanelStacksProps(leftStack, null, rightStack, null)

    //when & then
    ViewerPlugin.onKeyTrigger("f3", stacks) shouldBe None
  }

  it should "return Some(ViewerPluginUi) if file when onKeyTrigger(f3)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem("item 1")
    )))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(leftState))
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), updater = null)
    val stacks = WithPanelStacksProps(leftStack, null, rightStack, null)

    //when & then
    ViewerPlugin.onKeyTrigger("f3", stacks) should not be None
  }

  it should "return Some(ViewerPluginUi) if file when onKeyTrigger(onViewerOpenLeft)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem("item 1")
    )))
    val leftStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(leftState))
    ), updater = null)

    val rightStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), updater = null)
    val stacks = WithPanelStacksProps(leftStack, null, rightStack, null)

    //when & then
    ViewerPlugin.onKeyTrigger(onViewerOpenLeft, stacks) should not be None
  }

  it should "return Some(ViewerPluginUi) if file when onKeyTrigger(onViewerOpenRight)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem("item 1")
    )))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(leftState))
    ), updater = null)
    val stacks = WithPanelStacksProps(leftStack, null, rightStack, null)

    //when & then
    ViewerPlugin.onKeyTrigger(onViewerOpenRight, stacks) should not be None
  }

  it should "return Some(ViewItemsPopup) if dir when onKeyTrigger" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem("item 1", isDir = true)
    )))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(leftState))
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), updater = null)
    val stacks = WithPanelStacksProps(leftStack, null, rightStack, null)

    //when & then
    ViewerPlugin.onKeyTrigger("f3", stacks) should not be None
  }
}
