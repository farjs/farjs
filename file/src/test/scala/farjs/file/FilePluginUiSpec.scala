package farjs.file

import farjs.file.FilePluginUi._
import farjs.file.popups.FileViewHistoryControllerProps
import farjs.filelist.FileListPluginUiProps
import scommons.react.test._

import scala.scalajs.js

class FilePluginUiSpec extends TestSpec with TestRendererUtils {

  FilePluginUi.fileViewHistory = mockUiComponent("FileViewHistoryController")

  it should "render component" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onClose: js.Function0[Unit] = mockFunction[Unit]
    val fsPluginUi = new FilePluginUi()

    //when
    val result = createTestRenderer(
      <(fsPluginUi())(^.plain := FileListPluginUiProps(dispatch, onClose))()
    ).root

    //then
    assertComponents(result.children, List(
      <(fileViewHistory())(^.assertWrapped(inside(_) {
        case FileViewHistoryControllerProps(showPopup, resOnClose) =>
          showPopup shouldBe false
          resOnClose should be theSameInstanceAs onClose
      }))()
    ))
  }
}
