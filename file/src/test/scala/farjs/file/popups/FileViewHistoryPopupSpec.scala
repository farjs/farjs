package farjs.file.popups

import farjs.file.FileViewHistory.fileViewsHistoryKind
import farjs.file.popups.FileViewHistoryPopup._
import farjs.file.{FileViewHistory, FileViewHistoryParams}
import farjs.filelist.history.HistoryProviderSpec.withHistoryProvider
import farjs.filelist.history._
import farjs.ui.popup.ListPopupProps
import org.scalatest.Assertion
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js

class FileViewHistoryPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FileViewHistoryPopup.listPopup = "ListPopup".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class HistoryMocks {
    val get = mockFunction[HistoryKind, js.Promise[HistoryService]]
    val getAll = mockFunction[js.Promise[js.Array[History]]]

    val service = new MockHistoryService(
      getAllMock = getAll
    )
    val provider = new MockHistoryProvider(
      getMock = get
    )
  }

  it should "call onAction when onAction" in {
    //given
    val onAction = mockFunction[FileViewHistory, Unit]
    val props = getFileViewHistoryPopupProps(onAction = onAction)
    val historyMocks = new HistoryMocks
    val items = List("item 1", "item 2").map { path =>
      FileViewHistory(
        path = path,
        params = FileViewHistoryParams(
          isEdit = false,
          encoding = "utf8",
          position = 0,
          wrap = js.undefined,
          column = js.undefined
        )
      )
    }
    val itemsF = js.Promise.resolve[js.Array[History]](js.Array(items.map(FileViewHistory.toHistory): _*))
    var getAllCalled = false
    historyMocks.get.expects(fileViewsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }
    
    val result = createTestRenderer(withHistoryProvider(
      <(FileViewHistoryPopup())(^.wrapped := props)(), historyMocks.provider
    )).root

    eventually(getAllCalled shouldBe true).map { _ =>
      val popup = inside(findComponents(result, listPopup)) {
        case List(c) => c.props.asInstanceOf[ListPopupProps]
      }

      //then
      var capturedHistory: FileViewHistory = null
      onAction.expects(*).onCall { h: FileViewHistory =>
        capturedHistory = h
      }
      
      //when
      popup.onAction(1)

      //then
      capturedHistory shouldBe items(1)
    }
  }
  
  it should "render popup" in {
    //given
    val props = getFileViewHistoryPopupProps()
    val historyMocks = new HistoryMocks
    val items = List.fill(20)("item").zipWithIndex.map { case (path, index) =>
      FileViewHistory(
        path = path,
        params = FileViewHistoryParams(
          isEdit = index % 2 == 0,
          encoding = "utf8",
          position = 0,
          wrap = js.undefined,
          column = js.undefined
        )
      )
    }
    val itemsF = js.Promise.resolve[js.Array[History]](js.Array(items.map(FileViewHistory.toHistory): _*))
    var getAllCalled = false
    historyMocks.get.expects(fileViewsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }
    
    //when
    val result = createTestRenderer(withHistoryProvider(
      <(FileViewHistoryPopup())(^.wrapped := props)(), historyMocks.provider
    )).root

    //then
    result.children.toList should be (empty)
    eventually(getAllCalled shouldBe true).map { _ =>
      assertFileViewHistoryPopup(result, items)
    }
  }
  
  private def getFileViewHistoryPopupProps(onAction: FileViewHistory => Unit = _ => ()
                                          ): FileViewHistoryPopupProps = {
    FileViewHistoryPopupProps(
      onAction = onAction,
      onClose = () => ()
    )
  }

  private def assertFileViewHistoryPopup(result: TestInstance,
                                         items: List[FileViewHistory]): Assertion = {
    
    assertComponents(result.children, List(
      <(listPopup)(^.assertPlain[ListPopupProps](inside(_) {
        case ListPopupProps(
          title,
          resItems,
          _,
          _,
          selected,
          _,
          _,
          footer,
          textPaddingLeft,
          textPaddingRight,
          itemWrapPrefixLen
        ) =>
          title shouldBe "File view history"
          resItems.toList shouldBe items.map { item =>
            val prefix =
              if (item.params.isEdit) "Edit: "
              else "View: "
            s"$prefix${item.path}"
          }
          selected shouldBe (items.length - 1)
          footer shouldBe js.undefined
          textPaddingLeft shouldBe js.undefined
          textPaddingRight shouldBe js.undefined
          itemWrapPrefixLen shouldBe 9
      }))()
    ))
  }
}
