package farjs.app.filelist.fs.popups

import farjs.app.filelist.fs.popups.DriveController._
import farjs.app.filelist.fs.popups.FSPopupsActions._
import scommons.react.test._

class DriveControllerSpec extends TestSpec with TestRendererUtils {

  DriveController.drivePopup = mockUiComponent("DrivePopup")

  it should "call onChangeDir when onChangeDir on the left" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onChangeDir = mockFunction[String, Boolean, Unit]
    val props = DriveControllerProps(dispatch, show = ShowDriveOnLeft, onChangeDir)
    val renderer = createTestRenderer(<(DriveController())(^.wrapped := props)())
    val dir = "test dir"

    //then
    dispatch.expects(DrivePopupAction(show = DrivePopupHidden))
    onChangeDir.expects(dir, true)

    //when
    findComponentProps(renderer.root, drivePopup).onChangeDir(dir)
  }

  it should "call onChangeDir when onChangeDir on the right" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onChangeDir = mockFunction[String, Boolean, Unit]
    val props = DriveControllerProps(dispatch, show = ShowDriveOnRight, onChangeDir)
    val renderer = createTestRenderer(<(DriveController())(^.wrapped := props)())
    val dir = "test dir"

    //then
    dispatch.expects(DrivePopupAction(show = DrivePopupHidden))
    onChangeDir.expects(dir, false)

    //when
    findComponentProps(renderer.root, drivePopup).onChangeDir(dir)
  }

  it should "dispatch DrivePopupAction when onClose" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onChangeDir = mockFunction[String, Boolean, Unit]
    val props = DriveControllerProps(dispatch, show = ShowDriveOnLeft, onChangeDir)
    val comp = testRender(<(DriveController())(^.wrapped := props)())
    val popup = findComponentProps(comp, drivePopup)

    //then
    dispatch.expects(DrivePopupAction(show = DrivePopupHidden))
    onChangeDir.expects(*, *).never()

    //when
    popup.onClose()
  }

  it should "render popup on the left" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = DriveControllerProps(dispatch, show = ShowDriveOnLeft, (_, _) => ())

    //when
    val result = testRender(<(DriveController())(^.wrapped := props)())

    //then
    assertTestComponent(result, drivePopup) {
      case DrivePopupProps(dispatch, _, _, showOnLeft) =>
        dispatch shouldBe props.dispatch
        showOnLeft shouldBe true
    }
  }

  it should "render popup on the right" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = DriveControllerProps(dispatch, show = ShowDriveOnRight, (_, _) => ())

    //when
    val result = testRender(<(DriveController())(^.wrapped := props)())

    //then
    assertTestComponent(result, drivePopup) {
      case DrivePopupProps(dispatch, _, _, showOnLeft) =>
        dispatch shouldBe props.dispatch
        showOnLeft shouldBe false
    }
  }

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = DriveControllerProps(dispatch, show = DrivePopupHidden, (_, _) => ())

    //when
    val renderer = createTestRenderer(<(DriveController())(^.wrapped := props)())

    //then
    renderer.root.children.toList should be (empty)
  }
}
