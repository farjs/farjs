package farjs.archiver

import farjs.archiver.zip.ZipApi
import farjs.filelist.api.{FileListDir, FileListItem, MockFileListApi}
import farjs.filelist.stack._
import farjs.filelist.{FileListState, MockFileListActions}
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

class ArchiverPluginSpec extends AsyncTestSpec {

  ArchiverPlugin.readZip = _ => Future.successful(Map.empty)
  ArchiverPlugin.createApi = { // replace potential mock from prev test with default impl
    (zipPath, rootPath, entriesByParentF) =>
      new ZipApi(zipPath, rootPath, entriesByParentF)
  }

  it should "define triggerKeys" in {
    //when & then
    ArchiverPlugin.triggerKeys.toList shouldBe List("S-f7")
  }

  it should "return None if .. when onKeyTrigger" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
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
    ArchiverPlugin.onKeyTrigger("", stacks).map(_ shouldBe None)
  }

  it should "return None if non-local fs when onKeyTrigger" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(MockFileListApi(isLocalMock = false))
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
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
    ArchiverPlugin.onKeyTrigger("", stacks).map(_ shouldBe None)
  }

  it should "return Some(ui) if not .. when onKeyTrigger" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
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
    ArchiverPlugin.onKeyTrigger("", stacks).map(_ should not be None)
  }

  it should "return Some(ui) if selected items when onKeyTrigger" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem.up,
      FileListItem("item 1")
    )), selectedNames = js.Set("item 1"))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(leftState))
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), updater = null)
    val stacks = WithPanelStacksProps(leftStack, null, rightStack, null)

    //when & then
    ArchiverPlugin.onKeyTrigger("", stacks).map(_ should not be None)
  }

  it should "trigger plugin on .zip and .jar file extensions" in {
    //given
    val header = new Uint8Array(5)
    
    //when & then
    Future.sequence(Seq(
      ArchiverPlugin.onFileTrigger("filePath.txt", header, () => ()).map(_ shouldBe None),
      ArchiverPlugin.onFileTrigger("filePath.zip", header, () => ()).map(_ should not be None),
      ArchiverPlugin.onFileTrigger("filePath.ZIP", header, () => ()).map(_ should not be None),
      ArchiverPlugin.onFileTrigger("filePath.jar", header, () => ()).map(_ should not be None),
      ArchiverPlugin.onFileTrigger("filePath.Jar", header, () => ()).map(_ should not be None)
    )).map(_ => Succeeded)
  }

  it should "trigger plugin on PK34 file header" in {
    //given
    val header = new Uint8Array(js.Array[Short]('P', 'K', 0x03, 0x04, 0x01))
    
    //when & then
    Future.sequence(Seq(
      ArchiverPlugin.onFileTrigger("filePath.txt", new Uint8Array(2), () => ()).map(_ shouldBe None),
      ArchiverPlugin.onFileTrigger("filePath.txt", header, () => ()).map(_ should not be None)
    )).map(_ => Succeeded)
  }
}
