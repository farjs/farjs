package farjs.app.filelist.fs.popups

import farjs.app.filelist.fs.popups.FSPopupsActions._
import farjs.app.filelist.fs.popups.FolderShortcutsController._
import org.scalatest.Succeeded
import scommons.react.test._

class FolderShortcutsControllerSpec extends TestSpec with TestRendererUtils {

  FolderShortcutsController.folderShortcutsPopup = mockUiComponent("FolderShortcutsPopup")

  it should "change dir when onChangeDir" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FSPopupsProps(dispatch, FSPopupsState(showFolderShortcutsPopup = true))
    val renderer = createTestRenderer(<(FolderShortcutsController())(^.wrapped := props)())
    val dir = "test dir"

    //then
    dispatch.expects(FolderShortcutsPopupAction(show = false))

    //when
    findComponentProps(renderer.root, folderShortcutsPopup).onChangeDir(dir)
  }

  it should "dispatch FolderShortcutsPopupAction when onClose" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FSPopupsProps(dispatch, FSPopupsState(showFolderShortcutsPopup = true))
    val comp = testRender(<(FolderShortcutsController())(^.wrapped := props)())
    val popup = findComponentProps(comp, folderShortcutsPopup)

    //then
    dispatch.expects(FolderShortcutsPopupAction(show = false))

    //when
    popup.onClose()
  }

  it should "render popup component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FSPopupsProps(dispatch, FSPopupsState(showFolderShortcutsPopup = true))

    //when
    val result = testRender(<(FolderShortcutsController())(^.wrapped := props)())

    //then
    assertTestComponent(result, folderShortcutsPopup) {
      case FolderShortcutsPopupProps(_, _) => Succeeded
    }
  }

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FSPopupsProps(dispatch, FSPopupsState())

    //when
    val renderer = createTestRenderer(<(FolderShortcutsController())(^.wrapped := props)())

    //then
    renderer.root.children.toList should be (empty)
  }
}
