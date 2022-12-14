package farjs.app.filelist.fs.popups

import farjs.app.filelist.fs.popups.FolderShortcutsPopup._
import farjs.filelist.FileListServicesSpec.withServicesContext
import farjs.filelist.history.MockFileListHistoryService
import farjs.ui.popup.ListPopupProps
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

import scala.concurrent.Future

class FolderShortcutsPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FolderShortcutsPopup.listPopup = mockUiComponent("ListPopup")

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
    val props = getFolderShortcutsPopupProps(onChangeDir = onChangeDir)
    val historyService = new HistoryService
    val itemsF = Future.successful(List("item 1", "item 2"))
    historyService.getAll.expects().returning(itemsF)
    
    val result = createTestRenderer(withServicesContext(
      <(FolderShortcutsPopup())(^.wrapped := props)(), historyService.service
    )).root

    itemsF.flatMap { _ =>
      val popup = findComponentProps(result, listPopup)

      //then
      onChangeDir.expects("item 2")
      
      //when
      popup.onAction(0)

      Succeeded
    }
  }
  
  it should "render popup" in {
    //given
    val props = getFolderShortcutsPopupProps()
    val historyService = new HistoryService
    val items = List("item")
    val itemsF = Future.successful(items)
    historyService.getAll.expects().returning(itemsF)
    
    //when
    val result = createTestRenderer(withServicesContext(
      <(FolderShortcutsPopup())(^.wrapped := props)(), historyService.service
    )).root

    //then
    result.children.toList should be (empty)
    itemsF.map { _ =>
      assertFolderShortcutsPopup(result, props, items)
    }
  }
  
  private def getFolderShortcutsPopupProps(onChangeDir: String => Unit = _ => ()
                                          ): FolderShortcutsPopupProps = {
    FolderShortcutsPopupProps(
      onChangeDir = onChangeDir,
      onClose = () => ()
    )
  }

  private def assertFolderShortcutsPopup(result: TestInstance,
                                         props: FolderShortcutsPopupProps,
                                         items: List[String]): Assertion = {
    
    assertComponents(result.children, List(
      <(listPopup())(^.assertWrapped(inside(_) {
        case ListPopupProps(title, resItems, _, onClose, focusLast, textPaddingLeft, textPaddingRight) =>
          title shouldBe "Folder shortcuts"
          resItems shouldBe items
          onClose should be theSameInstanceAs props.onClose
          focusLast shouldBe false
          textPaddingLeft shouldBe 2
          textPaddingRight shouldBe 1
      }))()
    ))
  }
}
