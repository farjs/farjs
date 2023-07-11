package farjs.copymove

import farjs.copymove.CopyItemsPopup._
import farjs.filelist.FileListServicesSpec.withServicesContext
import farjs.filelist.api.FileListItem
import farjs.filelist.history.MockFileListHistoryService
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup.ModalProps
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class CopyItemsPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  CopyItemsPopup.modalComp = mockUiComponent("Modal")
  CopyItemsPopup.textLineComp = "TextLine".asInstanceOf[ReactClass]
  CopyItemsPopup.comboBoxComp = mockUiComponent("ComboBox")
  CopyItemsPopup.horizontalLineComp = mockUiComponent("HorizontalLine")
  CopyItemsPopup.buttonsPanelComp = "ButtonsPanel".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class HistoryService {
    val getAll = mockFunction[Future[Seq[String]]]

    val service = new MockFileListHistoryService(
      getAllMock = getAll
    )
  }

  it should "set path when onChange in TextBox" in {
    //given
    val path = "initial path"
    val props = CopyItemsPopupProps(move = false, path, Seq(FileListItem("file 1")), _ => (), () => ())
    val historyService = new HistoryService
    val itemsF = Future.successful(List("path", "path 2"))
    historyService.getAll.expects().returning(itemsF)

    val renderer = createTestRenderer(withServicesContext(
      withThemeContext(<(CopyItemsPopup())(^.wrapped := props)()), copyItemsHistory = historyService.service
    ))
    itemsF.flatMap { _ =>
      val textBox = findComponentProps(renderer.root, comboBoxComp, plain = true)
      textBox.value shouldBe path
      val newFolderName = "new path"

      //when
      textBox.onChange(newFolderName)

      //then
      findComponentProps(renderer.root, comboBoxComp, plain = true).value shouldBe newFolderName
    }
  }
  
  it should "call onAction when onEnter in TextBox" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps(move = false, "test", Seq(FileListItem("file 1")), onAction, onCancel)
    val historyService = new HistoryService
    val itemsF = Future.successful(List("path", "path 2"))
    historyService.getAll.expects().returning(itemsF)

    val comp = createTestRenderer(withServicesContext(
      withThemeContext(<(CopyItemsPopup())(^.wrapped := props)()), copyItemsHistory = historyService.service
    )).root
    itemsF.flatMap { _ =>
      val textBox = findComponentProps(comp, comboBoxComp, plain = true)

      //then
      onAction.expects("test")
      onCancel.expects().never()

      //when
      textBox.onEnter.get.apply()

      Succeeded
    }
  }
  
  it should "call onAction when press action button" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps(move = false, "test", Seq(FileListItem("file 1")), onAction, onCancel)
    val historyService = new HistoryService
    val itemsF = Future.successful(List("path", "path 2"))
    historyService.getAll.expects().returning(itemsF)

    val comp = createTestRenderer(withServicesContext(
      withThemeContext(<(CopyItemsPopup())(^.wrapped := props)()), copyItemsHistory = historyService.service
    )).root
    itemsF.flatMap { _ =>
      val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
        case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
      }
      val action = buttonsProps.actions.head

      //then
      onAction.expects("test")
      onCancel.expects().never()

      //when
      action.onAction()

      Succeeded
    }
  }
  
  it should "not call onAction if path is empty" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps(move = false, "", Seq(FileListItem("file 1")), onAction, onCancel)
    val historyService = new HistoryService
    val itemsF = Future.successful(List("path", "path 2"))
    historyService.getAll.expects().returning(itemsF)

    val comp = createTestRenderer(withServicesContext(
      withThemeContext(<(CopyItemsPopup())(^.wrapped := props)()), copyItemsHistory = historyService.service
    )).root
    itemsF.flatMap { _ =>
      val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
        case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
      }
      val action = buttonsProps.actions.head

      //then
      onAction.expects(*).never()
      onCancel.expects().never()

      //when
      action.onAction()

      Succeeded
    }
  }
  
  it should "call onCancel when press Cancel button" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps(move = false, "", Seq(FileListItem("file 1")), onAction, onCancel)
    val historyService = new HistoryService
    val itemsF = Future.successful(List("path", "path 2"))
    historyService.getAll.expects().returning(itemsF)

    val comp = createTestRenderer(withServicesContext(
      withThemeContext(<(CopyItemsPopup())(^.wrapped := props)()), copyItemsHistory = historyService.service
    )).root
    itemsF.flatMap { _ =>
      val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
        case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
      }
      val action = buttonsProps.actions(1)

      //then
      onAction.expects(*).never()
      onCancel.expects()

      //when
      action.onAction()

      Succeeded
    }
  }
  
  it should "render component when copy" in {
    //given
    val props = CopyItemsPopupProps(move = false, "test folder", Seq(FileListItem("file 1")), _ => (), () => ())
    val historyService = new HistoryService
    val itemsF = Future.successful(List("path", "path 2"))
    historyService.getAll.expects().returning(itemsF)

    //when
    val result = createTestRenderer(withServicesContext(
      withThemeContext(<(CopyItemsPopup())(^.wrapped := props)()), copyItemsHistory = historyService.service
    )).root

    //then
    itemsF.flatMap { items =>
      assertCopyItemsPopup(result.children(0), props, items, List("[ Copy ]", "[ Cancel ]"))
    }
  }

  it should "render component when move" in {
    //given
    val props = CopyItemsPopupProps(move = true, "test folder", Seq(FileListItem("file 1")), _ => (), () => ())
    val historyService = new HistoryService
    val itemsF = Future.successful(Nil)
    historyService.getAll.expects().returning(itemsF)

    //when
    val result = createTestRenderer(withServicesContext(
      withThemeContext(<(CopyItemsPopup())(^.wrapped := props)()), copyItemsHistory = historyService.service
    )).root

    //then
    itemsF.flatMap { items =>
      assertCopyItemsPopup(result.children(0), props, items, List("[ Rename ]", "[ Cancel ]"))
    }
  }

  private def assertCopyItemsPopup(result: TestInstance,
                                   props: CopyItemsPopupProps,
                                   items: List[String],
                                   actions: List[String]): Assertion = {
    val (width, height) = (75, 8)
    val style = DefaultTheme.popup.regular
    val count = props.items.size
    val itemsText =
      if (count > 1) s"$count items"
      else s"${props.items.headOption.map(i => s""""${i.name}"""").getOrElse("")}"

    val title = if (props.move) "Rename/Move" else "Copy"
    val text = if (props.move) "Rename or move" else "Copy"
    
    def assertComponents(label: TestInstance,
                         input: TestInstance,
                         sep: TestInstance,
                         actionsBox: TestInstance): Assertion = {

      assertNativeComponent(label, <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
        case TextLineProps(align, left, top, resWidth, resText, resStyle, focused, padding) =>
          align shouldBe TextAlign.left
          left shouldBe 2
          top shouldBe 1
          resWidth shouldBe (width - (paddingHorizontal + 2) * 2)
          resText shouldBe s"$text $itemsText to:"
          resStyle shouldBe style
          focused shouldBe js.undefined
          padding shouldBe 0
      }))())
      assertTestComponent(input, comboBoxComp, plain = true) {
        case ComboBoxProps(left, top, resWidth, resItems, resValue, _, _) =>
          left shouldBe 2
          top shouldBe 2
          resWidth shouldBe (width - (paddingHorizontal + 2) * 2)
          resItems.toList shouldBe items.reverse
          resValue shouldBe props.path
      }

      assertTestComponent(sep, horizontalLineComp, plain = true) {
        case HorizontalLineProps(resLeft, resTop, resLength, lineCh, resStyle, startCh, endCh) =>
          resLeft shouldBe 0
          resTop shouldBe 3
          resLength shouldBe (width - paddingHorizontal * 2)
          lineCh shouldBe SingleChars.horizontal
          resStyle shouldBe style
          startCh shouldBe DoubleChars.leftSingle
          endCh shouldBe DoubleChars.rightSingle
      }
      assertNativeComponent(actionsBox,
        <(buttonsPanelComp)(^.assertPlain[ButtonsPanelProps](inside(_) {
          case ButtonsPanelProps(top, resActions, resStyle, padding, margin) =>
            top shouldBe 4
            resActions.map(_.label).toList shouldBe actions
            resStyle shouldBe style
            padding shouldBe js.undefined
            margin shouldBe 2
        }))()
      )
    }
    
    assertTestComponent(result, modalComp)({ case ModalProps(resTitle, size, resStyle, onCancel) =>
      resTitle shouldBe title
      size shouldBe width -> height
      resStyle shouldBe style
      onCancel should be theSameInstanceAs props.onCancel
    }, inside(_) { case List(label, input, sep, actionsBox) =>
      assertComponents(label, input, sep, actionsBox)
    })
  }
}
