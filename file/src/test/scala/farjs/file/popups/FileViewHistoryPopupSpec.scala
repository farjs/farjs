package farjs.file.popups

import farjs.file.FileServicesSpec.withServicesContext
import farjs.file.popups.FileViewHistoryPopup._
import farjs.file.{FileViewHistory, MockFileViewHistoryService}
import farjs.ui.popup.ListPopupProps
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

import scala.concurrent.Future

class FileViewHistoryPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FileViewHistoryPopup.listPopup = mockUiComponent("ListPopup")

  //noinspection TypeAnnotation
  class HistoryService {
    val getAll = mockFunction[Future[Seq[FileViewHistory]]]

    val service = new MockFileViewHistoryService(
      getAllMock = getAll
    )
  }

  it should "call onAction when onAction" in {
    //given
    val onAction = mockFunction[FileViewHistory, Unit]
    val props = getFileViewHistoryPopupProps(onAction = onAction)
    val historyService = new HistoryService
    val items = List("item 1", "item 2").map { path =>
      FileViewHistory(path, isEdit = false, encoding = "utf8", position = 0, wrap = None, column = None)
    }
    val itemsF = Future.successful(items)
    historyService.getAll.expects().returning(itemsF)
    
    val result = createTestRenderer(withServicesContext(
      <(FileViewHistoryPopup())(^.wrapped := props)(), historyService.service
    )).root

    itemsF.flatMap { _ =>
      val popup = findComponentProps(result, listPopup)

      //then
      onAction.expects(items(1))
      
      //when
      popup.onAction(1)

      Succeeded
    }
  }
  
  it should "render popup" in {
    //given
    val props = getFileViewHistoryPopupProps()
    val historyService = new HistoryService
    val items = List.fill(20)("item").map { path =>
      FileViewHistory(path, isEdit = false, encoding = "utf8", position = 0, wrap = None, column = None)
    }
    val itemsF = Future.successful(items)
    historyService.getAll.expects().returning(itemsF)
    
    //when
    val result = createTestRenderer(withServicesContext(
      <(FileViewHistoryPopup())(^.wrapped := props)(), historyService.service
    )).root

    //then
    result.children.toList should be (empty)
    itemsF.map { _ =>
      assertFileViewHistoryPopup(result, props, items)
    }
  }
  
  private def getFileViewHistoryPopupProps(onAction: FileViewHistory => Unit = _ => ()): FileViewHistoryPopupProps = {
    FileViewHistoryPopupProps(
      onAction = onAction,
      onClose = () => ()
    )
  }

  private def assertFileViewHistoryPopup(result: TestInstance,
                                         props: FileViewHistoryPopupProps,
                                         items: List[FileViewHistory]): Assertion = {
    
    assertComponents(result.children, List(
      <(listPopup())(^.assertWrapped(inside(_) {
        case ListPopupProps(title, resItems, _, onClose, selected, _, _, footer, textPaddingLeft, textPaddingRight) =>
          title shouldBe "File view history"
          resItems shouldBe items.map(_.path)
          onClose should be theSameInstanceAs props.onClose
          selected shouldBe (items.length - 1)
          footer shouldBe None
          textPaddingLeft shouldBe 2
          textPaddingRight shouldBe 1
      }))()
    ))
  }
}
