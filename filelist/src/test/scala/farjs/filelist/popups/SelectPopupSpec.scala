package farjs.filelist.popups

import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.popups.SelectPopup._
import farjs.ui._
import farjs.ui.popup.ModalProps
import farjs.ui.theme.Theme
import scommons.react.test._

class SelectPopupSpec extends TestSpec with TestRendererUtils {

  SelectPopup.modalComp = mockUiComponent("Modal")
  SelectPopup.textBoxComp = mockUiComponent("TextBox")

  it should "set pattern when onChange in TextBox" in {
    //given
    val pattern = "initial pattern"
    val props = SelectPopupProps(pattern, ShowSelect, _ => (), () => ())
    val renderer = createTestRenderer(<(SelectPopup())(^.wrapped := props)())
    val textBox = findComponentProps(renderer.root, textBoxComp, plain = true)
    textBox.value shouldBe pattern
    val newPattern = "new pattern"

    //when
    textBox.onChange(newPattern)

    //then
    findComponentProps(renderer.root, textBoxComp, plain = true).value shouldBe newPattern
  }
  
  it should "call onAction when onEnter in TextBox" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = SelectPopupProps("test", ShowSelect, onAction, onCancel)
    val comp = testRender(<(SelectPopup())(^.wrapped := props)())
    val textBox = findComponentProps(comp, textBoxComp, plain = true)

    //then
    onAction.expects("test")

    //when
    textBox.onEnter.get.apply()
  }
  
  it should "not call onAction if pattern is empty" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = SelectPopupProps("", ShowSelect, onAction, onCancel)
    val comp = testRender(<(SelectPopup())(^.wrapped := props)())
    val textBox = findComponentProps(comp, textBoxComp, plain = true)

    //then
    onAction.expects(*).never()
    
    //when
    textBox.onEnter.get.apply()
  }
  
  it should "render Select component" in {
    //given
    val props = SelectPopupProps("test folder", ShowSelect, _ => (), () => ())

    //when
    val result = testRender(<(SelectPopup())(^.wrapped := props)())

    //then
    assertSelectPopup(result, props, "Select")
  }

  it should "render Deselect component" in {
    //given
    val props = SelectPopupProps("test folder", ShowDeselect, _ => (), () => ())

    //when
    val result = testRender(<(SelectPopup())(^.wrapped := props)())

    //then
    assertSelectPopup(result, props, "Deselect")
  }

  private def assertSelectPopup(result: TestInstance,
                                props: SelectPopupProps,
                                expectedTitle: String): Unit = {

    val (width, height) = (55, 5)
    val style = Theme.current.popup.regular
    
    assertNativeComponent(result,
      <(modalComp())(^.assertWrapped(inside(_) {
        case ModalProps(title, size, resStyle, onCancel) =>
          title shouldBe expectedTitle
          size shouldBe width -> height
          resStyle shouldBe style
          onCancel should be theSameInstanceAs props.onCancel
      }))(
        <(textBoxComp())(^.assertPlain[TextBoxProps](inside(_) {
          case TextBoxProps(left, top, resWidth, resValue, _, _) =>
            left shouldBe 2
            top shouldBe 1
            resWidth shouldBe (width - 10)
            resValue shouldBe props.pattern
        }))()
      )
    )
  }
}
