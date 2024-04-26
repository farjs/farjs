package farjs.fs.popups

import farjs.fs.popups.DriveController._
import scommons.react.test._

import scala.scalajs.js

class DriveControllerSpec extends TestSpec with TestRendererUtils {

  DriveController.drivePopup = mockUiComponent("DrivePopup")

  it should "call onChangeDir when onChangeDir on the left" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onChangeDir = mockFunction[String, Boolean, Unit]
    val onClose = mockFunction[Unit]
    val props = DriveControllerProps(dispatch, showDrivePopupOnLeft = Some(true), onChangeDir, onClose)
    val renderer = createTestRenderer(<(DriveController())(^.wrapped := props)())
    val dir = "test dir"

    //then
    onClose.expects()
    onChangeDir.expects(dir, true)

    //when
    findComponentProps(renderer.root, drivePopup).onChangeDir(dir)
  }

  it should "call onChangeDir when onChangeDir on the right" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onChangeDir = mockFunction[String, Boolean, Unit]
    val onClose = mockFunction[Unit]
    val props = DriveControllerProps(dispatch, showDrivePopupOnLeft = Some(false), onChangeDir, onClose)
    val renderer = createTestRenderer(<(DriveController())(^.wrapped := props)())
    val dir = "test dir"

    //then
    onClose.expects()
    onChangeDir.expects(dir, false)

    //when
    findComponentProps(renderer.root, drivePopup).onChangeDir(dir)
  }

  it should "call onClose when onClose" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onChangeDir = mockFunction[String, Boolean, Unit]
    val onClose = mockFunction[Unit]
    val props = DriveControllerProps(dispatch, showDrivePopupOnLeft = Some(true), onChangeDir, onClose)
    val comp = testRender(<(DriveController())(^.wrapped := props)())
    val popup = findComponentProps(comp, drivePopup)

    //then
    onClose.expects()
    onChangeDir.expects(*, *).never()

    //when
    popup.onClose()
  }

  it should "render popup on the left" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val props = DriveControllerProps(dispatch, showDrivePopupOnLeft = Some(true), (_, _) => (), () => ())

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
    val dispatch = mockFunction[js.Any, Unit]
    val props = DriveControllerProps(dispatch, showDrivePopupOnLeft = Some(false), (_, _) => (), () => ())

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
    val dispatch = mockFunction[js.Any, Unit]
    val props = DriveControllerProps(dispatch, showDrivePopupOnLeft = None, (_, _) => (), () => ())

    //when
    val renderer = createTestRenderer(<(DriveController())(^.wrapped := props)())

    //then
    renderer.root.children.toList should be (empty)
  }
}
