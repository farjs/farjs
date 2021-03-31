package farjs.filelist.popups

import farjs.filelist.popups.MakeFolderPopup._
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.PopupProps
import farjs.ui.theme.Theme
import org.scalatest.Assertion
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

class MakeFolderPopupSpec extends TestSpec with TestRendererUtils {

  MakeFolderPopup.popupComp = () => "Popup".asInstanceOf[ReactClass]
  MakeFolderPopup.doubleBorderComp = () => "DoubleBorder".asInstanceOf[ReactClass]
  MakeFolderPopup.textLineComp = () => "TextLine".asInstanceOf[ReactClass]
  MakeFolderPopup.textBoxComp = () => "TextBox".asInstanceOf[ReactClass]
  MakeFolderPopup.horizontalLineComp = () => "HorizontalLine".asInstanceOf[ReactClass]
  MakeFolderPopup.checkBoxComp = () => "CheckBox".asInstanceOf[ReactClass]
  MakeFolderPopup.buttonsPanelComp = () => "ButtonsPanel".asInstanceOf[ReactClass]

  it should "call onCancel when close popup" in {
    //given
    val onOk = mockFunction[String, Boolean, Unit]
    val onCancel = mockFunction[Unit]
    val props = MakeFolderPopupProps("", multiple = false, onOk = onOk, onCancel = onCancel)
    val comp = testRender(<(MakeFolderPopup())(^.wrapped := props)())
    val popup = findComponentProps(comp, popupComp)

    //then
    onOk.expects(*, *).never()
    onCancel.expects()
    
    //when
    popup.onClose()
  }
  
  it should "set folderName when onChange in TextBox" in {
    //given
    val folderName = "initial folder name"
    val props = MakeFolderPopupProps(folderName, multiple = false, (_, _) => (), () => ())
    val renderer = createTestRenderer(<(MakeFolderPopup())(^.wrapped := props)())
    val textBox = findComponentProps(renderer.root, textBoxComp)
    textBox.value shouldBe folderName
    val newFolderName = "new folder name"

    //when
    textBox.onChange(newFolderName)

    //then
    findComponentProps(renderer.root, textBoxComp).value shouldBe newFolderName
  }
  
  it should "set multiple flag when onChange in CheckBox" in {
    //given
    val props = MakeFolderPopupProps("", multiple = false, (_, _) => (), () => ())
    val renderer = createTestRenderer(<(MakeFolderPopup())(^.wrapped := props)())
    val checkbox = findComponentProps(renderer.root, checkBoxComp)
    checkbox.value shouldBe false

    //when
    checkbox.onChange()

    //then
    findComponentProps(renderer.root, checkBoxComp).value shouldBe true
  }
  
  it should "call onOk when onEnter in TextBox" in {
    //given
    val onOk = mockFunction[String, Boolean, Unit]
    val onCancel = mockFunction[Unit]
    val props = MakeFolderPopupProps("test", multiple = true, onOk, onCancel)
    val comp = testRender(<(MakeFolderPopup())(^.wrapped := props)())
    val textBox = findComponentProps(comp, textBoxComp)

    //then
    onOk.expects("test", true)
    onCancel.expects().never()

    //when
    textBox.onEnter()
  }
  
  it should "call onOk when press OK button" in {
    //given
    val onOk = mockFunction[String, Boolean, Unit]
    val onCancel = mockFunction[Unit]
    val props = MakeFolderPopupProps("test", multiple = true, onOk, onCancel)
    val comp = testRender(<(MakeFolderPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions.head

    //then
    onOk.expects("test", true)
    onCancel.expects().never()
    
    //when
    onPress()
  }
  
  it should "not call onOk if folderName is empty" in {
    //given
    val onOk = mockFunction[String, Boolean, Unit]
    val onCancel = mockFunction[Unit]
    val props = MakeFolderPopupProps("", multiple = true, onOk, onCancel)
    val comp = testRender(<(MakeFolderPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions.head

    //then
    onOk.expects(*, *).never()
    onCancel.expects().never()
    
    //when
    onPress()
  }
  
  it should "call onCancel when press Cancel button" in {
    //given
    val onOk = mockFunction[String, Boolean, Unit]
    val onCancel = mockFunction[Unit]
    val props = MakeFolderPopupProps("", multiple = false, onOk = onOk, onCancel = onCancel)
    val comp = testRender(<(MakeFolderPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions(1)

    //then
    onOk.expects(*, *).never()
    onCancel.expects()
    
    //when
    onPress()
  }
  
  it should "render component" in {
    //given
    val props = MakeFolderPopupProps("test folder", multiple = false, (_, _) => (), () => ())

    //when
    val result = testRender(<(MakeFolderPopup())(^.wrapped := props)())

    //then
    assertMakeFolderPopup(result, props, List("[ OK ]", "[ Cancel ]"))
  }

  private def assertMakeFolderPopup(result: TestInstance,
                                    props: MakeFolderPopupProps,
                                    actions: List[String]): Unit = {
    val (width, height) = (75, 10)
    val style = Theme.current.popup.regular
    
    def assertComponents(border: TestInstance,
                         label: TestInstance,
                         input: TestInstance,
                         sep1: TestInstance,
                         multi: TestInstance,
                         sep2: TestInstance,
                         actionsBox: TestInstance): Assertion = {

      assertTestComponent(border, doubleBorderComp) {
        case DoubleBorderProps(resSize, resStyle, pos, title) =>
          resSize shouldBe (width - 6) -> (height - 2)
          resStyle shouldBe style
          pos shouldBe 3 -> 1
          title shouldBe Some("Make Folder")
      }
      
      assertTestComponent(label, textLineComp) {
        case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextLine.Left
          pos shouldBe 4 -> 2
          resWidth shouldBe (width - 8)
          text shouldBe "Create the folder"
          resStyle shouldBe style
          focused shouldBe false
          padding shouldBe 1
      }
      assertTestComponent(input, textBoxComp) {
        case TextBoxProps(pos, resWidth, resValue, _, _) =>
          pos shouldBe 5 -> 3
          resWidth shouldBe (width - 10)
          resValue shouldBe props.folderName
      }
      assertTestComponent(sep1, horizontalLineComp) {
        case HorizontalLineProps(pos, resLength, lineCh, resStyle, startCh, endCh) =>
          pos shouldBe 3 -> 4
          resLength shouldBe (width - 6)
          lineCh shouldBe SingleBorder.horizontalCh
          resStyle shouldBe style
          startCh shouldBe Some(DoubleBorder.leftSingleCh)
          endCh shouldBe Some(DoubleBorder.rightSingleCh)
      }
      
      assertTestComponent(multi, checkBoxComp) {
        case CheckBoxProps(pos, resValue, resLabel, resStyle, _) =>
          pos shouldBe 5 -> 5
          resValue shouldBe false
          resLabel shouldBe "Process multiple names"
          resStyle shouldBe style
      }
      assertTestComponent(sep2, horizontalLineComp) {
        case HorizontalLineProps(pos, resLength, lineCh, resStyle, startCh, endCh) =>
          pos shouldBe 3 -> 6
          resLength shouldBe (width - 6)
          lineCh shouldBe SingleBorder.horizontalCh
          resStyle shouldBe style
          startCh shouldBe Some(DoubleBorder.leftSingleCh)
          endCh shouldBe Some(DoubleBorder.rightSingleCh)
      }

      assertTestComponent(actionsBox, buttonsPanelComp) {
        case ButtonsPanelProps(top, resActions, resStyle, padding, margin) =>
          top shouldBe (height - 3)
          resActions.map(_._1) shouldBe actions
          resStyle shouldBe style
          padding shouldBe 0
          margin shouldBe 2
      }
    }
    
    assertTestComponent(result, popupComp)({ case PopupProps(_, resClosable, focusable, _) =>
      resClosable shouldBe true
      focusable shouldBe true
    }, inside(_) { case List(box) =>
      assertNativeComponent(box,
        <.box(
          ^.rbClickable := true,
          ^.rbAutoFocus := false,
          ^.rbWidth := width,
          ^.rbHeight := height,
          ^.rbTop := "center",
          ^.rbLeft := "center",
          ^.rbShadow := true,
          ^.rbStyle := style
        )(), inside(_) {
          case List(border, label, input, sep1, multi, sep2, actionsBox) =>
            assertComponents(border, label, input, sep1, multi, sep2, actionsBox)
        }
      )
    })
  }
}
