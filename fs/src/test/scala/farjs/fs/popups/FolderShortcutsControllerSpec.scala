package farjs.fs.popups

import farjs.fs.popups.FSPopupsActions._
import farjs.fs.popups.FolderShortcutsController._
import org.scalatest.Succeeded
import scommons.react.test._

class FolderShortcutsControllerSpec extends TestSpec with TestRendererUtils {

  FolderShortcutsController.folderShortcutsPopup = mockUiComponent("FolderShortcutsPopup")

  it should "call onChangeDir when onChangeDir" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onChangeDir = mockFunction[String, Unit]
    val props = FolderShortcutsControllerProps(dispatch, showPopup = true, onChangeDir)
    val renderer = createTestRenderer(<(FolderShortcutsController())(^.wrapped := props)())
    val dir = "test dir"

    //then
    dispatch.expects(FolderShortcutsPopupAction(show = false))
    onChangeDir.expects(dir)

    //when
    findComponentProps(renderer.root, folderShortcutsPopup).onChangeDir(dir)
  }

  it should "dispatch FolderShortcutsPopupAction when onClose" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onChangeDir = mockFunction[String, Unit]
    val props = FolderShortcutsControllerProps(dispatch, showPopup = true, onChangeDir)
    val comp = testRender(<(FolderShortcutsController())(^.wrapped := props)())
    val popup = findComponentProps(comp, folderShortcutsPopup)

    //then
    dispatch.expects(FolderShortcutsPopupAction(show = false))
    onChangeDir.expects(*).never()

    //when
    popup.onClose()
  }

  it should "render popup component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FolderShortcutsControllerProps(dispatch, showPopup = true, _ => ())

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
    val props = FolderShortcutsControllerProps(dispatch, showPopup = false, _ => ())

    //when
    val renderer = createTestRenderer(<(FolderShortcutsController())(^.wrapped := props)())

    //then
    renderer.root.children.toList should be (empty)
  }
}
