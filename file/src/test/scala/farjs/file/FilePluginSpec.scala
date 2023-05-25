package farjs.file

import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack._
import farjs.filelist.{FileListState, MockFileListActions}
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass

import scala.concurrent.Future

class FilePluginSpec extends AsyncTestSpec {

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
    Future.sequence(Seq(
      FilePlugin.onKeyTrigger("test_key", stacks).map(_ shouldBe None),
      FilePlugin.onKeyTrigger("M-v", stacks).map(_ should not be None)
    )).map(_ => Succeeded)
  }

  it should "return Some(ui) if trigger key when createUi" in {
    //when & then
    inside(FilePlugin.createUi("M-v")) { case Some(FilePluginUi(true)) => }
    Succeeded
  }
}
