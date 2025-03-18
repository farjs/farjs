package farjs.fs.popups

import farjs.fs.popups.FolderShortcutsController._
import org.scalatest.Succeeded
import scommons.react.test._

class FolderShortcutsControllerSpec extends TestSpec with TestRendererUtils {

  FolderShortcutsController.folderShortcutsPopup = mockUiComponent("FolderShortcutsPopup")

  it should "call onChangeDir when onChangeDir" in {
    //given
    val onChangeDir = mockFunction[String, Unit]
    val onClose = mockFunction[Unit]
    val props = FolderShortcutsControllerProps(showPopup = true, onChangeDir, onClose)
    val renderer = createTestRenderer(<(FolderShortcutsController())(^.plain := props)())
    val dir = "test dir"

    //then
    onClose.expects()
    onChangeDir.expects(dir)

    //when
    findComponentProps(renderer.root, folderShortcutsPopup).onChangeDir(dir)
  }

  it should "call onClose when onClose" in {
    //given
    val onChangeDir = mockFunction[String, Unit]
    val onClose = mockFunction[Unit]
    val props = FolderShortcutsControllerProps(showPopup = true, onChangeDir, onClose)
    val comp = testRender(<(FolderShortcutsController())(^.plain := props)())
    val popup = findComponentProps(comp, folderShortcutsPopup)

    //then
    onClose.expects()
    onChangeDir.expects(*).never()

    //when
    popup.onClose()
  }

  it should "render popup component" in {
    //given
    val props = FolderShortcutsControllerProps(showPopup = true, _ => (), () => ())

    //when
    val result = testRender(<(FolderShortcutsController())(^.plain := props)())

    //then
    assertTestComponent(result, folderShortcutsPopup) {
      case FolderShortcutsPopupProps(_, _) => Succeeded
    }
  }

  it should "render empty component" in {
    //given
    val props = FolderShortcutsControllerProps(showPopup = false, _ => (), () => ())

    //when
    val renderer = createTestRenderer(<(FolderShortcutsController())(^.plain := props)())

    //then
    renderer.root.children.toList should be (empty)
  }
}
