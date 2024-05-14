package farjs.viewer

import farjs.file.FileEvent.onFileView
import farjs.file.FileViewHistory
import farjs.filelist.api.{FileListDir, FileListItem, MockFileListApi}
import farjs.filelist.stack._
import farjs.filelist.{FileListState, MockFileListActions}
import farjs.viewer.ViewerEvent._
import scommons.nodejs._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

class ViewerPluginSpec extends AsyncTestSpec {

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

  it should "return failed Future if no such file when onKeyTrigger(onFileView)" in {
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

    //when
    ViewerPlugin.onKeyTrigger(onFileView, null, data.asInstanceOf[js.Dynamic]).failed.map { ex =>
      //then
      ex.getMessage should include("no such file")
    }
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

    //when
    ViewerPlugin.onKeyTrigger(onFileView, null, data.asInstanceOf[js.Dynamic]).map { res =>
      //then
      res should not be None
    }
  }

  it should "return None if .. when onKeyTrigger(f3)" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem.up,
      FileListItem("item 1")
    )))
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, actions, leftState)
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), updater = null)
    val stacks = WithPanelStacksProps(PanelStackData(leftStack, null), PanelStackData(rightStack, null))

    //when & then
    ViewerPlugin.onKeyTrigger("f3", stacks).map(_ shouldBe None)
  }

  it should "return None if non-local fs when onKeyTrigger(f3)" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions(new MockFileListApi(isLocalMock = false))
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem("item 1")
    )))
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, actions, leftState)
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), updater = null)
    val stacks = WithPanelStacksProps(PanelStackData(leftStack, null), PanelStackData(rightStack, null))

    //when & then
    ViewerPlugin.onKeyTrigger("f3", stacks).map(_ shouldBe None)
  }

  it should "return failed Future if no such file when onKeyTrigger(f3)" in {
    //given
    val fs = new FS
    ViewerPlugin.fs = fs.fs
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val item = FileListItem("item 1")
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(item)))
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, actions, leftState)
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), updater = null)
    val stacks = WithPanelStacksProps(PanelStackData(leftStack, null), PanelStackData(rightStack, null))
    val filePath = path.join(leftState.currDir.path, item.name)

    //then
    fs.lstatSync.expects(filePath).throwing(JavaScriptException(js.Error("no such file")))

    //when
    ViewerPlugin.onKeyTrigger("f3", stacks).failed.map { ex =>
      //then
      ex.getMessage should include("no such file")
    }
  }

  it should "return Some(ViewerPluginUi) if file when onKeyTrigger(f3)" in {
    //given
    val fs = new FS
    ViewerPlugin.fs = fs.fs
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val item = FileListItem("item 1")
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(item)))
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, actions, leftState)
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), updater = null)
    val stacks = WithPanelStacksProps(PanelStackData(leftStack, null), PanelStackData(rightStack, null))
    val filePath = path.join(leftState.currDir.path, item.name)

    //then
    fs.lstatSync.expects(filePath).returning(js.Dynamic.literal(size = 50).asInstanceOf[Stats])

    //when & then
    ViewerPlugin.onKeyTrigger("f3", stacks).map(_ should not be None)
  }

  it should "return Some(ViewerPluginUi) if file when onKeyTrigger(onViewerOpenLeft)" in {
    //given
    val fs = new FS
    ViewerPlugin.fs = fs.fs
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val item = FileListItem("item 1")
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(item)))
    val leftStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, actions, leftState)
    ), updater = null)

    val rightStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), updater = null)
    val stacks = WithPanelStacksProps(PanelStackData(leftStack, null), PanelStackData(rightStack, null))
    val filePath = path.join(leftState.currDir.path, item.name)
    
    //then
    fs.lstatSync.expects(filePath).returning(js.Dynamic.literal(size = 50).asInstanceOf[Stats])

    //when & then
    ViewerPlugin.onKeyTrigger(onViewerOpenLeft, stacks).map(_ should not be None)
  }

  it should "return Some(ViewerPluginUi) if file when onKeyTrigger(onViewerOpenRight)" in {
    //given
    val fs = new FS
    ViewerPlugin.fs = fs.fs
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val item = FileListItem("item 1")
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(item)))
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, actions, leftState)
    ), updater = null)
    val stacks = WithPanelStacksProps(PanelStackData(leftStack, null), PanelStackData(rightStack, null))
    val filePath = path.join(leftState.currDir.path, item.name)

    //then
    fs.lstatSync.expects(filePath).returning(js.Dynamic.literal(size = 50).asInstanceOf[Stats])

    //when & then
    ViewerPlugin.onKeyTrigger(onViewerOpenRight, stacks).map(_ should not be None)
  }

  it should "return Some(ViewItemsPopup) if dir when onKeyTrigger(f3)" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem("item 1", isDir = true)
    )))
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, actions, leftState)
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), updater = null)
    val stacks = WithPanelStacksProps(PanelStackData(leftStack, null), PanelStackData(rightStack, null))

    //when & then
    ViewerPlugin.onKeyTrigger("f3", stacks).map(_ should not be None)
  }
}
