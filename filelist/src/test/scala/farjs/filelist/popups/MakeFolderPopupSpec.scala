package farjs.filelist.popups

import farjs.filelist.popups.MakeFolderPopup._
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalProps
import farjs.ui.theme.Theme
import org.scalatest.Assertion
import scommons.react.ReactClass
import scommons.react.test._

class MakeFolderPopupSpec extends TestSpec with TestRendererUtils {

  MakeFolderPopup.modalComp = () => "Modal".asInstanceOf[ReactClass]
  MakeFolderPopup.textLineComp = () => "TextLine".asInstanceOf[ReactClass]
  MakeFolderPopup.textBoxComp = () => "TextBox".asInstanceOf[ReactClass]
  MakeFolderPopup.horizontalLineComp = () => "HorizontalLine".asInstanceOf[ReactClass]
  MakeFolderPopup.checkBoxComp = () => "CheckBox".asInstanceOf[ReactClass]
  MakeFolderPopup.buttonsPanelComp = () => "ButtonsPanel".asInstanceOf[ReactClass]

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
    
    def assertComponents(label: TestInstance,
                         input: TestInstance,
                         sep1: TestInstance,
                         multi: TestInstance,
                         sep2: TestInstance,
                         actionsBox: TestInstance): Assertion = {

      assertTestComponent(label, textLineComp) {
        case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextLine.Left
          pos shouldBe 2 -> 1
          resWidth shouldBe (width - 10)
          text shouldBe "Create the folder"
          resStyle shouldBe style
          focused shouldBe false
          padding shouldBe 0
      }
      assertTestComponent(input, textBoxComp) {
        case TextBoxProps(pos, resWidth, resValue, _, _) =>
          pos shouldBe 2 -> 2
          resWidth shouldBe (width - 10)
          resValue shouldBe props.folderName
      }
      
      assertTestComponent(sep1, horizontalLineComp) {
        case HorizontalLineProps(pos, resLength, lineCh, resStyle, startCh, endCh) =>
          pos shouldBe 0 -> 3
          resLength shouldBe (width - 6)
          lineCh shouldBe SingleBorder.horizontalCh
          resStyle shouldBe style
          startCh shouldBe Some(DoubleBorder.leftSingleCh)
          endCh shouldBe Some(DoubleBorder.rightSingleCh)
      }
      assertTestComponent(multi, checkBoxComp) {
        case CheckBoxProps(pos, resValue, resLabel, resStyle, _) =>
          pos shouldBe 2 -> 4
          resValue shouldBe false
          resLabel shouldBe "Process multiple names"
          resStyle shouldBe style
      }
      
      assertTestComponent(sep2, horizontalLineComp) {
        case HorizontalLineProps(pos, resLength, lineCh, resStyle, startCh, endCh) =>
          pos shouldBe 0 -> 5
          resLength shouldBe (width - 6)
          lineCh shouldBe SingleBorder.horizontalCh
          resStyle shouldBe style
          startCh shouldBe Some(DoubleBorder.leftSingleCh)
          endCh shouldBe Some(DoubleBorder.rightSingleCh)
      }
      assertTestComponent(actionsBox, buttonsPanelComp) {
        case ButtonsPanelProps(top, resActions, resStyle, padding, margin) =>
          top shouldBe 6
          resActions.map(_._1) shouldBe actions
          resStyle shouldBe style
          padding shouldBe 0
          margin shouldBe 2
      }
    }
    
    assertTestComponent(result, modalComp)({ case ModalProps(title, size, resStyle, onCancel) =>
      title shouldBe "Make Folder"
      size shouldBe width -> height
      resStyle shouldBe style
      onCancel should be theSameInstanceAs props.onCancel
    }, inside(_) {
      case List(label, input, sep1, multi, sep2, actionsBox) =>
        assertComponents(label, input, sep1, multi, sep2, actionsBox)
    })
  }
}
