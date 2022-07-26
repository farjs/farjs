package farjs.filelist.popups

import farjs.filelist.popups.MakeFolderPopup._
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalProps
import farjs.ui.theme.Theme
import scommons.react.test._

import scala.scalajs.js

class MakeFolderPopupSpec extends TestSpec with TestRendererUtils {

  MakeFolderPopup.modalComp = mockUiComponent("Modal")
  MakeFolderPopup.textLineComp = mockUiComponent("TextLine")
  MakeFolderPopup.textBoxComp = mockUiComponent("TextBox")
  MakeFolderPopup.horizontalLineComp = mockUiComponent("HorizontalLine")
  MakeFolderPopup.checkBoxComp = mockUiComponent("CheckBox")
  MakeFolderPopup.buttonsPanelComp = mockUiComponent("ButtonsPanel")

  it should "set folderName when onChange in TextBox" in {
    //given
    val folderName = "initial folder name"
    val props = MakeFolderPopupProps(folderName, multiple = false, (_, _) => (), () => ())
    val renderer = createTestRenderer(<(MakeFolderPopup())(^.wrapped := props)())
    val textBox = findComponentProps(renderer.root, textBoxComp, plain = true)
    textBox.value shouldBe folderName
    val newFolderName = "new folder name"

    //when
    textBox.onChange(newFolderName)

    //then
    findComponentProps(renderer.root, textBoxComp, plain = true).value shouldBe newFolderName
  }
  
  it should "set multiple flag when onChange in CheckBox" in {
    //given
    val props = MakeFolderPopupProps("", multiple = false, (_, _) => (), () => ())
    val renderer = createTestRenderer(<(MakeFolderPopup())(^.wrapped := props)())
    val checkbox = findComponentProps(renderer.root, checkBoxComp, plain = true)
    checkbox.value shouldBe false

    //when
    checkbox.onChange()

    //then
    findComponentProps(renderer.root, checkBoxComp, plain = true).value shouldBe true
  }
  
  it should "call onOk when onEnter in TextBox" in {
    //given
    val onOk = mockFunction[String, Boolean, Unit]
    val onCancel = mockFunction[Unit]
    val props = MakeFolderPopupProps("test", multiple = true, onOk, onCancel)
    val comp = testRender(<(MakeFolderPopup())(^.wrapped := props)())
    val textBox = findComponentProps(comp, textBoxComp, plain = true)

    //then
    onOk.expects("test", true)
    onCancel.expects().never()

    //when
    textBox.onEnter.get.apply()
  }
  
  it should "call onOk when press OK button" in {
    //given
    val onOk = mockFunction[String, Boolean, Unit]
    val onCancel = mockFunction[Unit]
    val props = MakeFolderPopupProps("test", multiple = true, onOk, onCancel)
    val comp = testRender(<(MakeFolderPopup())(^.wrapped := props)())
    val action = findComponentProps(comp, buttonsPanelComp, plain = true).actions.head

    //then
    onOk.expects("test", true)
    onCancel.expects().never()
    
    //when
    action.onAction()
  }
  
  it should "not call onOk if folderName is empty" in {
    //given
    val onOk = mockFunction[String, Boolean, Unit]
    val onCancel = mockFunction[Unit]
    val props = MakeFolderPopupProps("", multiple = true, onOk, onCancel)
    val comp = testRender(<(MakeFolderPopup())(^.wrapped := props)())
    val action = findComponentProps(comp, buttonsPanelComp, plain = true).actions.head

    //then
    onOk.expects(*, *).never()
    onCancel.expects().never()
    
    //when
    action.onAction()
  }
  
  it should "call onCancel when press Cancel button" in {
    //given
    val onOk = mockFunction[String, Boolean, Unit]
    val onCancel = mockFunction[Unit]
    val props = MakeFolderPopupProps("", multiple = false, onOk = onOk, onCancel = onCancel)
    val comp = testRender(<(MakeFolderPopup())(^.wrapped := props)())
    val action = findComponentProps(comp, buttonsPanelComp, plain = true).actions(1)

    //then
    onOk.expects(*, *).never()
    onCancel.expects()
    
    //when
    action.onAction()
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
    
    assertNativeComponent(result,
      <(modalComp())(^.assertWrapped(inside(_) {
        case ModalProps(title, size, resStyle, onCancel) =>
          title shouldBe "Make Folder"
          size shouldBe width -> height
          resStyle shouldBe style
          onCancel should be theSameInstanceAs props.onCancel
      }))(
        <(textLineComp())(^.assertWrapped(inside(_) {
          case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
            align shouldBe TextLine.Left
            pos shouldBe 2 -> 1
            resWidth shouldBe (width - 10)
            text shouldBe "Create the folder"
            resStyle shouldBe style
            focused shouldBe false
            padding shouldBe 0
        }))(),
        <(textBoxComp())(^.assertPlain[TextBoxProps](inside(_) {
          case TextBoxProps(left, top, resWidth, resValue, _, _) =>
            left shouldBe 2
            top shouldBe 2
            resWidth shouldBe (width - 10)
            resValue shouldBe props.folderName
        }))(),
        
        <(horizontalLineComp())(^.assertWrapped(inside(_) {
          case HorizontalLineProps(pos, resLength, lineCh, resStyle, startCh, endCh) =>
            pos shouldBe 0 -> 3
            resLength shouldBe (width - 6)
            lineCh shouldBe SingleBorder.horizontalCh
            resStyle shouldBe style
            startCh shouldBe Some(DoubleBorder.leftSingleCh)
            endCh shouldBe Some(DoubleBorder.rightSingleCh)
        }))(),
        <(checkBoxComp())(^.assertPlain[CheckBoxProps](inside(_) {
          case CheckBoxProps(left, top, resValue, resLabel, resStyle, _) =>
            left shouldBe 2
            top shouldBe 4
            resValue shouldBe false
            resLabel shouldBe "Process multiple names"
            resStyle shouldBe style
        }))(),

        <(horizontalLineComp())(^.assertWrapped(inside(_) {
          case HorizontalLineProps(pos, resLength, lineCh, resStyle, startCh, endCh) =>
            pos shouldBe 0 -> 5
            resLength shouldBe (width - 6)
            lineCh shouldBe SingleBorder.horizontalCh
            resStyle shouldBe style
            startCh shouldBe Some(DoubleBorder.leftSingleCh)
            endCh shouldBe Some(DoubleBorder.rightSingleCh)
        }))(),
        <(buttonsPanelComp())(^.assertPlain[ButtonsPanelProps](inside(_) {
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
