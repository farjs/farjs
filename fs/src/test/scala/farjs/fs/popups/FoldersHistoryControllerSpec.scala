package farjs.fs.popups

import farjs.fs.popups.FoldersHistoryController._
import org.scalatest.Succeeded
import scommons.react.ReactClass
import scommons.react.test._

class FoldersHistoryControllerSpec extends TestSpec with TestRendererUtils {

  FoldersHistoryController.foldersHistoryPopup = "FoldersHistoryPopup".asInstanceOf[ReactClass]

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
    inside(findComponents(renderer.root, foldersHistoryPopup)) {
      case List(c) => c.props.asInstanceOf[FoldersHistoryPopupProps].onChangeDir(dir)
    }
  }

  it should "call onClose when onClose" in {
    //given
    val onChangeDir = mockFunction[String, Unit]
    val onClose = mockFunction[Unit]
    val props = FoldersHistoryControllerProps(showPopup = true, onChangeDir, onClose)
    val comp = testRender(<(FoldersHistoryController())(^.plain := props)())
    val popup = inside(findComponents(comp, foldersHistoryPopup)) {
      case List(c) => c.props.asInstanceOf[FoldersHistoryPopupProps]
    }

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
    assertNativeComponent(result, <(foldersHistoryPopup)(^.assertPlain[FoldersHistoryPopupProps](inside(_) {
      case FoldersHistoryPopupProps(_, _) => Succeeded
    }))())
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
