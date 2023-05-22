package farjs.viewer

import farjs.file.FileEvent.onFileView
import farjs.file.FileViewHistory
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack._
import farjs.filelist.{FileListState, MockFileListActions}
import farjs.viewer.ViewerEvent._
import scommons.nodejs.Stats
import scommons.nodejs.test.TestSpec
import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

class ViewerPluginSpec extends TestSpec {

  //noinspection TypeAnnotation
  class FS {
    val lstatSync = mockFunction[String, Stats]

    val fs = new MockFS(
      lstatSyncMock = lstatSync
    )
  }

  it should "define triggerKeys" in {
    //when & then
    ViewerPlugin.triggerKeys.toList shouldBe List(
      "f3", onViewerOpenLeft, onViewerOpenRight, onFileView
    )
  }

  it should "not fail if no such file when onKeyTrigger(onFileView)" in {
    //given
    val fs = new FS
    ViewerPlugin.fs = fs.fs
    val data = FileViewHistory(
      path = "test/path",
      isEdit = false,
      encoding = "utf8",
      position = 123,
      wrap = None,
      column = None
    )

    //then
    fs.lstatSync.expects(data.path).throwing(JavaScriptException(js.Error("no such file")))

    //when & then
    ViewerPlugin.onKeyTrigger(onFileView, null, data.asInstanceOf[js.Dynamic]) should not be None
  }

  it should "return Some(ViewerPluginUi) when onKeyTrigger(onFileView)" in {
    //given
    val fs = new FS
    ViewerPlugin.fs = fs.fs
    val data = FileViewHistory(
      path = "test/path",
      isEdit = false,
      encoding = "utf8",
      position = 123,
      wrap = None,
      column = None
    )

    //then
    fs.lstatSync.expects(data.path).returning(js.Dynamic.literal(size = 50).asInstanceOf[Stats])

    //when & then
    ViewerPlugin.onKeyTrigger(onFileView, null, data.asInstanceOf[js.Dynamic]) should not be None
  }

  it should "return None if .. when onKeyTrigger(f3)" in {
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

  it should "return None if non-local fs when onKeyTrigger(f3)" in {
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

  it should "return Some(ViewItemsPopup) if dir when onKeyTrigger(f3)" in {
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
