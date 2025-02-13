package farjs.file.popups

import farjs.file.popups.FileViewHistoryController._
import farjs.file.{FileEvent, FileViewHistory, FileViewHistoryParams}
import farjs.filelist.stack.WithStacksSpec.withContext
import farjs.filelist.stack.{PanelStack, WithStacksData, PanelStackItem}
import org.scalatest.Succeeded
import scommons.react.ReactClass
import scommons.react.blessed.BlessedElement
import scommons.react.test._

import scala.scalajs.js

class FileViewHistoryControllerSpec extends TestSpec with TestRendererUtils {

  FileViewHistoryController.fileViewHistoryPopup = "FileViewHistoryPopup".asInstanceOf[ReactClass]

  private val currStack = new PanelStack(isActive = true, js.Array(
    PanelStackItem("fsComp".asInstanceOf[ReactClass])
  ), updater = null)

  private val otherStack = new PanelStack(isActive = false, js.Array(
    PanelStackItem("fsComp".asInstanceOf[ReactClass])
  ), updater = null)

  it should "emit onFileView event when onAction" in {
    //given
    val emitMock = mockFunction[String, js.Any, js.Dynamic, Boolean]
    val inputElementMock = js.Dynamic.literal("emit" -> emitMock)
    val onClose = mockFunction[Unit]
    val props = FileViewHistoryControllerProps(showPopup = true, onClose)
    val renderer = createTestRenderer(withContext(
      <(FileViewHistoryController())(^.plain := props)(),
      left = WithStacksData(currStack, inputElementMock.asInstanceOf[BlessedElement]),
      right = WithStacksData(otherStack, null)
    ))
    val history = FileViewHistory(
      path = "test/path",
      params = FileViewHistoryParams(
        isEdit = false,
        encoding = "utf8",
        position = 0,
        wrap = js.undefined,
        column = js.undefined
      )
    )

    //then
    emitMock.expects("keypress", *, *).onCall { (_, _, key) =>
      key.name shouldBe ""
      key.full shouldBe FileEvent.onFileView
      key.data shouldBe history
      false
    }
    onClose.expects()

    //when
    inside(findComponents(renderer.root, fileViewHistoryPopup)) {
      case List(c) => c.props.asInstanceOf[FileViewHistoryPopupProps].onAction(history)
    }
  }

  it should "call onClose when onClose" in {
    //given
    val onClose = mockFunction[Unit]
    val props = FileViewHistoryControllerProps(showPopup = true, onClose)
    val comp = testRender(withContext(
      <(FileViewHistoryController())(^.plain := props)(),
      left = WithStacksData(currStack, null),
      right = WithStacksData(otherStack, null)
    ))
    val popup = inside(findComponents(comp, fileViewHistoryPopup)) {
      case List(c) => c.props.asInstanceOf[FileViewHistoryPopupProps]
    }

    //then
    onClose.expects()

    //when
    popup.onClose()
  }

  it should "render popup component" in {
    //given
    val props = FileViewHistoryControllerProps(showPopup = true, () => ())

    //when
    val result = testRender(withContext(
      <(FileViewHistoryController())(^.plain := props)(),
      left = WithStacksData(currStack, null),
      right = WithStacksData(otherStack, null)
    ))

    //then
    assertNativeComponent(result, <(fileViewHistoryPopup)(^.assertPlain[FileViewHistoryPopupProps](inside(_) {
      case FileViewHistoryPopupProps(_, _) => Succeeded
    }))())
  }

  it should "render empty component" in {
    //given
    val props = FileViewHistoryControllerProps(showPopup = false, () => ())

    //when
    val renderer = createTestRenderer(withContext(
      <(FileViewHistoryController())(^.plain := props)(),
      left = WithStacksData(currStack, null),
      right = WithStacksData(otherStack, null)
    ))

    //then
    renderer.root.children.toList should be (empty)
  }
}
