package farjs.file.popups

import farjs.file.popups.TextSearchPopup._
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalProps
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js

class TextSearchPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  TextSearchPopup.modalComp = "Modal".asInstanceOf[ReactClass]
  TextSearchPopup.textLineComp = "TextLine".asInstanceOf[ReactClass]
  TextSearchPopup.comboBoxComp = "ComboBox".asInstanceOf[ReactClass]
  TextSearchPopup.horizontalLineComp = "HorizontalLine".asInstanceOf[ReactClass]
  TextSearchPopup.buttonsPanelComp = "ButtonsPanel".asInstanceOf[ReactClass]

  it should "call onCancel when onCancel in modal" in {
    //given
    val onCancel = mockFunction[Unit]
    val props = getTextSearchPopupProps(onCancel = onCancel)

    val renderer = createTestRenderer(
      withThemeContext(<(TextSearchPopup())(^.wrapped := props)())
    )
    val modal = inside(findComponents(renderer.root, modalComp)) {
      case List(modal) => modal.props.asInstanceOf[ModalProps]
    }

    //then
    onCancel.expects()

    //when
    modal.onCancel()

    Succeeded
  }

  it should "set searchText when onChange in ComboBox" in {
    //given
    val props = getTextSearchPopupProps()

    val renderer = createTestRenderer(
      withThemeContext(<(TextSearchPopup())(^.wrapped := props)())
    )
    val comboBox = inside(findComponents(renderer.root, comboBoxComp)) {
      case List(c) => c.props.asInstanceOf[ComboBoxProps]
    }
    val newSearchText = "new search text"

    //when
    comboBox.onChange(newSearchText)

    //then
    inside(findComponents(renderer.root, comboBoxComp)) {
      case List(c) => c.props.asInstanceOf[ComboBoxProps].value shouldBe newSearchText
    }
  }
  
  it should "call onSearch when onEnter in ComboBox" in {
    //given
    val onSearch = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = getTextSearchPopupProps(onSearch, onCancel)

    val comp = createTestRenderer(
      withThemeContext(<(TextSearchPopup())(^.wrapped := props)())
    ).root
    inside(findComponents(comp, comboBoxComp)) {
      case List(c) => c.props.asInstanceOf[ComboBoxProps].onChange("test")
    }

    //then
    onSearch.expects("test")
    onCancel.expects().never()

    //when
    inside(findComponents(comp, comboBoxComp)) {
      case List(c) => c.props.asInstanceOf[ComboBoxProps].onEnter.get.apply()
    }

    Succeeded
  }
  
  it should "call onSearch when press OK button" in {
    //given
    val onSearch = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = getTextSearchPopupProps(onSearch, onCancel)

    val comp = createTestRenderer(
      withThemeContext(<(TextSearchPopup())(^.wrapped := props)())
    ).root
    val comboBox = inside(findComponents(comp, comboBoxComp)) {
      case List(c) => c.props.asInstanceOf[ComboBoxProps]
    }
    comboBox.onChange("test")
    val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    val action = buttonsProps.actions.head

    //then
    onSearch.expects("test")
    onCancel.expects().never()

    //when
    action.onAction()

    Succeeded
  }
  
  it should "not call onSearch if searchText is empty" in {
    //given
    val onSearch = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = getTextSearchPopupProps(onSearch, onCancel)

    val comp = createTestRenderer(
      withThemeContext(<(TextSearchPopup())(^.wrapped := props)())
    ).root
    val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    val action = buttonsProps.actions.head

    //then
    onSearch.expects(*).never()
    onCancel.expects().never()

    //when
    action.onAction()

    Succeeded
  }
  
  it should "call onCancel when press Cancel button" in {
    //given
    val onSearch = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = getTextSearchPopupProps(onSearch = onSearch, onCancel = onCancel)

    val comp = createTestRenderer(
      withThemeContext(<(TextSearchPopup())(^.wrapped := props)())
    ).root
    val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    val action = buttonsProps.actions(1)

    //then
    onSearch.expects(*).never()
    onCancel.expects()

    //when
    action.onAction()

    Succeeded
  }
  
  it should "render component" in {
    //given
    val props = getTextSearchPopupProps()

    //when
    val result = createTestRenderer(
      withThemeContext(<(TextSearchPopup())(^.wrapped := props)())
    ).root

    //then
    result.children.toList should not be empty
    assertTextSearchPopup(result.children(0), Nil, List("[ Search ]", "[ Cancel ]"))
  }

  private def getTextSearchPopupProps(onSearch: String => Unit = _ => (),
                                      onCancel: () => Unit = () => ()): TextSearchPopupProps = {
    TextSearchPopupProps(
      onSearch = onSearch,
      onCancel = onCancel
    )
  }

  private def assertTextSearchPopup(result: TestInstance,
                                    items: List[String],
                                    actions: List[String]): Assertion = {
    val (width, height) = (75, 8)
    val style = DefaultTheme.popup.regular
    
    assertNativeComponent(result,
      <(modalComp)(^.assertPlain[ModalProps](inside(_) {
        case ModalProps(title, resWidth, resHeight, resStyle, _) =>
          title shouldBe "Search"
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
            text shouldBe "Search for"
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

        <(buttonsPanelComp)(^.assertPlain[ButtonsPanelProps](inside(_) {
          case ButtonsPanelProps(top, resActions, resStyle, padding, margin) =>
            top shouldBe 4
            resActions.map(_.label).toList shouldBe actions
            resStyle shouldBe style
            padding shouldBe js.undefined
            margin shouldBe 2
        }))()
      )
    )
  }
}
