package farjs.app.filelist.fs

import farjs.app.filelist.fs.FSFoldersHistory._
import farjs.filelist.FileListServicesSpec.withServicesContext
import farjs.filelist.history.MockFileListHistoryService
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

import scala.concurrent.Future

class FSFoldersHistorySpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FSFoldersHistory.fsFoldersPopup = mockUiComponent("FSFoldersPopup")

  //noinspection TypeAnnotation
  class HistoryService {
    val save = mockFunction[String, Future[Unit]]

    val service = new MockFileListHistoryService(
      saveMock = save
    )
  }

  it should "render popup component" in {
    //given
    val props = getFSFoldersHistoryProps(showPopup = true, "")
    val historyService = new HistoryService
    historyService.save.expects(props.currDirPath).never()
    
    //when & then
    val renderer = createTestRenderer(withServicesContext(
      <(FSFoldersHistory())(^.wrapped := props)(), historyService.service
    ))
    assertComponents(renderer.root.children, List(
      <(fsFoldersPopup())(^.assertWrapped(inside(_) {
        case FSFoldersPopupProps(onChangeDir, onClose) =>
          onChangeDir should be theSameInstanceAs props.onChangeDir
          onClose should be theSameInstanceAs props.onHidePopup
      }))()
    ))

    //when & then
    historyService.save.expects("dir 1").returning(Future.unit)
    TestRenderer.act { () =>
      renderer.update(withServicesContext(
        <(FSFoldersHistory())(^.wrapped := props.copy(currDirPath = "dir 1"))(), historyService.service
      ))
    }
    assertComponents(renderer.root.children, List(
      <(fsFoldersPopup())(^.assertWrapped(inside(_) {
        case FSFoldersPopupProps(onChangeDir, onClose) =>
          onChangeDir should be theSameInstanceAs props.onChangeDir
          onClose should be theSameInstanceAs props.onHidePopup
      }))()
    ))
  }

  it should "render empty component" in {
    //given
    val props = getFSFoldersHistoryProps(showPopup = false)
    val historyService = new HistoryService
    historyService.save.expects(props.currDirPath).returning(Future.unit)
    
    //when
    val result = createTestRenderer(withServicesContext(
      <(FSFoldersHistory())(^.wrapped := props)(), historyService.service
    )).root

    //then
    result.children.toList should be (empty)
  }
  
  private def getFSFoldersHistoryProps(showPopup: Boolean,
                                       currDirPath: String = "test/dir",
                                       onChangeDir: String => Unit = _ => (),
                                       onHidePopup: () => Unit = () => ()): FSFoldersHistoryProps = {
    FSFoldersHistoryProps(
      showPopup = showPopup,
      currDirPath = currDirPath,
      onChangeDir = onChangeDir,
      onHidePopup = onHidePopup
    )
  }
}
