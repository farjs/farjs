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

  ArchiverPlugin.readZip = _ => js.Promise.resolve[js.Map[String, js.Array[FileListItem]]](new js.Map[String, js.Array[FileListItem]]())
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
    val stacks = WithStacksProps(WithStacksData(leftStack, null), WithStacksData(rightStack, null))

    //when & then
    ArchiverPlugin.onKeyTrigger("", stacks).toFuture.map(_ shouldBe js.undefined)
  }

  it should "return None if non-local fs when onKeyTrigger" in {
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
    val stacks = WithStacksProps(WithStacksData(leftStack, null), WithStacksData(rightStack, null))

    //when & then
    ArchiverPlugin.onKeyTrigger("", stacks).toFuture.map(_ shouldBe js.undefined)
  }

  it should "return Some(ui) if not .. when onKeyTrigger" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem("item 1")
    )))
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, actions, leftState)
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), updater = null)
    val stacks = WithStacksProps(WithStacksData(leftStack, null), WithStacksData(rightStack, null))

    //when & then
    ArchiverPlugin.onKeyTrigger("", stacks).toFuture.map(_ should not be js.undefined)
  }

  it should "return Some(ui) if selected items when onKeyTrigger" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem.up,
      FileListItem("item 1")
    )), selectedNames = js.Set("item 1"))
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, actions, leftState)
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), updater = null)
    val stacks = WithStacksProps(WithStacksData(leftStack, null), WithStacksData(rightStack, null))

    //when & then
    ArchiverPlugin.onKeyTrigger("", stacks).toFuture.map(_ should not be None)
  }

  it should "trigger plugin on .zip and .jar file extensions" in {
    //given
    val header = new Uint8Array(5)
    
    //when & then
    Future.sequence(Seq(
      ArchiverPlugin.onFileTrigger("filePath.txt", header, () => ()).toFuture.map(_ shouldBe js.undefined),
      ArchiverPlugin.onFileTrigger("filePath.zip", header, () => ()).toFuture.map(_ should not be js.undefined),
      ArchiverPlugin.onFileTrigger("filePath.ZIP", header, () => ()).toFuture.map(_ should not be js.undefined),
      ArchiverPlugin.onFileTrigger("filePath.jar", header, () => ()).toFuture.map(_ should not be js.undefined),
      ArchiverPlugin.onFileTrigger("filePath.Jar", header, () => ()).toFuture.map(_ should not be js.undefined)
    )).map(_ => Succeeded)
  }

  it should "trigger plugin on PK34 file header" in {
    //given
    val header = new Uint8Array(js.Array[Short]('P', 'K', 0x03, 0x04, 0x01))
    
    //when & then
    Future.sequence(Seq(
      ArchiverPlugin.onFileTrigger("filePath.txt", new Uint8Array(2), () => ()).toFuture.map(_ shouldBe js.undefined),
      ArchiverPlugin.onFileTrigger("filePath.txt", header, () => ()).toFuture.map(_ should not be js.undefined)
    )).map(_ => Succeeded)
  }
}
