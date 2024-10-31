package farjs.fs

import farjs.filelist.FileListServicesSpec.withServicesContext
import farjs.filelist.history._
import farjs.fs.FSFoldersHistory.foldersHistoryKind
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

import scala.scalajs.js

class FSFoldersHistorySpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  //noinspection TypeAnnotation
  class HistoryMocks {
    val get = mockFunction[HistoryKind, js.Promise[HistoryService]]
    val save = mockFunction[History, js.Promise[Unit]]

    val service = new MockHistoryService(
      saveMock = save
    )
    val provider = new MockHistoryProvider(
      getMock = get
    )
  }

  it should "not save current dir if it is empty" in {
    //given
    val props = FSFoldersHistoryProps(currDirPath = "")
    val historyMocks = new HistoryMocks

    //then
    historyMocks.get.expects(foldersHistoryKind).never()
    historyMocks.save.expects(*).never()
    
    //when
    val renderer = createTestRenderer(withServicesContext(
      <(FSFoldersHistory())(^.wrapped := props)(), historyProvider = historyMocks.provider
    ))

    //then
    renderer.root.children.toList should be(empty)
  }

  it should "save current dir if it is non-empty" in {
    //given
    val props = FSFoldersHistoryProps(currDirPath = "dir 1")
    val historyMocks = new HistoryMocks

    //then
    var saveHistory: History = null
    historyMocks.get.expects(foldersHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.save.expects(*).onCall { h: History =>
      saveHistory = h
      js.Promise.resolve[Unit](())
    }
    
    //when
    val renderer = createTestRenderer(withServicesContext(
      <(FSFoldersHistory())(^.wrapped := props)(), historyProvider = historyMocks.provider
    ))

    //then
    eventually(saveHistory should not be null).map { _ =>
      inside(saveHistory) {
        case History(item, params) =>
          item shouldBe props.currDirPath
          params shouldBe js.undefined
      }
      renderer.root.children.toList should be(empty)
    }
  }

  it should "not save current dir if it is not changed" in {
    //given
    val props = FSFoldersHistoryProps(currDirPath = "dir 1")
    val historyMocks = new HistoryMocks
    var saveHistory: History = null
    historyMocks.get.expects(foldersHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.save.expects(*).onCall { h: History =>
      saveHistory = h
      js.Promise.resolve[Unit](())
    }

    val renderer = createTestRenderer(withServicesContext(
      <(FSFoldersHistory())(^.wrapped := props)(), historyProvider = historyMocks.provider
    ))
    eventually(saveHistory should not be null).map { _ =>
      inside(saveHistory) {
        case History(item, params) =>
          item shouldBe props.currDirPath
          params shouldBe js.undefined
      }
    }

    val updatedProps = props.copy(currDirPath = "dir 1")

    //then
    historyMocks.save.expects(*).never()

    //when
    TestRenderer.act { () =>
      renderer.update(withServicesContext(
        <(FSFoldersHistory())(^.wrapped := updatedProps)(), historyProvider = historyMocks.provider
      ))
    }
    
    Succeeded
  }

  it should "save current dir if it is changed" in {
    //given
    val props = FSFoldersHistoryProps(currDirPath = "dir 1")
    val historyMocks = new HistoryMocks

    var saveHistory: History = null
    historyMocks.get.expects(foldersHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
      .twice()
    historyMocks.save.expects(*).onCall { h: History =>
      saveHistory = h
      js.Promise.resolve[Unit](())
    }.twice()
    
    val renderer = createTestRenderer(withServicesContext(
      <(FSFoldersHistory())(^.wrapped := props)(), historyProvider = historyMocks.provider
    ))
    eventually(saveHistory should not be null).map { _ =>
      inside(saveHistory) {
        case History(item, params) =>
          item shouldBe props.currDirPath
          params shouldBe js.undefined
      }
    }
    saveHistory = null
    val updatedProps = props.copy(currDirPath = "dir 2")

    //when
    TestRenderer.act { () =>
      renderer.update(withServicesContext(
        <(FSFoldersHistory())(^.wrapped := updatedProps)(), historyProvider = historyMocks.provider
      ))
    }

    //then
    eventually(saveHistory should not be null).map { _ =>
      inside(saveHistory) {
        case History(item, params) =>
          item shouldBe updatedProps.currDirPath
          params shouldBe js.undefined
      }
    }
  }
}
