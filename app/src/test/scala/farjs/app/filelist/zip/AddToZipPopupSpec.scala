package farjs.app.filelist.zip

import farjs.app.filelist.zip.AddToZipPopup._
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalProps
import farjs.ui.theme.Theme
import scommons.react.test._

class AddToZipPopupSpec extends TestSpec with TestRendererUtils {

  AddToZipPopup.modalComp = mockUiComponent("Modal")
  AddToZipPopup.textLineComp = mockUiComponent("TextLine")
  AddToZipPopup.textBoxComp = mockUiComponent("TextBox")
  AddToZipPopup.horizontalLineComp = mockUiComponent("HorizontalLine")
  AddToZipPopup.buttonsPanelComp = mockUiComponent("ButtonsPanel")

  it should "set zipName when onChange in TextBox" in {
    //given
    val zipName = "initial zip name"
    val props = AddToZipPopupProps(zipName, _ => (), () => ())
    val renderer = createTestRenderer(<(AddToZipPopup())(^.wrapped := props)())
    val textBox = findComponentProps(renderer.root, textBoxComp)
    textBox.value shouldBe zipName
    val newZipName = "new zip name"

    //when
    textBox.onChange(newZipName)

    //then
    findComponentProps(renderer.root, textBoxComp).value shouldBe newZipName
  }
  
  it should "call onAdd when onEnter in TextBox" in {
    //given
    val onAdd = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = AddToZipPopupProps("test", onAdd, onCancel)
    val comp = testRender(<(AddToZipPopup())(^.wrapped := props)())
    val textBox = findComponentProps(comp, textBoxComp)

    //then
    onAdd.expects("test")
    onCancel.expects().never()

    //when
    textBox.onEnter()
  }
  
  it should "call onAdd when press Add button" in {
    //given
    val onAdd = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = AddToZipPopupProps("test", onAdd, onCancel)
    val comp = testRender(<(AddToZipPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions.head

    //then
    onAdd.expects("test")
    onCancel.expects().never()
    
    //when
    onPress()
  }
  
  it should "not call onAdd if zipName is empty" in {
    //given
    val onAdd = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = AddToZipPopupProps("", onAdd, onCancel)
    val comp = testRender(<(AddToZipPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions.head

    //then
    onAdd.expects(*).never()
    onCancel.expects().never()
    
    //when
    onPress()
  }
  
  it should "call onCancel when press Cancel button" in {
    //given
    val onAdd = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = AddToZipPopupProps("", onAdd, onCancel)
    val comp = testRender(<(AddToZipPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions(1)

    //then
    onAdd.expects(*).never()
    onCancel.expects()
    
    //when
    onPress()
  }
  
  it should "render component" in {
    //given
    val props = AddToZipPopupProps("test zip", _ => (), () => ())

    //when
    val result = testRender(<(AddToZipPopup())(^.wrapped := props)())

    //then
    assertAddToZipPopup(result, props, List("[ Add ]", "[ Cancel ]"))
  }

  private def assertAddToZipPopup(result: TestInstance,
                                  props: AddToZipPopupProps,
                                  actions: List[String]): Unit = {
    val (width, height) = (75, 8)
    val style = Theme.current.popup.regular

    assertNativeComponent(result,
      <(modalComp())(^.assertWrapped(inside(_) {
        case ModalProps(title, size, resStyle, onCancel) =>
          title shouldBe "Add files to archive"
          size shouldBe width -> height
          resStyle shouldBe style
          onCancel should be theSameInstanceAs props.onCancel
      }))(
        <(textLineComp())(^.assertWrapped(inside(_) {
          case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
            align shouldBe TextLine.Left
            pos shouldBe 2 -> 1
            resWidth shouldBe (width - 10)
            text shouldBe "Add to zip archive:"
            resStyle shouldBe style
            focused shouldBe false
            padding shouldBe 0
        }))(),
        <(textBoxComp())(^.assertWrapped(inside(_) {
          case TextBoxProps(pos, resWidth, resValue, _, _) =>
            pos shouldBe 2 -> 2
            resWidth shouldBe (width - 10)
            resValue shouldBe props.zipName
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
        <(buttonsPanelComp())(^.assertWrapped(inside(_) {
          case ButtonsPanelProps(top, resActions, resStyle, padding, margin) =>
            top shouldBe 4
            resActions.map(_._1) shouldBe actions
            resStyle shouldBe style
            padding shouldBe 0
            margin shouldBe 2
        }))()
      )
    )
  }
}
