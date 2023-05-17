package farjs.file

import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack._
import farjs.filelist.{FileListState, MockFileListActions}
import scommons.nodejs.test.TestSpec
import scommons.react.ReactClass

class FilePluginSpec extends TestSpec {

  it should "define triggerKeys" in {
    //when & then
    FilePlugin.triggerKeys.toList shouldBe List("M-v")
  }

  it should "return None/Some if non-/trigger key when onKeyTrigger" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem("item 1")
    )))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(state))
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(state))
    ), updater = null)
    val stacks = WithPanelStacksProps(leftStack, null, rightStack, null)

    //when & then
    FilePlugin.onKeyTrigger("test_key", stacks) shouldBe None
    FilePlugin.onKeyTrigger("M-v", stacks) should not be None
  }

  it should "return Some(ui) if trigger key when createUi" in {
    //when & then
    inside(FilePlugin.createUi("M-v")) { case Some(FilePluginUi(true)) => }
  }
}
