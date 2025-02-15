package farjs.fs.popups

import farjs.filelist.FileListState
import farjs.filelist.api.FileListDir
import farjs.filelist.stack.WithStacksSpec.withContext
import farjs.filelist.stack.{PanelStack, WithStacksData, PanelStackItem}
import farjs.fs.FSServicesSpec.withServicesContext
import farjs.fs.popups.FolderShortcutsPopup._
import farjs.ui.popup.ListPopupProps
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js

class FolderShortcutsPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FolderShortcutsPopup.listPopup = "ListPopup".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class ShortcutsService {
    val getAll = mockFunction[js.Promise[js.Array[js.UndefOr[String]]]]
    val save = mockFunction[Int, String, js.Promise[Unit]]
    val delete = mockFunction[Int, js.Promise[Unit]]

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
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), null)
    val shortcutsService = new ShortcutsService
    val itemsF = js.Promise.resolve[js.Array[js.UndefOr[String]]](
      js.Array[js.UndefOr[String]](List.fill(10)("item": js.UndefOr[String]): _*)
    )
    shortcutsService.getAll.expects().returning(itemsF)
    
    val result = createTestRenderer(withContext(withServicesContext(
      <(FolderShortcutsPopup())(^.wrapped := props)(), shortcutsService.service
    ), WithStacksData(leftStack, null), WithStacksData(rightStack, null))).root

    itemsF.toFuture.flatMap { _ =>
      val popup = findListPopupProps(result)

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
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), null)
    val shortcutsService = new ShortcutsService
    val itemsF = js.Promise.resolve[js.Array[js.UndefOr[String]]](
      js.Array[js.UndefOr[String]](List.fill(10)(js.undefined): _*)
    )
    shortcutsService.getAll.expects().returning(itemsF)
    
    val result = createTestRenderer(withContext(withServicesContext(
      <(FolderShortcutsPopup())(^.wrapped := props)(), shortcutsService.service
    ), WithStacksData(leftStack, null), WithStacksData(rightStack, null))).root

    itemsF.toFuture.flatMap { _ =>
      val popup = findListPopupProps(result)

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
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), null)
    val shortcutsService = new ShortcutsService
    val itemsF = js.Promise.resolve[js.Array[js.UndefOr[String]]](js.Array[js.UndefOr[String]](
      "item 1",
      "item 2",
      "item 3",
      "item 4",
      "item 5",
      "item 6",
      "item 7",
      "item 8",
      "item 9",
      "item 10"
    ))
    shortcutsService.getAll.expects().returning(itemsF)

    val result = createTestRenderer(withContext(withServicesContext(
      <(FolderShortcutsPopup())(^.wrapped := props)(), shortcutsService.service
    ), WithStacksData(leftStack, null), WithStacksData(rightStack, null))).root

    itemsF.toFuture.flatMap { _ =>
      val popup = findListPopupProps(result)

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
      popup.onKeypress.foreach(_.apply("0") shouldBe true)
      popup.onKeypress.foreach(_.apply("1") shouldBe true)
      popup.onKeypress.foreach(_.apply("2") shouldBe true)
      popup.onKeypress.foreach(_.apply("3") shouldBe true)
      popup.onKeypress.foreach(_.apply("4") shouldBe true)
      popup.onKeypress.foreach(_.apply("5") shouldBe true)
      popup.onKeypress.foreach(_.apply("6") shouldBe true)
      popup.onKeypress.foreach(_.apply("7") shouldBe true)
      popup.onKeypress.foreach(_.apply("8") shouldBe true)
      popup.onKeypress.foreach(_.apply("9") shouldBe true)
      Succeeded
    }
  }

  it should "set item to <none> when onKeypress(-)" in {
    //given
    val props = getFolderShortcutsPopupProps()
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), null)
    val shortcutsService = new ShortcutsService
    val itemsF = js.Promise.resolve[js.Array[js.UndefOr[String]]](js.Array[js.UndefOr[String]]("item"))
    val deleteF = js.Promise.resolve[Unit](())
    shortcutsService.getAll.expects().returning(itemsF)
    shortcutsService.delete.expects(0).returning(deleteF)
    
    val result = createTestRenderer(withContext(withServicesContext(
      <(FolderShortcutsPopup())(^.wrapped := props)(), shortcutsService.service
    ), WithStacksData(leftStack, null), WithStacksData(rightStack, null))).root

    itemsF.toFuture.flatMap { _ =>
      val popup = findListPopupProps(result)
      popup.items.head shouldBe "0: item"

      //when
      popup.onKeypress.foreach(_.apply("-") shouldBe true)

      //then
      deleteF.toFuture.map { _ =>
        findListPopupProps(result).items.head shouldBe "0: <none>"
      }
    }
  }
  
  it should "set item to current fs path when onKeypress(+)" in {
    //given
    val props = getFolderShortcutsPopupProps()
    val currState = FileListState(currDir = FileListDir("/test", isRoot = false, js.Array()))
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("otherComp".asInstanceOf[ReactClass]),
      PanelStackItem("fsComp".asInstanceOf[ReactClass], js.undefined, js.undefined, currState)
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), null)
    val shortcutsService = new ShortcutsService
    val itemsF = js.Promise.resolve[js.Array[js.UndefOr[String]]](
      js.Array[js.UndefOr[String]](List.fill(10)(js.undefined): _*)
    )
    val saveF = js.Promise.resolve[Unit](())
    shortcutsService.getAll.expects().returning(itemsF)
    shortcutsService.save.expects(1, "/test").returning(saveF)
    
    val result = createTestRenderer(withContext(withServicesContext(
      <(FolderShortcutsPopup())(^.wrapped := props)(), shortcutsService.service
    ), WithStacksData(leftStack, null), WithStacksData(rightStack, null))).root

    itemsF.toFuture.flatMap { _ =>
      findListPopupProps(result).onSelect.foreach(f => f(1))
      val popup = findListPopupProps(result)
      popup.items(1) shouldBe "1: <none>"

      //when
      popup.onKeypress.foreach(_.apply("+") shouldBe true)

      //then
      saveF.toFuture.map { _ =>
        findListPopupProps(result).items(1) shouldBe "1: /test"
      }
    }
  }
  
  it should "return false if unknown key when onKeypress" in {
    //given
    val props = getFolderShortcutsPopupProps()
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), null)
    val shortcutsService = new ShortcutsService
    val itemsF = js.Promise.resolve[js.Array[js.UndefOr[String]]](
      js.Array[js.UndefOr[String]]("item")
    )
    shortcutsService.getAll.expects().returning(itemsF)
    
    val result = createTestRenderer(withContext(withServicesContext(
      <(FolderShortcutsPopup())(^.wrapped := props)(), shortcutsService.service
    ), WithStacksData(leftStack, null), WithStacksData(rightStack, null))).root

    itemsF.toFuture.flatMap { _ =>
      val popup = findListPopupProps(result)

      //when & then
      popup.onKeypress.foreach(_.apply("unknown") shouldBe false)
      Succeeded
    }
  }
  
  it should "render popup" in {
    //given
    val props = getFolderShortcutsPopupProps()
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), null)
    val shortcutsService = new ShortcutsService
    val itemsF = js.Promise.resolve[js.Array[js.UndefOr[String]]](js.Array[js.UndefOr[String]](
      "item",
      js.undefined,
      js.undefined,
      js.undefined,
      js.undefined,
      js.undefined,
      js.undefined,
      js.undefined,
      js.undefined,
      js.undefined
    ))
    shortcutsService.getAll.expects().returning(itemsF)
    
    //when
    val result = createTestRenderer(withContext(withServicesContext(
      <(FolderShortcutsPopup())(^.wrapped := props)(), shortcutsService.service
    ), WithStacksData(leftStack, null), WithStacksData(rightStack, null))).root

    //then
    result.children.toList should be (empty)
    itemsF.toFuture.map { _ =>
      assertFolderShortcutsPopup(result, List(
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
  
  private def findListPopupProps(result: TestInstance): ListPopupProps = {
    inside(findComponents(result, listPopup)) {
      case List(c) => c.props.asInstanceOf[ListPopupProps]
    }
  }
  
  private def getFolderShortcutsPopupProps(onChangeDir: String => Unit = _ => ()
                                          ): FolderShortcutsPopupProps = {
    FolderShortcutsPopupProps(
      onChangeDir = onChangeDir,
      onClose = () => ()
    )
  }

  private def assertFolderShortcutsPopup(result: TestInstance, items: List[String]): Assertion = {
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
          title shouldBe "Folder shortcuts"
          resItems.toList shouldBe items
          selected shouldBe js.undefined
          footer shouldBe "Edit: +, -"
          textPaddingLeft shouldBe js.undefined
          textPaddingRight shouldBe js.undefined
          itemWrapPrefixLen shouldBe js.undefined
      }))()
    ))
  }
}
