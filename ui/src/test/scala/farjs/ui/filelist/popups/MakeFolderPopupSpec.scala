package farjs.ui.filelist.popups

import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.{Popup, PopupProps}
import org.scalatest.{Assertion, Succeeded}
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

class MakeFolderPopupSpec extends TestSpec with ShallowRendererUtils {

  it should "call onCancel when close popup" in {
    //given
    val onOk = mockFunction[String, Boolean, Unit]
    val onCancel = mockFunction[Unit]
    val props = MakeFolderPopupProps("", multiple = false, onOk = onOk, onCancel = onCancel)
    val comp = shallowRender(<(MakeFolderPopup())(^.wrapped := props)())
    val popup = findComponentProps(comp, Popup)

    //then
    onOk.expects(*, *).never()
    onCancel.expects()
    
    //when
    popup.onClose()
  }
  
  it should "set multiple flag when onChange in checkbox" in {
    //given
    val props = MakeFolderPopupProps("", multiple = false, (_, _) => (), () => ())
    val renderer = createRenderer()
    renderer.render(<(MakeFolderPopup())(^.wrapped := props)())
    val checkbox = findComponentProps(renderer.getRenderOutput(), CheckBox)
    checkbox.value shouldBe false

    //when
    checkbox.onChange()

    //then
    findComponentProps(renderer.getRenderOutput(), CheckBox).value shouldBe true
  }
  
  ignore should "call onOk when press OK button" in {
    //given
    val onOk = mockFunction[String, Boolean, Unit]
    val onCancel = mockFunction[Unit]
    val props = MakeFolderPopupProps("", multiple = false, onOk = onOk, onCancel = onCancel)
    val comp = shallowRender(<(MakeFolderPopup())(^.wrapped := props)())
    val okButton = findComponents(comp, "button").head

    //then
    onOk.expects("", false)
    onCancel.expects().never()
    
    //when
    okButton.props.onPress()
  }
  
  it should "call onCancel when press Cancel button" in {
    //given
    val onOk = mockFunction[String, Boolean, Unit]
    val onCancel = mockFunction[Unit]
    val props = MakeFolderPopupProps("", multiple = false, onOk = onOk, onCancel = onCancel)
    val comp = shallowRender(<(MakeFolderPopup())(^.wrapped := props)())
    val cancelButton = findComponents(comp, "button")(1)

    //then
    onOk.expects(*, *).never()
    onCancel.expects()
    
    //when
    cancelButton.props.onPress()
  }
  
  it should "render component" in {
    //given
    val props = MakeFolderPopupProps("", multiple = false, (_, _) => (), () => ())

    //when
    val result = shallowRender(<(MakeFolderPopup())(^.wrapped := props)())

    //then
    assertMakeFolderPopup(result, props, List("[ OK ]" -> 0, "[ Cancel ]" -> 8))
  }

  private def assertMakeFolderPopup(result: ShallowInstance,
                                    props: MakeFolderPopupProps,
                                    actions: List[(String, Int)]): Unit = {
    val (width, height) = (75, 10)
    val style = Popup.Styles.normal
    
    def assertComponents(border: ShallowInstance,
                         label: ShallowInstance,
                         input: ShallowInstance,
                         sep1: ShallowInstance,
                         multi: ShallowInstance,
                         sep2: ShallowInstance,
                         actionsBox: ShallowInstance): Assertion = {

      assertComponent(border, DoubleBorder) {
        case DoubleBorderProps(resSize, resStyle, pos, title) =>
          resSize shouldBe (width - 6) -> (height - 2)
          resStyle shouldBe style
          pos shouldBe 3 -> 1
          title shouldBe Some("Make Folder")
      }
      
      assertComponent(label, TextLine) {
        case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextLine.Left
          pos shouldBe 4 -> 2
          resWidth shouldBe (width - 8)
          text shouldBe "Create the folder"
          resStyle shouldBe style
          focused shouldBe false
          padding shouldBe 1
      }
      assertComponent(input, TextBox) {
        case TextBoxProps(pos, resWidth, resValue, resStyle, _) =>
          pos shouldBe 5 -> 3
          resWidth shouldBe (width - 10)
          resValue shouldBe "initial folder name"
          resStyle shouldBe style.focus
      }
      assertComponent(sep1, HorizontalLine) {
        case HorizontalLineProps(pos, resLength, lineCh, resStyle, startCh, endCh) =>
          pos shouldBe 3 -> 4
          resLength shouldBe (width - 6)
          lineCh shouldBe SingleBorder.horizontalCh
          resStyle shouldBe style
          startCh shouldBe Some(DoubleBorder.leftSingleCh)
          endCh shouldBe Some(DoubleBorder.rightSingleCh)
      }
      
      assertComponent(multi, CheckBox) {
        case CheckBoxProps(pos, resValue, resLabel, resStyle, _) =>
          pos shouldBe 5 -> 5
          resValue shouldBe false
          resLabel shouldBe "Process multiple names"
          resStyle shouldBe style
      }
      assertComponent(sep2, HorizontalLine) {
        case HorizontalLineProps(pos, resLength, lineCh, resStyle, startCh, endCh) =>
          pos shouldBe 3 -> 6
          resLength shouldBe (width - 6)
          lineCh shouldBe SingleBorder.horizontalCh
          resStyle shouldBe style
          startCh shouldBe Some(DoubleBorder.leftSingleCh)
          endCh shouldBe Some(DoubleBorder.rightSingleCh)
      }

      val buttonsWidth = actions.map(_._1.length).sum + 2
      assertNativeComponent(actionsBox,
        <.box(
          ^.rbWidth := buttonsWidth,
          ^.rbHeight := 1,
          ^.rbTop := height - 3,
          ^.rbLeft := "center",
          ^.rbStyle := style
        )(), { buttons: List[ShallowInstance] =>
          buttons.size shouldBe actions.size
          buttons.zip(actions).foreach { case (btn, (action, pos)) =>
            assertNativeComponent(btn,
              <.button(
                ^.key := s"$pos",
                ^.rbMouse := true,
                ^.rbHeight := 1,
                ^.rbLeft := pos,
                ^.rbStyle := style,
                ^.content := action
              )()
            )
          }
          Succeeded
        }
      )
    }
    
    assertComponent(result, Popup)({ case PopupProps(_, resClosable, focusable, _) =>
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
