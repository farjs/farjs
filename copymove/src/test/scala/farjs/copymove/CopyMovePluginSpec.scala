package farjs.copymove

import farjs.copymove.CopyMoveUiAction._
import farjs.filelist._
import farjs.filelist.api._
import farjs.filelist.stack._
import org.scalactic.source.Position
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.blessed.BlessedElement

import scala.concurrent.Future
import scala.scalajs.js

class CopyMovePluginSpec extends AsyncTestSpec {

  it should "define triggerKeys" in {
    //when & then
    CopyMovePlugin.triggerKeys.toList shouldBe List("f5", "f6", "S-f5", "S-f6")
  }

  it should "return None if .. when onKeyTrigger" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(capabilitiesMock = Set(
      FileListCapability.read,
      FileListCapability.write,
      FileListCapability.delete,
      FileListCapability.copyInplace,
      FileListCapability.moveInplace
    ))
    val state = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem.up,
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
      CopyMovePlugin.onKeyTrigger("f5", stacks).map(_ shouldBe None),
      CopyMovePlugin.onKeyTrigger("f6", stacks).map(_ shouldBe None),
      CopyMovePlugin.onKeyTrigger("S-f5", stacks).map(_ shouldBe None),
      CopyMovePlugin.onKeyTrigger("S-f6", stacks).map(_ shouldBe None)
    )).map(_ => Succeeded)
  }

  it should "return None if other state type when onKeyTrigger" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(capabilitiesMock = Set(
      FileListCapability.read,
      FileListCapability.write,
      FileListCapability.delete,
      FileListCapability.copyInplace,
      FileListCapability.moveInplace
    ))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some("otherState"))
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some("otherState"))
    ), updater = null)
    val stacks = WithPanelStacksProps(leftStack, null, rightStack, null)

    //when & then
    Future.sequence(Seq(
      CopyMovePlugin.onKeyTrigger("f5", stacks).map(_ shouldBe None),
      CopyMovePlugin.onKeyTrigger("f6", stacks).map(_ shouldBe None),
      CopyMovePlugin.onKeyTrigger("S-f5", stacks).map(_ shouldBe None),
      CopyMovePlugin.onKeyTrigger("S-f6", stacks).map(_ shouldBe None)
    )).map(_ => Succeeded)
  }

  it should "return None when onKeyTrigger(unknown)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(capabilitiesMock = Set(
      FileListCapability.read,
      FileListCapability.write,
      FileListCapability.delete,
      FileListCapability.copyInplace,
      FileListCapability.moveInplace
    ))
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
    CopyMovePlugin.onKeyTrigger("unknown", stacks).map(_ shouldBe None)
  }

  it should "return Some(ui) when onKeyTrigger(Shift-F5)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(capabilitiesMock = Set(FileListCapability.copyInplace))
    val leftState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem("item 1")
    )))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(leftState))
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), updater = null)
    val stacks = WithPanelStacksProps(leftStack, null, rightStack, null)

    //when & then
    CopyMovePlugin.onKeyTrigger("S-f5", stacks).map(_ should not be None)
  }

  it should "return Some(ui) when onKeyTrigger(F5)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(capabilitiesMock = Set(
      FileListCapability.read, FileListCapability.write, FileListCapability.delete
    ))
    val state = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem("item 1")
    )))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(state))
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass],Some(dispatch), Some(actions), Some(state))
    ), updater = null)
    val stacks = WithPanelStacksProps(leftStack, null, rightStack, null)

    //when & then
    CopyMovePlugin.onKeyTrigger("f5", stacks).map(_ should not be None)
  }

  it should "return Some(ui) if selected items when onKeyTrigger(F6)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(capabilitiesMock = Set(
      FileListCapability.read, FileListCapability.write, FileListCapability.delete
    ))
    val state = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem.up,
      FileListItem("item 1")
    )), selectedNames = Set("item 1"))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(state))
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(state))
    ), updater = null)
    val stacks = WithPanelStacksProps(leftStack, null, rightStack, null)

    //when & then
    CopyMovePlugin.onKeyTrigger("f6", stacks).map(_ should not be None)
  }

  it should "return CopyMoveUiAction when onCopyMoveInplace" in {
    //given
    val currState = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("file 1"),
        FileListItem("dir 1", isDir = true)
      ))
    )
    val dispatch = mockFunction[Any, Any]
    val capabilities = Set(FileListCapability.copyInplace, FileListCapability.moveInplace)

    def check(fullKey: String,
              action: CopyMoveUiAction,
              index: Int = 0,
              selectedNames: Set[String] = Set.empty,
              never: Boolean = false,
              capabilities: Set[String] = capabilities)(implicit pos: Position): Unit = {
      //given
      val actions = new MockFileListActions(capabilitiesMock = capabilities)
      val from = FileListData(dispatch, actions, currState.copy(
        index = index,
        selectedNames = selectedNames
      ))

      //when
      val res = CopyMovePlugin.onCopyMoveInplace(fullKey == "S-f6", from)

      //then
      if (!never) res shouldBe Some(action)
      else res shouldBe None
    }

    //when & then
    check("S-f5", ShowCopyInplace, never = true)
    check("S-f5", ShowCopyInplace, never = true, selectedNames = Set("file 1"))
    check("S-f5", ShowCopyInplace, index = 1, never = true, capabilities = Set(FileListCapability.moveInplace))
    check("S-f5", ShowCopyInplace, index = 1, capabilities = Set(FileListCapability.copyInplace))
    check("S-f5", ShowCopyInplace, index = 2)

    //when & then
    check("S-f6", ShowMoveInplace, never = true)
    check("S-f6", ShowMoveInplace, never = true, selectedNames = Set("file 1"))
    check("S-f6", ShowMoveInplace, index = 1, never = true, capabilities = Set(FileListCapability.copyInplace))
    check("S-f6", ShowMoveInplace, index = 1, capabilities = Set(FileListCapability.moveInplace))
    check("S-f6", ShowMoveInplace, index = 2)
    
    Succeeded
  }

  it should "return CopyMoveUiAction or emit FileListEvent when onCopyMove" in {
    //given
    val emitMock = mockFunction[String, js.Any, js.Dynamic, Boolean]
    val toButtonMock = js.Dynamic.literal("emit" -> emitMock)

    val currState = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("file 1"),
        FileListItem("dir 1", isDir = true)
      ))
    )
    val dispatch = mockFunction[Any, Any]
    val capabilities = Set(
      FileListCapability.read,
      FileListCapability.write,
      FileListCapability.delete
    )

    def check(fullKey: String,
              action: CopyMoveUiAction,
              index: Int = 0,
              selectedNames: Set[String] = Set.empty,
              never: Boolean = false,
              fromCapabilities: Set[String] = capabilities,
              toCapabilities: Set[String] = capabilities,
              emit: Option[String] = None)(implicit pos: Position): Unit = {
      //given
      val fromActions = new MockFileListActions(capabilitiesMock = fromCapabilities)
      val toActions = new MockFileListActions(capabilitiesMock = toCapabilities)
      val from = FileListData(dispatch, fromActions, currState.copy(
        index = index,
        selectedNames = selectedNames
      ))
      val to = FileListData(dispatch, toActions, currState)

      //then
      emit.foreach { event =>
        emitMock.expects("keypress", *, *).onCall { (_, _, key) =>
          key.name shouldBe ""
          key.full shouldBe event
          false
        }
      }

      //when
      val res = CopyMovePlugin.onCopyMove(fullKey == "f6", from, to, toButtonMock.asInstanceOf[BlessedElement])

      //then
      if (!never) res shouldBe Some(action)
      else res shouldBe None
    }

    //when & then
    check("f5", ShowCopyToTarget, never = true)
    check("f5", ShowCopyToTarget, index = 1, never = true, fromCapabilities = Set.empty)
    check("f5", ShowCopyToTarget, index = 1, never = true, toCapabilities = Set.empty,
      emit = Some(FileListEvent.onFileListCopy))
    check("f5", ShowCopyToTarget, index = 1, fromCapabilities = Set(
      FileListCapability.read
    ))
    check("f5", ShowCopyToTarget, index = 2)
    check("f5", ShowCopyToTarget, selectedNames = Set("file 1"))

    //when & then
    check("f6", ShowMoveToTarget, never = true)
    check("f6", ShowMoveToTarget, index = 1, never = true, fromCapabilities = Set(
      FileListCapability.read
    ))
    check("f6", ShowMoveToTarget, index = 1, never = true, toCapabilities = Set.empty,
      emit = Some(FileListEvent.onFileListMove))
    check("f6", ShowMoveToTarget, index = 1)
    check("f6", ShowMoveToTarget, index = 2)
    check("f6", ShowMoveToTarget, selectedNames = Set("file 1"))
    
    Succeeded
  }
}
