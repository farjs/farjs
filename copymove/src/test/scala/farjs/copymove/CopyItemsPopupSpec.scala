package farjs.copymove

import farjs.copymove.CopyItemsPopup._
import farjs.copymove.CopyMoveUi.copyItemsHistoryKind
import farjs.filelist.api.FileListItem
import farjs.filelist.history.HistoryProviderSpec.withHistoryProvider
import farjs.filelist.history._
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

import scala.scalajs.js

class CopyItemsPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  CopyItemsPopup.modalComp = "Modal".asInstanceOf[ReactClass]
  CopyItemsPopup.textLineComp = "TextLine".asInstanceOf[ReactClass]
  CopyItemsPopup.comboBoxComp = "ComboBox".asInstanceOf[ReactClass]
  CopyItemsPopup.horizontalLineComp = "HorizontalLine".asInstanceOf[ReactClass]
  CopyItemsPopup.buttonsPanelComp = "ButtonsPanel".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class HistoryMocks {
    val get = mockFunction[HistoryKind, js.Promise[HistoryService]]
    val getAll = mockFunction[js.Promise[js.Array[History]]]

    val service = new MockHistoryService(
      getAllMock = getAll
    )
    val provider = new MockHistoryProvider(
      getMock = get
    )
  }

  it should "call onCancel when onCancel in modal" in {
    //given
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps(move = false, "path", js.Array(FileListItem("file 1")), _ => (), onCancel)
    val historyMocks = new HistoryMocks
    val items = List("path", "path 2")
    val itemsF = js.Promise.resolve[js.Array[History]](js.Array(items.map(i => History(i, js.undefined)): _*))
    var getAllCalled = false
    historyMocks.get.expects(copyItemsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }

    val renderer = createTestRenderer(withHistoryProvider(
      withThemeContext(<(CopyItemsPopup())(^.plain := props)()), historyMocks.provider
    ))
    eventually(getAllCalled shouldBe true).map { _ =>
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
  
  it should "set path when onChange in TextBox" in {
    //given
    val path = "initial path"
    val props = CopyItemsPopupProps(move = false, path, js.Array(FileListItem("file 1")), _ => (), () => ())
    val historyMocks = new HistoryMocks
    val items = List("path", "path 2")
    val itemsF = js.Promise.resolve[js.Array[History]](js.Array(items.map(i => History(i, js.undefined)): _*))
    var getAllCalled = false
    historyMocks.get.expects(copyItemsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }

    val renderer = createTestRenderer(withHistoryProvider(
      withThemeContext(<(CopyItemsPopup())(^.plain := props)()), historyMocks.provider
    ))
    eventually(getAllCalled shouldBe true).map { _ =>
      val comboBox = inside(findComponents(renderer.root, comboBoxComp)) {
        case List(c) => c.props.asInstanceOf[ComboBoxProps]
      }
      comboBox.value shouldBe path
      val newFolderName = "new path"

      //when
      comboBox.onChange(newFolderName)

      //then
      inside(findComponents(renderer.root, comboBoxComp)) {
        case List(c) => c.props.asInstanceOf[ComboBoxProps].value shouldBe newFolderName
      }
    }
  }
  
  it should "call onAction when onEnter in TextBox" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps(move = false, "test", js.Array(FileListItem("file 1")), onAction, onCancel)
    val historyMocks = new HistoryMocks
    val items = List("path", "path 2")
    val itemsF = js.Promise.resolve[js.Array[History]](js.Array(items.map(i => History(i, js.undefined)): _*))
    var getAllCalled = false
    historyMocks.get.expects(copyItemsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }

    val comp = createTestRenderer(withHistoryProvider(
      withThemeContext(<(CopyItemsPopup())(^.plain := props)()), historyMocks.provider
    )).root
    eventually(getAllCalled shouldBe true).map { _ =>
      val comboBox = inside(findComponents(comp, comboBoxComp)) {
        case List(c) => c.props.asInstanceOf[ComboBoxProps]
      }

      //then
      onAction.expects("test")
      onCancel.expects().never()

      //when
      comboBox.onEnter.get.apply()

      Succeeded
    }
  }
  
  it should "call onAction when press action button" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps(move = false, "test", js.Array(FileListItem("file 1")), onAction, onCancel)
    val historyMocks = new HistoryMocks
    val items = List("path", "path 2")
    val itemsF = js.Promise.resolve[js.Array[History]](js.Array(items.map(i => History(i, js.undefined)): _*))
    var getAllCalled = false
    historyMocks.get.expects(copyItemsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }

    val comp = createTestRenderer(withHistoryProvider(
      withThemeContext(<(CopyItemsPopup())(^.plain := props)()), historyMocks.provider
    )).root
    eventually(getAllCalled shouldBe true).map { _ =>
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
    val props = CopyItemsPopupProps(move = false, "", js.Array(FileListItem("file 1")), onAction, onCancel)
    val historyMocks = new HistoryMocks
    val items = List("path", "path 2")
    val itemsF = js.Promise.resolve[js.Array[History]](js.Array(items.map(i => History(i, js.undefined)): _*))
    var getAllCalled = false
    historyMocks.get.expects(copyItemsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }

    val comp = createTestRenderer(withHistoryProvider(
      withThemeContext(<(CopyItemsPopup())(^.plain := props)()), historyMocks.provider
    )).root
    eventually(getAllCalled shouldBe true).map { _ =>
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
    val props = CopyItemsPopupProps(move = false, "", js.Array(FileListItem("file 1")), onAction, onCancel)
    val historyMocks = new HistoryMocks
    val items = List("path", "path 2")
    val itemsF = js.Promise.resolve[js.Array[History]](js.Array(items.map(i => History(i, js.undefined)): _*))
    var getAllCalled = false
    historyMocks.get.expects(copyItemsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }

    val comp = createTestRenderer(withHistoryProvider(
      withThemeContext(<(CopyItemsPopup())(^.plain := props)()), historyMocks.provider
    )).root
    eventually(getAllCalled shouldBe true).map { _ =>
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
    val props = CopyItemsPopupProps(move = false, "test folder", js.Array(FileListItem("file 1")), _ => (), () => ())
    val historyMocks = new HistoryMocks
    val items = List("path", "path 2")
    val itemsF = js.Promise.resolve[js.Array[History]](js.Array(items.map(i => History(i, js.undefined)): _*))
    var getAllCalled = false
    historyMocks.get.expects(copyItemsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }

    //when
    val result = createTestRenderer(withHistoryProvider(
      withThemeContext(<(CopyItemsPopup())(^.plain := props)()), historyMocks.provider
    )).root

    //then
    eventually(getAllCalled shouldBe true).map { _ =>
      assertCopyItemsPopup(result.children(0), props, items, List("[ Copy ]", "[ Cancel ]"))
    }
  }

  it should "render component when move" in {
    //given
    val props = CopyItemsPopupProps(move = true, "test folder", js.Array(FileListItem("file 1")), _ => (), () => ())
    val historyMocks = new HistoryMocks
    val itemsF = js.Promise.resolve[js.Array[History]](js.Array[History]())
    var getAllCalled = false
    historyMocks.get.expects(copyItemsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }

    //when
    val result = createTestRenderer(withHistoryProvider(
      withThemeContext(<(CopyItemsPopup())(^.plain := props)()), historyMocks.provider
    )).root

    //then
    eventually(getAllCalled shouldBe true).map { _ =>
      assertCopyItemsPopup(result.children(0), props, Nil, List("[ Rename ]", "[ Cancel ]"))
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
      assertNativeComponent(input, <(comboBoxComp)(^.assertPlain[ComboBoxProps](inside(_) {
        case ComboBoxProps(left, top, resWidth, resItems, resValue, _, _) =>
          left shouldBe 2
          top shouldBe 2
          resWidth shouldBe (width - (paddingHorizontal + 2) * 2)
          resItems.toList shouldBe items.reverse
          resValue shouldBe props.path
      }))())

      assertNativeComponent(sep, <(horizontalLineComp)(^.assertPlain[HorizontalLineProps](inside(_) {
        case HorizontalLineProps(resLeft, resTop, resLength, lineCh, resStyle, startCh, endCh) =>
          resLeft shouldBe 0
          resTop shouldBe 3
          resLength shouldBe (width - paddingHorizontal * 2)
          lineCh shouldBe SingleChars.horizontal
          resStyle shouldBe style
          startCh shouldBe DoubleChars.leftSingle
          endCh shouldBe DoubleChars.rightSingle
      }))())
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
    
    assertNativeComponent(result, <(modalComp)(^.assertPlain[ModalProps](inside(_) {
      case ModalProps(resTitle, resWidth, resHeight, resStyle, _) =>
        resTitle shouldBe title
        resWidth shouldBe width
        resHeight shouldBe height
        resStyle shouldBe style
    }))(), inside(_) { case List(label, input, sep, actionsBox) =>
      assertComponents(label, input, sep, actionsBox)
    })
  }
}
