package farjs.app.filelist.fs.popups

import farjs.app.filelist.fs.popups.FSPopupsActions._
import farjs.app.filelist.fs.popups.FoldersHistoryController._
import org.scalatest.Succeeded
import scommons.react.test._

class FoldersHistoryControllerSpec extends TestSpec with TestRendererUtils {

  FoldersHistoryController.foldersHistoryPopup = mockUiComponent("FoldersHistoryPopup")

  it should "call onChangeDir when onChangeDir" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onChangeDir = mockFunction[String, Unit]
    val props = FoldersHistoryControllerProps(dispatch, showPopup = true, onChangeDir)
    val renderer = createTestRenderer(<(FoldersHistoryController())(^.wrapped := props)())
    val dir = "test dir"

    //then
    dispatch.expects(FoldersHistoryPopupAction(show = false))
    onChangeDir.expects(dir)

    //when
    findComponentProps(renderer.root, foldersHistoryPopup).onChangeDir(dir)
  }

  it should "dispatch FoldersHistoryPopupAction when onClose" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onChangeDir = mockFunction[String, Unit]
    val props = FoldersHistoryControllerProps(dispatch, showPopup = true, onChangeDir)
    val comp = testRender(<(FoldersHistoryController())(^.wrapped := props)())
    val popup = findComponentProps(comp, foldersHistoryPopup)

    //then
    dispatch.expects(FoldersHistoryPopupAction(show = false))
    onChangeDir.expects(*).never()

    //when
    popup.onClose()
  }

  it should "render popup component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FoldersHistoryControllerProps(dispatch, showPopup = true, _ => ())

    //when
    val result = testRender(<(FoldersHistoryController())(^.wrapped := props)())

    //then
    assertTestComponent(result, foldersHistoryPopup) {
      case FoldersHistoryPopupProps(_, _) => Succeeded
    }
  }

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FoldersHistoryControllerProps(dispatch, showPopup = false, _ => ())

    //when
    val renderer = createTestRenderer(<(FoldersHistoryController())(^.wrapped := props)())

    //then
    renderer.root.children.toList should be (empty)
  }
}
