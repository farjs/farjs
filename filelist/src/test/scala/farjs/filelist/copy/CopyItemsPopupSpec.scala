package farjs.filelist.copy

import farjs.filelist.api.FileListItem
import farjs.filelist.copy.CopyItemsPopup._
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.PopupProps
import farjs.ui.theme.Theme
import org.scalatest.Assertion
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

class CopyItemsPopupSpec extends TestSpec with TestRendererUtils {

  CopyItemsPopup.popupComp = () => "Popup".asInstanceOf[ReactClass]
  CopyItemsPopup.doubleBorderComp = () => "DoubleBorder".asInstanceOf[ReactClass]
  CopyItemsPopup.textLineComp = () => "TextLine".asInstanceOf[ReactClass]
  CopyItemsPopup.textBoxComp = () => "TextBox".asInstanceOf[ReactClass]
  CopyItemsPopup.horizontalLineComp = () => "HorizontalLine".asInstanceOf[ReactClass]
  CopyItemsPopup.buttonsPanelComp = () => "ButtonsPanel".asInstanceOf[ReactClass]

  it should "call onCancel when close popup" in {
    //given
    val onCopy = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps("", Seq(FileListItem("file 1")), onCopy, onCancel)
    val comp = testRender(<(CopyItemsPopup())(^.wrapped := props)())
    val popup = findComponentProps(comp, popupComp)

    //then
    onCopy.expects(*).never()
    onCancel.expects()
    
    //when
    popup.onClose()
  }
  
  it should "set path when onChange in TextBox" in {
    //given
    val path = "initial path"
    val props = CopyItemsPopupProps(path, Seq(FileListItem("file 1")), _ => (), () => ())
    val renderer = createTestRenderer(<(CopyItemsPopup())(^.wrapped := props)())
    val textBox = findComponentProps(renderer.root, textBoxComp)
    textBox.value shouldBe path
    val newFolderName = "new path"

    //when
    textBox.onChange(newFolderName)

    //then
    findComponentProps(renderer.root, textBoxComp).value shouldBe newFolderName
  }
  
  it should "call onCopy when onEnter in TextBox" in {
    //given
    val onCopy = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps("test", Seq(FileListItem("file 1")), onCopy, onCancel)
    val comp = testRender(<(CopyItemsPopup())(^.wrapped := props)())
    val textBox = findComponentProps(comp, textBoxComp)

    //then
    onCopy.expects("test")
    onCancel.expects().never()

    //when
    textBox.onEnter()
  }
  
  it should "call onCopy when press Copy button" in {
    //given
    val onCopy = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps("test", Seq(FileListItem("file 1")), onCopy, onCancel)
    val comp = testRender(<(CopyItemsPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions.head

    //then
    onCopy.expects("test")
    onCancel.expects().never()
    
    //when
    onPress()
  }
  
  it should "not call onCopy if path is empty" in {
    //given
    val onCopy = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps("", Seq(FileListItem("file 1")), onCopy, onCancel)
    val comp = testRender(<(CopyItemsPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions.head

    //then
    onCopy.expects(*).never()
    onCancel.expects().never()
    
    //when
    onPress()
  }
  
  it should "call onCancel when press Cancel button" in {
    //given
    val onCopy = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps("", Seq(FileListItem("file 1")), onCopy, onCancel)
    val comp = testRender(<(CopyItemsPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions(1)

    //then
    onCopy.expects(*).never()
    onCancel.expects()
    
    //when
    onPress()
  }
  
  it should "render component" in {
    //given
    val props = CopyItemsPopupProps("test folder", Seq(FileListItem("file 1")), _ => (), () => ())

    //when
    val result = testRender(<(CopyItemsPopup())(^.wrapped := props)())

    //then
    assertCopyItemsPopup(result, props, List("[ Copy ]", "[ Cancel ]"))
  }

  private def assertCopyItemsPopup(result: TestInstance,
                                   props: CopyItemsPopupProps,
                                   actions: List[String]): Unit = {
    val (width, height) = (75, 8)
    val style = Theme.current.popup.regular
    val count = props.items.size
    val itemsText =
      if (count > 1) s"$count items"
      else s"${props.items.headOption.map(i => s""""${i.name}"""").getOrElse("")}"
    
    def assertComponents(border: TestInstance,
                         label: TestInstance,
                         input: TestInstance,
                         sep: TestInstance,
                         actionsBox: TestInstance): Assertion = {

      assertTestComponent(border, doubleBorderComp) {
        case DoubleBorderProps(resSize, resStyle, pos, title) =>
          resSize shouldBe (width - 6) -> (height - 2)
          resStyle shouldBe style
          pos shouldBe 3 -> 1
          title shouldBe Some("Copy")
      }
      
      assertTestComponent(label, textLineComp) {
        case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextLine.Left
          pos shouldBe 4 -> 2
          resWidth shouldBe (width - 8)
          text shouldBe s"Copy $itemsText to:"
          resStyle shouldBe style
          focused shouldBe false
          padding shouldBe 1
      }
      assertTestComponent(input, textBoxComp) {
        case TextBoxProps(pos, resWidth, resValue, _, _) =>
          pos shouldBe 5 -> 3
          resWidth shouldBe (width - 10)
          resValue shouldBe props.path
      }
      assertTestComponent(sep, horizontalLineComp) {
        case HorizontalLineProps(pos, resLength, lineCh, resStyle, startCh, endCh) =>
          pos shouldBe 3 -> 4
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
          case List(border, label, input, sep, actionsBox) =>
            assertComponents(border, label, input, sep, actionsBox)
        }
      )
    })
  }
}
