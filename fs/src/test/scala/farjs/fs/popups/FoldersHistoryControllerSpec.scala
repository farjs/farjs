package farjs.fs.popups

import farjs.fs.popups.FoldersHistoryController._
import org.scalatest.Succeeded
import scommons.react.test._

class FoldersHistoryControllerSpec extends TestSpec with TestRendererUtils {

  FoldersHistoryController.foldersHistoryPopup = mockUiComponent("FoldersHistoryPopup")

  it should "call onChangeDir when onChangeDir" in {
    //given
    val onChangeDir = mockFunction[String, Unit]
    val onClose = mockFunction[Unit]
    val props = FoldersHistoryControllerProps(showPopup = true, onChangeDir, onClose)
    val renderer = createTestRenderer(<(FoldersHistoryController())(^.plain := props)())
    val dir = "test dir"

    //then
    onClose.expects()
    onChangeDir.expects(dir)

    //when
    findComponentProps(renderer.root, foldersHistoryPopup, plain = true).onChangeDir(dir)
  }

  it should "call onClose when onClose" in {
    //given
    val onChangeDir = mockFunction[String, Unit]
    val onClose = mockFunction[Unit]
    val props = FoldersHistoryControllerProps(showPopup = true, onChangeDir, onClose)
    val comp = testRender(<(FoldersHistoryController())(^.plain := props)())
    val popup = findComponentProps(comp, foldersHistoryPopup, plain = true)

    //then
    onClose.expects()
    onChangeDir.expects(*).never()

    //when
    popup.onClose()
  }

  it should "render popup component" in {
    //given
    val props = FoldersHistoryControllerProps(showPopup = true, _ => (), () => ())

    //when
    val result = testRender(<(FoldersHistoryController())(^.plain := props)())

    //then
    assertTestComponent(result, foldersHistoryPopup, plain = true) {
      case FoldersHistoryPopupProps(_, _) => Succeeded
    }
  }

  it should "render empty component" in {
    //given
    val props = FoldersHistoryControllerProps(showPopup = false, _ => (), () => ())

    //when
    val renderer = createTestRenderer(<(FoldersHistoryController())(^.plain := props)())

    //then
    renderer.root.children.toList should be (empty)
  }
}
