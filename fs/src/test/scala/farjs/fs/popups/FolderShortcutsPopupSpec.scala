package farjs.fs.popups

import farjs.filelist.FileListState
import farjs.filelist.api.FileListDir
import farjs.filelist.stack.WithPanelStacksSpec.withContext
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import farjs.fs.FSServicesSpec.withServicesContext
import farjs.fs.popups.FolderShortcutsPopup._
import farjs.ui.popup.ListPopupProps
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

import scala.concurrent.Future

class FolderShortcutsPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FolderShortcutsPopup.listPopup = mockUiComponent("ListPopup")

  //noinspection TypeAnnotation
  class ShortcutsService {
    val getAll = mockFunction[Future[Seq[Option[String]]]]
    val save = mockFunction[Int, String, Future[Unit]]
    val delete = mockFunction[Int, Future[Unit]]

    val service = new MockFolderShortcutsService(
      getAllMock = getAll,
      saveMock = save,
      deleteMock = delete
    )
  }

  it should "call onChangeDir when onAction" in {
    //given
    val onChangeDir = mockFunction[String, Unit]
    val props = getFolderShortcutsPopupProps(onChangeDir = onChangeDir)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val shortcutsService = new ShortcutsService
    val itemsF = Future.successful(List.fill(10)(Option("item")))
    shortcutsService.getAll.expects().returning(itemsF)
    
    val result = createTestRenderer(withContext(withServicesContext(
      <(FolderShortcutsPopup())(^.wrapped := props)(), shortcutsService.service
    ), leftStack, rightStack)).root

    itemsF.flatMap { _ =>
      val popup = findComponentProps(result, listPopup)

      //then
      onChangeDir.expects("item")
      
      //when
      popup.onAction(0)

      Succeeded
    }
  }
  
  it should "not call onChangeDir if <none> when onAction" in {
    //given
    val onChangeDir = mockFunction[String, Unit]
    val props = getFolderShortcutsPopupProps(onChangeDir = onChangeDir)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val shortcutsService = new ShortcutsService
    val itemsF = Future.successful(List.fill(10)(None))
    shortcutsService.getAll.expects().returning(itemsF)
    
    val result = createTestRenderer(withContext(withServicesContext(
      <(FolderShortcutsPopup())(^.wrapped := props)(), shortcutsService.service
    ), leftStack, rightStack)).root

    itemsF.flatMap { _ =>
      val popup = findComponentProps(result, listPopup)

      //then
      onChangeDir.expects(*).never()
      
      //when
      popup.onAction(1)

      Succeeded
    }
  }

  it should "call onChangeDir when onKeypress(0-9)" in {
    //given
    val onChangeDir = mockFunction[String, Unit]
    val props = getFolderShortcutsPopupProps(onChangeDir = onChangeDir)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val shortcutsService = new ShortcutsService
    val itemsF = Future.successful(List(
      Option("item 1"),
      Option("item 2"),
      Option("item 3"),
      Option("item 4"),
      Option("item 5"),
      Option("item 6"),
      Option("item 7"),
      Option("item 8"),
      Option("item 9"),
      Option("item 10")
    ))
    shortcutsService.getAll.expects().returning(itemsF)

    val result = createTestRenderer(withContext(withServicesContext(
      <(FolderShortcutsPopup())(^.wrapped := props)(), shortcutsService.service
    ), leftStack, rightStack)).root

    itemsF.flatMap { _ =>
      val popup = findComponentProps(result, listPopup)

      //then
      onChangeDir.expects("item 1")
      onChangeDir.expects("item 2")
      onChangeDir.expects("item 3")
      onChangeDir.expects("item 4")
      onChangeDir.expects("item 5")
      onChangeDir.expects("item 6")
      onChangeDir.expects("item 7")
      onChangeDir.expects("item 8")
      onChangeDir.expects("item 9")
      onChangeDir.expects("item 10")

      //when & then
      popup.onKeypress("0") shouldBe true
      popup.onKeypress("1") shouldBe true
      popup.onKeypress("2") shouldBe true
      popup.onKeypress("3") shouldBe true
      popup.onKeypress("4") shouldBe true
      popup.onKeypress("5") shouldBe true
      popup.onKeypress("6") shouldBe true
      popup.onKeypress("7") shouldBe true
      popup.onKeypress("8") shouldBe true
      popup.onKeypress("9") shouldBe true
    }
  }

  it should "set item to <none> when onKeypress(-)" in {
    //given
    val props = getFolderShortcutsPopupProps()
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val shortcutsService = new ShortcutsService
    val itemsF = Future.successful(List(Option("item")))
    val deleteF = Future.unit
    shortcutsService.getAll.expects().returning(itemsF)
    shortcutsService.delete.expects(0).returning(deleteF)
    
    val result = createTestRenderer(withContext(withServicesContext(
      <(FolderShortcutsPopup())(^.wrapped := props)(), shortcutsService.service
    ), leftStack, rightStack)).root

    itemsF.flatMap { _ =>
      val popup = findComponentProps(result, listPopup)
      popup.items.head shouldBe "0: item"

      //when
      popup.onKeypress("-") shouldBe true

      //then
      deleteF.map { _ =>
        findComponentProps(result, listPopup).items.head shouldBe "0: <none>"
      }
    }
  }
  
  it should "set item to current fs path when onKeypress(+)" in {
    //given
    val props = getFolderShortcutsPopupProps()
    val currState = FileListState(currDir = FileListDir("/test", isRoot = false, Nil))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None),
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, Some(currState))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val shortcutsService = new ShortcutsService
    val itemsF = Future.successful(List.fill(10)(None))
    val saveF = Future.unit
    shortcutsService.getAll.expects().returning(itemsF)
    shortcutsService.save.expects(1, "/test").returning(saveF)
    
    val result = createTestRenderer(withContext(withServicesContext(
      <(FolderShortcutsPopup())(^.wrapped := props)(), shortcutsService.service
    ), leftStack, rightStack)).root

    itemsF.flatMap { _ =>
      findComponentProps(result, listPopup).onSelect(1)
      val popup = findComponentProps(result, listPopup)
      popup.items(1) shouldBe "1: <none>"

      //when
      popup.onKeypress("+") shouldBe true

      //then
      saveF.map { _ =>
        findComponentProps(result, listPopup).items(1) shouldBe "1: /test"
      }
    }
  }
  
  it should "return false if unknown key when onKeypress" in {
    //given
    val props = getFolderShortcutsPopupProps()
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val shortcutsService = new ShortcutsService
    val itemsF = Future.successful(List(Option("item")))
    shortcutsService.getAll.expects().returning(itemsF)
    
    val result = createTestRenderer(withContext(withServicesContext(
      <(FolderShortcutsPopup())(^.wrapped := props)(), shortcutsService.service
    ), leftStack, rightStack)).root

    itemsF.flatMap { _ =>
      val popup = findComponentProps(result, listPopup)

      //when & then
      popup.onKeypress("unknown") shouldBe false
    }
  }
  
  it should "render popup" in {
    //given
    val props = getFolderShortcutsPopupProps()
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val shortcutsService = new ShortcutsService
    val itemsF = Future.successful(Option("item") :: List.fill(9)(None))
    shortcutsService.getAll.expects().returning(itemsF)
    
    //when
    val result = createTestRenderer(withContext(withServicesContext(
      <(FolderShortcutsPopup())(^.wrapped := props)(), shortcutsService.service
    ), leftStack, rightStack)).root

    //then
    result.children.toList should be (empty)
    itemsF.map { _ =>
      assertFolderShortcutsPopup(result, props, List(
        "0: item",
        "1: <none>",
        "2: <none>",
        "3: <none>",
        "4: <none>",
        "5: <none>",
        "6: <none>",
        "7: <none>",
        "8: <none>",
        "9: <none>"
      ))
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
        case ListPopupProps(title, resItems, _, onClose, _, _, footer, focusLast, textPaddingLeft, textPaddingRight) =>
          title shouldBe "Folder shortcuts"
          resItems shouldBe items
          onClose should be theSameInstanceAs props.onClose
          footer shouldBe Some("Edit: +, -")
          focusLast shouldBe false
          textPaddingLeft shouldBe 2
          textPaddingRight shouldBe 1
      }))()
    ))
  }
}
