package farjs.app.filelist.fs

import farjs.app.filelist.fs.FSFoldersPopup._
import farjs.filelist.FileListServicesSpec.withServicesContext
import farjs.filelist.history.MockFileListHistoryService
import farjs.ui.popup.ListPopupProps
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

import scala.concurrent.Future

class FSFoldersPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FSFoldersPopup.listPopup = mockUiComponent("ListPopup")

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
    val props = getFSFoldersPopupProps(onChangeDir = onChangeDir)
    val historyService = new HistoryService
    val itemsF = Future.successful(List("item 1", "item 2"))
    historyService.getAll.expects().returning(itemsF)
    
    val result = createTestRenderer(withServicesContext(
      <(FSFoldersPopup())(^.wrapped := props)(), historyService.service
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
    val props = getFSFoldersPopupProps()
    val historyService = new HistoryService
    val items = List.fill(20)("item")
    val itemsF = Future.successful(items)
    historyService.getAll.expects().returning(itemsF)
    
    //when
    val result = createTestRenderer(withServicesContext(
      <(FSFoldersPopup())(^.wrapped := props)(), historyService.service
    )).root

    //then
    result.children.toList should be (empty)
    itemsF.map { _ =>
      assertFSFoldersPopup(result, props, items)
    }
  }
  
  private def getFSFoldersPopupProps(onChangeDir: String => Unit = _ => ()): FSFoldersPopupProps = {
    FSFoldersPopupProps(
      onChangeDir = onChangeDir,
      onClose = () => ()
    )
  }
  
  private def assertFSFoldersPopup(result: TestInstance,
                                   props: FSFoldersPopupProps,
                                   items: List[String]): Assertion = {
    
    assertComponents(result.children, List(
      <(listPopup())(^.assertWrapped(inside(_) {
        case ListPopupProps(title, resItems, _, onClose, focusLast, textPaddingLeft, textPaddingRight) =>
          title shouldBe "Folders history"
          resItems shouldBe items
          onClose should be theSameInstanceAs props.onClose
          focusLast shouldBe true
          textPaddingLeft shouldBe 2
          textPaddingRight shouldBe 1
      }))()
    ))
  }
}
