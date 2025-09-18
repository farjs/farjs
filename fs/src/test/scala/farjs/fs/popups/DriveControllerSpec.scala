package farjs.fs.popups

import farjs.fs.popups.DriveController._
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js

class DriveControllerSpec extends TestSpec with TestRendererUtils {

  DriveController.drivePopup = "DrivePopup".asInstanceOf[ReactClass]

  it should "call onChangeDir when onChangeDir on the left" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onChangeDir = mockFunction[String, Boolean, Unit]
    val onClose = mockFunction[Unit]
    val props = DriveControllerProps(dispatch, showDrivePopupOnLeft = true, onChangeDir, onClose)
    val renderer = createTestRenderer(<(DriveController())(^.plain := props)())
    val dir = "test dir"

    //then
    onClose.expects()
    onChangeDir.expects(dir, true)

    //when
    inside(findComponents(renderer.root, drivePopup)) {
      case List(c) => c.props.asInstanceOf[DrivePopupProps].onChangeDir(dir)
    }
  }

  it should "call onChangeDir when onChangeDir on the right" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onChangeDir = mockFunction[String, Boolean, Unit]
    val onClose = mockFunction[Unit]
    val props = DriveControllerProps(dispatch, showDrivePopupOnLeft = false, onChangeDir, onClose)
    val renderer = createTestRenderer(<(DriveController())(^.plain := props)())
    val dir = "test dir"

    //then
    onClose.expects()
    onChangeDir.expects(dir, false)

    //when
    inside(findComponents(renderer.root, drivePopup)) {
      case List(c) => c.props.asInstanceOf[DrivePopupProps].onChangeDir(dir)
    }
  }

  it should "call onClose when onClose" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onChangeDir = mockFunction[String, Boolean, Unit]
    val onClose = mockFunction[Unit]
    val props = DriveControllerProps(dispatch, showDrivePopupOnLeft = true, onChangeDir, onClose)
    val comp = testRender(<(DriveController())(^.plain := props)())
    val popup = inside(findComponents(comp, drivePopup)) {
      case List(c) => c.props.asInstanceOf[DrivePopupProps]
    }

    //then
    onClose.expects()
    onChangeDir.expects(*, *).never()

    //when
    popup.onClose()
  }

  it should "render popup on the left" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val props = DriveControllerProps(dispatch, showDrivePopupOnLeft = true, (_, _) => (), () => ())

    //when
    val result = testRender(<(DriveController())(^.plain := props)())

    //then
    assertNativeComponent(result, <(drivePopup)(^.assertPlain[DrivePopupProps](inside(_) {
      case DrivePopupProps(dispatch, _, _, showOnLeft) =>
        dispatch shouldBe props.dispatch
        showOnLeft shouldBe true
    }))())
  }

  it should "render popup on the right" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val props = DriveControllerProps(dispatch, showDrivePopupOnLeft = false, (_, _) => (), () => ())

    //when
    val result = testRender(<(DriveController())(^.plain := props)())

    //then
    assertNativeComponent(result, <(drivePopup)(^.assertPlain[DrivePopupProps](inside(_) {
      case DrivePopupProps(dispatch, _, _, showOnLeft) =>
        dispatch shouldBe props.dispatch
        showOnLeft shouldBe false
    }))())
  }

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val props = DriveControllerProps(dispatch, showDrivePopupOnLeft = js.undefined, (_, _) => (), () => ())

    //when
    val renderer = createTestRenderer(<(DriveController())(^.plain := props)())

    //then
    renderer.root.children.toList should be (empty)
  }
}
