package farjs.app.filelist.fs.popups

import farjs.app.filelist.fs.popups.FSPopups._
import farjs.filelist.stack.WithPanelStacksSpec.withContext
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import scommons.react.ReactClass
import scommons.react.test._

class FSPopupsSpec extends TestSpec with TestRendererUtils {

  FSPopups.folderShortcuts = mockUiComponent("FolderShortcutsController")

  it should "render component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FSPopupsProps(dispatch, FSPopupsState())
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), None, None)
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    //when
    val result = createTestRenderer(
      withContext(<(FSPopups())(^.wrapped := props)(), leftStack, rightStack)
    ).root

    //then
    assertComponents(result.children, List(
      <(folderShortcuts())(^.wrapped := props)()
    ))
  }
}
