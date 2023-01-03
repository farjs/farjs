package farjs.fs

import farjs.filelist.FileListServicesSpec.withServicesContext
import farjs.filelist.history.MockFileListHistoryService
import scommons.react.test._

import scala.concurrent.Future

class FSFoldersHistorySpec extends TestSpec with TestRendererUtils {

  //noinspection TypeAnnotation
  class HistoryService {
    val save = mockFunction[String, Future[Unit]]

    val service = new MockFileListHistoryService(
      saveMock = save
    )
  }

  it should "not save current dir if it is empty" in {
    //given
    val props = FSFoldersHistoryProps(currDirPath = "")
    val historyService = new HistoryService

    //then
    historyService.save.expects(props.currDirPath).never()
    
    //when
    val renderer = createTestRenderer(withServicesContext(
      <(FSFoldersHistory())(^.wrapped := props)(), historyService.service
    ))

    //then
    renderer.root.children.toList should be(empty)
  }

  it should "save current dir if it is non-empty" in {
    //given
    val props = FSFoldersHistoryProps(currDirPath = "dir 1")
    val historyService = new HistoryService

    //then
    historyService.save.expects(props.currDirPath).returning(Future.unit)
    
    //when
    val renderer = createTestRenderer(withServicesContext(
      <(FSFoldersHistory())(^.wrapped := props)(), historyService.service
    ))

    //then
    renderer.root.children.toList should be(empty)
  }

  it should "not save current dir if it is not changed" in {
    //given
    val props = FSFoldersHistoryProps(currDirPath = "dir 1")
    val historyService = new HistoryService
    historyService.save.expects(props.currDirPath).returning(Future.unit)

    val renderer = createTestRenderer(withServicesContext(
      <(FSFoldersHistory())(^.wrapped := props)(), historyService.service
    ))
    val updatedProps = props.copy(currDirPath = "dir 1")

    //then
    historyService.save.expects(*).never()

    //when
    TestRenderer.act { () =>
      renderer.update(withServicesContext(
        <(FSFoldersHistory())(^.wrapped := updatedProps)(), historyService.service
      ))
    }
  }

  it should "save current dir if it is changed" in {
    //given
    val props = FSFoldersHistoryProps(currDirPath = "dir 1")
    val historyService = new HistoryService
    historyService.save.expects(props.currDirPath).returning(Future.unit)
    
    val renderer = createTestRenderer(withServicesContext(
      <(FSFoldersHistory())(^.wrapped := props)(), historyService.service
    ))
    val updatedProps = props.copy(currDirPath = "dir 2")

    //then
    historyService.save.expects("dir 2").returning(Future.unit)
  
    //when
    TestRenderer.act { () =>
      renderer.update(withServicesContext(
        <(FSFoldersHistory())(^.wrapped := updatedProps)(), historyService.service
      ))
    }
  }
}
