package farjs.fs.popups

import farjs.filelist.FileListServicesSpec.withServicesContext
import farjs.filelist.history.MockFileListHistoryService
import farjs.fs.popups.FoldersHistoryPopup._
import farjs.ui.popup.ListPopupProps
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

import scala.concurrent.Future

class FoldersHistoryPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FoldersHistoryPopup.listPopup = mockUiComponent("ListPopup")

  //noinspection TypeAnnotation
  class HistoryService {
    val getAll = mockFunction[Future[Seq[String]]]

    val service = new MockFileListHistoryService(
      getAllMock = getAll
    )
  }

  it should "call onChangeDir when onAction" in {
    //given
    val onChangeDir = mockFunction[String, Unit]
    val props = getFoldersHistoryPopupProps(onChangeDir = onChangeDir)
    val historyService = new HistoryService
    val itemsF = Future.successful(List("item 1", "item 2"))
    historyService.getAll.expects().returning(itemsF)
    
    val result = createTestRenderer(withServicesContext(
      <(FoldersHistoryPopup())(^.wrapped := props)(), historyService.service
    )).root

    itemsF.flatMap { _ =>
      val popup = findComponentProps(result, listPopup)

      //then
      onChangeDir.expects("item 2")
      
      //when
      popup.onAction(1)

      Succeeded
    }
  }
  
  it should "render popup" in {
    //given
    val props = getFoldersHistoryPopupProps()
    val historyService = new HistoryService
    val items = List.fill(20)("item")
    val itemsF = Future.successful(items)
    historyService.getAll.expects().returning(itemsF)
    
    //when
    val result = createTestRenderer(withServicesContext(
      <(FoldersHistoryPopup())(^.wrapped := props)(), historyService.service
    )).root

    //then
    result.children.toList should be (empty)
    itemsF.map { _ =>
      assertFoldersHistoryPopup(result, props, items)
    }
  }
  
  private def getFoldersHistoryPopupProps(onChangeDir: String => Unit = _ => ()): FoldersHistoryPopupProps = {
    FoldersHistoryPopupProps(
      onChangeDir = onChangeDir,
      onClose = () => ()
    )
  }

  private def assertFoldersHistoryPopup(result: TestInstance,
                                        props: FoldersHistoryPopupProps,
                                        items: List[String]): Assertion = {
    
    assertComponents(result.children, List(
      <(listPopup())(^.assertWrapped(inside(_) {
        case ListPopupProps(title, resItems, _, onClose, selected, _, _, footer, textPaddingLeft, textPaddingRight) =>
          title shouldBe "Folders history"
          resItems shouldBe items
          onClose should be theSameInstanceAs props.onClose
          selected shouldBe (items.length - 1)
          footer shouldBe None
          textPaddingLeft shouldBe 2
          textPaddingRight shouldBe 1
      }))()
    ))
  }
}
