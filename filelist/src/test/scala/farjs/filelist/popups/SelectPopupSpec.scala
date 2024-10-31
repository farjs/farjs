package farjs.filelist.popups

import farjs.filelist.FileListServicesSpec.withServicesContext
import farjs.filelist.history._
import farjs.filelist.popups.SelectController.selectPatternsHistoryKind
import farjs.filelist.popups.SelectPopup._
import farjs.ui._
import farjs.ui.popup.ModalProps
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js

class SelectPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  SelectPopup.modalComp = "Modal".asInstanceOf[ReactClass]
  SelectPopup.comboBoxComp = "ComboBox".asInstanceOf[ReactClass]

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
    val props = getSelectPopupProps(showSelect = true, onCancel = onCancel)
    val historyMocks = new HistoryMocks
    val items = List("pattern", "test")
    val itemsF = js.Promise.resolve[js.Array[History]](
      js.Array(items.map(i => History(i, js.undefined)): _*)
    )
    var getAllCalled = false
    historyMocks.get.expects(selectPatternsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }

    val comp = createTestRenderer(withServicesContext(
      withThemeContext(<(SelectPopup())(^.wrapped := props)()), historyProvider = historyMocks.provider
    )).root

    eventually(getAllCalled shouldBe true).map { _ =>
      val modal = inside(findComponents(comp, modalComp)) {
        case List(modal) => modal.props.asInstanceOf[ModalProps]
      }

      //then
      onCancel.expects()

      //when
      modal.onCancel()

      Succeeded
    }
  }

  it should "set pattern when onChange in TextBox" in {
    //given
    val pattern = "initial pattern"
    val props = getSelectPopupProps(showSelect = true)
    val historyMocks = new HistoryMocks
    val items = List("pattern", pattern)
    val itemsF = js.Promise.resolve[js.Array[History]](
      js.Array(items.map(i => History(i, js.undefined)): _*)
    )
    var getAllCalled = false
    historyMocks.get.expects(selectPatternsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }

    val renderer = createTestRenderer(withServicesContext(
      withThemeContext(<(SelectPopup())(^.wrapped := props)()), historyProvider = historyMocks.provider
    ))
    eventually(getAllCalled shouldBe true).map { _ =>
      val comboBox = inside(findComponents(renderer.root, comboBoxComp)) {
        case List(c) => c.props.asInstanceOf[ComboBoxProps]
      }
      comboBox.value shouldBe pattern
      val newPattern = "new pattern"

      //when
      comboBox.onChange(newPattern)

      //then
      inside(findComponents(renderer.root, comboBoxComp)) {
        case List(c) => c.props.asInstanceOf[ComboBoxProps].value shouldBe newPattern
      }
    }
  }
  
  it should "call onAction when onEnter in TextBox" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = getSelectPopupProps(showSelect = true, onAction, onCancel)
    val historyMocks = new HistoryMocks
    val items = List("pattern", "test")
    val itemsF = js.Promise.resolve[js.Array[History]](
      js.Array(items.map(i => History(i, js.undefined)): _*)
    )
    var getAllCalled = false
    historyMocks.get.expects(selectPatternsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }

    val comp = createTestRenderer(withServicesContext(
      withThemeContext(<(SelectPopup())(^.wrapped := props)()), historyProvider = historyMocks.provider
    )).root

    eventually(getAllCalled shouldBe true).map { _ =>
      val comboBox = inside(findComponents(comp, comboBoxComp)) {
        case List(c) => c.props.asInstanceOf[ComboBoxProps]
      }

      //then
      onAction.expects("test")

      //when
      comboBox.onEnter.get.apply()

      Succeeded
    }
  }
  
  it should "not call onAction if pattern is empty" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = getSelectPopupProps(showSelect = true, onAction, onCancel)
    val historyMocks = new HistoryMocks
    val itemsF = js.Promise.resolve[js.Array[History]](js.Array[History]())
    var getAllCalled = false
    historyMocks.get.expects(selectPatternsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }

    val comp = createTestRenderer(withServicesContext(
      withThemeContext(<(SelectPopup())(^.wrapped := props)()), historyProvider = historyMocks.provider
    )).root

    eventually(getAllCalled shouldBe true).map { _ =>
      val comboBox = inside(findComponents(comp, comboBoxComp)) {
        case List(c) => c.props.asInstanceOf[ComboBoxProps]
      }

      //then
      onAction.expects(*).never()

      //when
      comboBox.onEnter.get.apply()

      Succeeded
    }
  }
  
  it should "render Select component" in {
    //given
    val props = getSelectPopupProps(showSelect = true)
    val historyMocks = new HistoryMocks
    val items = List("pattern", "pattern 2")
    val itemsF = js.Promise.resolve[js.Array[History]](
      js.Array(items.map(i => History(i, js.undefined)): _*)
    )
    var getAllCalled = false
    historyMocks.get.expects(selectPatternsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }

    //when
    val result = createTestRenderer(withServicesContext(
      withThemeContext(<(SelectPopup())(^.wrapped := props)()), historyProvider = historyMocks.provider
    )).root

    //then
    eventually(getAllCalled shouldBe true).map { _ =>
      assertSelectPopup(result.children(0), items, "Select")
    }
  }

  it should "render Deselect component" in {
    //given
    val props = getSelectPopupProps(showSelect = false)
    val historyMocks = new HistoryMocks
    val items = List("pattern", "pattern 2")
    val itemsF = js.Promise.resolve[js.Array[History]](
      js.Array(items.map(i => History(i, js.undefined)): _*)
    )
    var getAllCalled = false
    historyMocks.get.expects(selectPatternsHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }

    //when
    val result = createTestRenderer(withServicesContext(
      withThemeContext(<(SelectPopup())(^.wrapped := props)()), historyProvider = historyMocks.provider
    )).root

    //then
    eventually(getAllCalled shouldBe true).map { _ =>
      assertSelectPopup(result.children(0), items, "Deselect")
    }
  }
  
  private def getSelectPopupProps(showSelect: Boolean,
                                  onAction: String => Unit = _ => (),
                                  onCancel: () => Unit = () => ()): SelectPopupProps = {
    SelectPopupProps(
      showSelect = showSelect,
      onAction = onAction,
      onCancel = onCancel
    )
  }

  private def assertSelectPopup(result: TestInstance,
                                items: List[String],
                                expectedTitle: String): Assertion = {

    val (width, height) = (55, 5)
    val style = DefaultTheme.popup.regular
    
    assertNativeComponent(result,
      <(modalComp)(^.assertPlain[ModalProps](inside(_) {
        case ModalProps(title, resWidth, resHeight, resStyle, _) =>
          title shouldBe expectedTitle
          resWidth shouldBe width
          resHeight shouldBe height
          resStyle shouldBe style
      }))(
        <(comboBoxComp)(^.assertPlain[ComboBoxProps](inside(_) {
          case ComboBoxProps(left, top, resWidth, resItems, resValue, _, _) =>
            left shouldBe 2
            top shouldBe 1
            resWidth shouldBe (width - 10)
            resItems.toList shouldBe items.reverse
            resValue shouldBe items.lastOption.getOrElse("")
        }))()
      )
    )
  }
}
