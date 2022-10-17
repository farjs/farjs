package farjs.app.filelist.fs

import farjs.app.filelist.fs.FSFoldersPopup._
import farjs.filelist.FileListServicesSpec.withServicesContext
import farjs.filelist.history.MockFileListHistoryService
import farjs.ui.WithSizeProps
import farjs.ui.popup.{ModalContentProps, PopupProps}
import farjs.ui.theme.DefaultTheme
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

import scala.concurrent.Future

class FSFoldersPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FSFoldersPopup.popupComp = mockUiComponent("Popup")
  FSFoldersPopup.modalContentComp = mockUiComponent("ModalContent")
  FSFoldersPopup.withSizeComp = mockUiComponent("WithSize")
  FSFoldersPopup.fsFoldersViewComp = mockUiComponent("FSFoldersView")

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
      val renderContent = findComponentProps(result, withSizeComp, plain = true).render(60, 20)
      val resultContent = createTestRenderer(renderContent).root

      //then
      onChangeDir.expects("item 2")
      
      //when
      findComponentProps(resultContent, fsFoldersViewComp).onAction(1)

      Succeeded
    }
  }
  
  it should "render component with empty list" in {
    //given
    val props = getFSFoldersPopupProps()
    val historyService = new HistoryService
    val itemsF = Future.successful(Nil)
    historyService.getAll.expects().returning(itemsF)
    
    //when
    val result = createTestRenderer(withServicesContext(
      <(FSFoldersPopup())(^.wrapped := props)(), historyService.service
    )).root

    //then
    result.children.toList should be (empty)
    itemsF.map { _ =>
      assertFSFoldersPopup(result, props, Nil, (60, 20), (56, 14))
    }
  }
  
  it should "render component with min size" in {
    //given
    val props = getFSFoldersPopupProps()
    val historyService = new HistoryService
    val itemsF = Future.successful(List.fill(20)("item"))
    historyService.getAll.expects().returning(itemsF)
    
    //when
    val result = createTestRenderer(withServicesContext(
      <(FSFoldersPopup())(^.wrapped := props)(), historyService.service
    )).root

    //then
    result.children.toList should be (empty)
    itemsF.map { _ =>
      assertFSFoldersPopup(result, props, List.fill(20)("  item"), (55, 13), (56, 14))
    }
  }
  
  it should "render component with max height" in {
    //given
    val props = getFSFoldersPopupProps()
    val historyService = new HistoryService
    val itemsF = Future.successful(List.fill(20)("item"))
    historyService.getAll.expects().returning(itemsF)
    
    //when
    val result = createTestRenderer(withServicesContext(
      <(FSFoldersPopup())(^.wrapped := props)(), historyService.service
    )).root

    //then
    result.children.toList should be (empty)
    itemsF.map { _ =>
      assertFSFoldersPopup(result, props, List.fill(20)("  item"), (60, 20), (56, 16))
    }
  }
  
  it should "render component with max width" in {
    //given
    val props = getFSFoldersPopupProps()
    val historyService = new HistoryService
    val itemsF = Future.successful(List.fill(20)(
      "iteeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeem"
    ))
    historyService.getAll.expects().returning(itemsF)
    
    //when
    val result = createTestRenderer(withServicesContext(
      <(FSFoldersPopup())(^.wrapped := props)(), historyService.service
    )).root

    //then
    result.children.toList should be (empty)
    itemsF.map { _ =>
      assertFSFoldersPopup(result, props, List.fill(20)(
        "  ite...eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeem"
      ), (60, 20), (60, 16))
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
                                   items: List[String],
                                   screenSize: (Int, Int),
                                   expectedSize: (Int, Int)): Assertion = {

    val theme = DefaultTheme.popup.menu
    val (width, height) = screenSize
    val (expectedWidth, expectedHeight) = expectedSize
    val contentWidth = expectedWidth - 2 * (paddingHorizontal + 1)
    val contentHeight = expectedHeight - 2 * (paddingVertical + 1)
    
    assertComponents(result.children, List(
      <(popupComp())(^.assertWrapped(inside(_) {
        case PopupProps(onClose, closable, focusable, _, _) =>
          onClose should be theSameInstanceAs props.onClose
          closable shouldBe true
          focusable shouldBe true
      }))(
        <(withSizeComp())(^.assertPlain[WithSizeProps](inside(_) {
          case WithSizeProps(render) =>
            val content = createTestRenderer(render(width, height)).root
            
            assertTestComponent(content, modalContentComp)({
              case ModalContentProps(title, size, style, padding, left) =>
                title shouldBe "Folders history"
                size shouldBe (expectedWidth -> expectedHeight)
                style shouldBe theme
                padding shouldBe FSFoldersPopup.padding
                left shouldBe "center"
            }, inside(_) { case List(view) =>
              assertTestComponent(view, fsFoldersViewComp) {
                case FSFoldersViewProps(left, top, width, height, selected, resItems, style, _) =>
                  left shouldBe 1
                  top shouldBe 1
                  width shouldBe contentWidth
                  height shouldBe contentHeight
                  selected shouldBe {
                    if (items.isEmpty) 0
                    else items.length - 1
                  }
                  resItems shouldBe items
                  style shouldBe theme
              }
            })
        }))()
      )
    ))
  }
}
