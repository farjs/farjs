package farjs.file.popups

import farjs.file.FileServicesSpec.withServicesContext
import farjs.file.popups.FileViewHistoryPopup._
import farjs.file.{FileViewHistory, FileViewHistoryParams, MockFileViewHistoryService}
import farjs.ui.popup.ListPopupProps
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class FileViewHistoryPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FileViewHistoryPopup.listPopup = "ListPopup".asInstanceOf[ReactClass]

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
    val itemsF = Future.successful(items)
    historyService.getAll.expects().returning(itemsF)
    
    val result = createTestRenderer(withServicesContext(
      <(FileViewHistoryPopup())(^.wrapped := props)(), historyService.service
    )).root

    itemsF.flatMap { _ =>
      val popup = inside(findComponents(result, listPopup)) {
        case List(c) => c.props.asInstanceOf[ListPopupProps]
      }

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
    val itemsF = Future.successful(items)
    historyService.getAll.expects().returning(itemsF)
    
    //when
    val result = createTestRenderer(withServicesContext(
      <(FileViewHistoryPopup())(^.wrapped := props)(), historyService.service
    )).root

    //then
    result.children.toList should be (empty)
    itemsF.map { _ =>
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
