package farjs.filelist.popups

import farjs.filelist.FileListServicesSpec.withServicesContext
import farjs.filelist.history.MockFileListHistoryService
import farjs.filelist.popups.MakeFolderPopup._
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalProps
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class MakeFolderPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  MakeFolderPopup.modalComp = "Modal".asInstanceOf[ReactClass]
  MakeFolderPopup.textLineComp = "TextLine".asInstanceOf[ReactClass]
  MakeFolderPopup.comboBoxComp = "ComboBox".asInstanceOf[ReactClass]
  MakeFolderPopup.horizontalLineComp = "HorizontalLine".asInstanceOf[ReactClass]
  MakeFolderPopup.checkBoxComp = "CheckBox".asInstanceOf[ReactClass]
  MakeFolderPopup.buttonsPanelComp = "ButtonsPanel".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class HistoryService {
    val getAll = mockFunction[Future[Seq[String]]]

    val service = new MockFileListHistoryService(
      getAllMock = getAll
    )
  }

  it should "call onCancel when onCancel in modal" in {
    //given
    val onCancel = mockFunction[Unit]
    val props = getMakeFolderPopupProps(onCancel = onCancel)
    val historyService = new HistoryService
    val itemsF = Future.successful(List("folder"))
    historyService.getAll.expects().returning(itemsF)

    val renderer = createTestRenderer(withServicesContext(
      withThemeContext(<(MakeFolderPopup())(^.wrapped := props)()), mkDirsHistory = historyService.service
    ))
    itemsF.flatMap { _ =>
      val modal = inside(findComponents(renderer.root, modalComp)) {
        case List(modal) => modal.props.asInstanceOf[ModalProps]
      }

      //then
      onCancel.expects()

      //when
      modal.onCancel()

      Succeeded
    }
  }

  it should "set folderName when onChange in ComboBox" in {
    //given
    val folderName = "initial folder name"
    val props = getMakeFolderPopupProps()
    val historyService = new HistoryService
    val itemsF = Future.successful(List("folder", folderName))
    historyService.getAll.expects().returning(itemsF)

    val renderer = createTestRenderer(withServicesContext(
      withThemeContext(<(MakeFolderPopup())(^.wrapped := props)()), mkDirsHistory = historyService.service
    ))
    itemsF.flatMap { _ =>
      val comboBox = inside(findComponents(renderer.root, comboBoxComp)) {
        case List(c) => c.props.asInstanceOf[ComboBoxProps]
      }
      comboBox.value shouldBe folderName
      val newFolderName = "new folder name"

      //when
      comboBox.onChange(newFolderName)

      //then
      inside(findComponents(renderer.root, comboBoxComp)) {
        case List(c) => c.props.asInstanceOf[ComboBoxProps].value shouldBe newFolderName
      }
    }
  }
  
  it should "set multiple flag when onChange in CheckBox" in {
    //given
    val props = getMakeFolderPopupProps()
    val historyService = new HistoryService
    val itemsF = Future.successful(List("folder", "folder 2"))
    historyService.getAll.expects().returning(itemsF)

    val renderer = createTestRenderer(withServicesContext(
      withThemeContext(<(MakeFolderPopup())(^.wrapped := props)()), mkDirsHistory = historyService.service
    ))
    itemsF.flatMap { _ =>
      val checkbox = inside(findComponents(renderer.root, checkBoxComp)) {
        case List(c) => c.props.asInstanceOf[CheckBoxProps]
      }
      checkbox.value shouldBe false

      //when
      checkbox.onChange()

      //then
      inside(findComponents(renderer.root, checkBoxComp)) { case List(c) =>
        c.props.asInstanceOf[CheckBoxProps].value shouldBe true
      }
    }
  }
  
  it should "call onOk when onEnter in ComboBox" in {
    //given
    val onOk = mockFunction[String, Boolean, Unit]
    val onCancel = mockFunction[Unit]
    val props = getMakeFolderPopupProps(multiple = true, onOk, onCancel)
    val historyService = new HistoryService
    val itemsF = Future.successful(List("folder", "test"))
    historyService.getAll.expects().returning(itemsF)

    val comp = createTestRenderer(withServicesContext(
      withThemeContext(<(MakeFolderPopup())(^.wrapped := props)()), mkDirsHistory = historyService.service
    )).root
    itemsF.flatMap { _ =>
      val comboBox = inside(findComponents(comp, comboBoxComp)) {
        case List(c) => c.props.asInstanceOf[ComboBoxProps]
      }

      //then
      onOk.expects("test", true)
      onCancel.expects().never()

      //when
      comboBox.onEnter.get.apply()

      Succeeded
    }
  }
  
  it should "call onOk when press OK button" in {
    //given
    val onOk = mockFunction[String, Boolean, Unit]
    val onCancel = mockFunction[Unit]
    val props = getMakeFolderPopupProps(multiple = true, onOk, onCancel)
    val historyService = new HistoryService
    val itemsF = Future.successful(List("folder", "test"))
    historyService.getAll.expects().returning(itemsF)

    val comp = createTestRenderer(withServicesContext(
      withThemeContext(<(MakeFolderPopup())(^.wrapped := props)()), mkDirsHistory = historyService.service
    )).root
    itemsF.flatMap { _ =>
      val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
        case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
      }
      val action = buttonsProps.actions.head

      //then
      onOk.expects("test", true)
      onCancel.expects().never()

      //when
      action.onAction()

      Succeeded
    }
  }
  
  it should "not call onOk if folderName is empty" in {
    //given
    val onOk = mockFunction[String, Boolean, Unit]
    val onCancel = mockFunction[Unit]
    val props = getMakeFolderPopupProps(multiple = true, onOk, onCancel)
    val historyService = new HistoryService
    val itemsF = Future.successful(Nil)
    historyService.getAll.expects().returning(itemsF)

    val comp = createTestRenderer(withServicesContext(
      withThemeContext(<(MakeFolderPopup())(^.wrapped := props)()), mkDirsHistory = historyService.service
    )).root
    itemsF.flatMap { _ =>
      val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
        case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
      }
      val action = buttonsProps.actions.head

      //then
      onOk.expects(*, *).never()
      onCancel.expects().never()

      //when
      action.onAction()

      Succeeded
    }
  }
  
  it should "call onCancel when press Cancel button" in {
    //given
    val onOk = mockFunction[String, Boolean, Unit]
    val onCancel = mockFunction[Unit]
    val props = getMakeFolderPopupProps(onOk = onOk, onCancel = onCancel)
    val historyService = new HistoryService
    val itemsF = Future.successful(Nil)
    historyService.getAll.expects().returning(itemsF)

    val comp = createTestRenderer(withServicesContext(
      withThemeContext(<(MakeFolderPopup())(^.wrapped := props)()), mkDirsHistory = historyService.service
    )).root
    itemsF.flatMap { _ =>
      val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
        case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
      }
      val action = buttonsProps.actions(1)

      //then
      onOk.expects(*, *).never()
      onCancel.expects()

      //when
      action.onAction()

      Succeeded
    }
  }
  
  it should "render component" in {
    //given
    val props = getMakeFolderPopupProps()
    val historyService = new HistoryService
    val itemsF = Future.successful(List("folder", "folder 2"))
    historyService.getAll.expects().returning(itemsF)

    //when
    val result = createTestRenderer(withServicesContext(
      withThemeContext(<(MakeFolderPopup())(^.wrapped := props)()), mkDirsHistory = historyService.service
    )).root

    //then
    result.children.toList should be (empty)
    itemsF.flatMap { items =>
      result.children.toList should not be empty
      assertMakeFolderPopup(result.children(0), items, List("[ OK ]", "[ Cancel ]"))
    }
  }

  private def getMakeFolderPopupProps(multiple: Boolean = false,
                                      onOk: (String, Boolean) => Unit = (_, _) => (),
                                      onCancel: () => Unit = () => ()): MakeFolderPopupProps = {
    MakeFolderPopupProps(
      multiple = multiple,
      onOk = onOk,
      onCancel = onCancel
    )
  }

  private def assertMakeFolderPopup(result: TestInstance,
                                    items: List[String],
                                    actions: List[String]): Assertion = {
    val (width, height) = (75, 10)
    val style = DefaultTheme.popup.regular
    
    assertNativeComponent(result,
      <(modalComp)(^.assertPlain[ModalProps](inside(_) {
        case ModalProps(title, resWidth, resHeight, resStyle, _) =>
          title shouldBe "Make Folder"
          resWidth shouldBe width
          resHeight shouldBe height
          resStyle shouldBe style
      }))(
        <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
          case TextLineProps(align, left, top, resWidth, text, resStyle, focused, padding) =>
            align shouldBe TextAlign.left
            left shouldBe 2
            top shouldBe 1
            resWidth shouldBe (width - 10)
            text shouldBe "Create the folder"
            resStyle shouldBe style
            focused shouldBe js.undefined
            padding shouldBe 0
        }))(),
        <(comboBoxComp)(^.assertPlain[ComboBoxProps](inside(_) {
          case ComboBoxProps(left, top, resWidth, resItems, resValue, _, _) =>
            left shouldBe 2
            top shouldBe 2
            resItems.toList shouldBe items.reverse
            resWidth shouldBe (width - 10)
            resValue shouldBe items.lastOption.getOrElse("")
        }))(),
        
        <(horizontalLineComp)(^.assertPlain[HorizontalLineProps](inside(_) {
          case HorizontalLineProps(resLeft, resTop, resLength, lineCh, resStyle, startCh, endCh) =>
            resLeft shouldBe 0
            resTop shouldBe 3
            resLength shouldBe (width - 6)
            lineCh shouldBe SingleChars.horizontal
            resStyle shouldBe style
            startCh shouldBe DoubleChars.leftSingle
            endCh shouldBe DoubleChars.rightSingle
        }))(),
        <(checkBoxComp)(^.assertPlain[CheckBoxProps](inside(_) {
          case CheckBoxProps(left, top, resValue, resLabel, resStyle, _) =>
            left shouldBe 2
            top shouldBe 4
            resValue shouldBe false
            resLabel shouldBe "Process multiple names"
            resStyle shouldBe style
        }))(),

        <(horizontalLineComp)(^.assertPlain[HorizontalLineProps](inside(_) {
          case HorizontalLineProps(resLeft, resTop, resLength, lineCh, resStyle, startCh, endCh) =>
            resLeft shouldBe 0
            resTop shouldBe 5
            resLength shouldBe (width - 6)
            lineCh shouldBe SingleChars.horizontal
            resStyle shouldBe style
            startCh shouldBe DoubleChars.leftSingle
            endCh shouldBe DoubleChars.rightSingle
        }))(),
        <(buttonsPanelComp)(^.assertPlain[ButtonsPanelProps](inside(_) {
          case ButtonsPanelProps(top, resActions, resStyle, padding, margin) =>
            top shouldBe 6
            resActions.map(_.label).toList shouldBe actions
            resStyle shouldBe style
            padding shouldBe js.undefined
            margin shouldBe 2
        }))()
      )
    )
  }
}
