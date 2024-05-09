package farjs.file.popups

import farjs.file.popups.FileViewHistoryController._
import farjs.file.{FileEvent, FileViewHistory}
import farjs.filelist.stack.WithPanelStacksSpec.withContext
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import org.scalatest.Succeeded
import scommons.react.ReactClass
import scommons.react.blessed.BlessedElement
import scommons.react.test._

import scala.scalajs.js

class FileViewHistoryControllerSpec extends TestSpec with TestRendererUtils {

  FileViewHistoryController.fileViewHistoryPopup = mockUiComponent("FileViewHistoryPopup")

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
      <(FileViewHistoryController())(^.wrapped := props)(),
      leftStack = currStack,
      rightStack = otherStack,
      leftInput = inputElementMock.asInstanceOf[BlessedElement]
    ))
    val history = FileViewHistory(
      path = "test/path",
      isEdit = false,
      encoding = "utf8",
      position = 0,
      wrap = None,
      column = None
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
    findComponentProps(renderer.root, fileViewHistoryPopup).onAction(history)
  }

  it should "call onClose when onClose" in {
    //given
    val onClose = mockFunction[Unit]
    val props = FileViewHistoryControllerProps(showPopup = true, onClose)
    val comp = testRender(withContext(
      <(FileViewHistoryController())(^.wrapped := props)(),
      leftStack = currStack,
      rightStack = otherStack
    ))
    val popup = findComponentProps(comp, fileViewHistoryPopup)

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
      <(FileViewHistoryController())(^.wrapped := props)(),
      leftStack = currStack,
      rightStack = otherStack
    ))

    //then
    assertTestComponent(result, fileViewHistoryPopup) {
      case FileViewHistoryPopupProps(_, _) => Succeeded
    }
  }

  it should "render empty component" in {
    //given
    val props = FileViewHistoryControllerProps(showPopup = false, () => ())

    //when
    val renderer = createTestRenderer(withContext(
      <(FileViewHistoryController())(^.wrapped := props)(),
      leftStack = currStack,
      rightStack = otherStack
    ))

    //then
    renderer.root.children.toList should be (empty)
  }
}
