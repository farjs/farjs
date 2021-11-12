package farjs.filelist.copy

import farjs.filelist.api.FileListItem
import farjs.filelist.copy.CopyItemsPopup._
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup.ModalProps
import farjs.ui.theme.Theme
import org.scalatest.Assertion
import scommons.react.test._

class CopyItemsPopupSpec extends TestSpec with TestRendererUtils {

  CopyItemsPopup.modalComp = mockUiComponent("Modal")
  CopyItemsPopup.textLineComp = mockUiComponent("TextLine")
  CopyItemsPopup.textBoxComp = mockUiComponent("TextBox")
  CopyItemsPopup.horizontalLineComp = mockUiComponent("HorizontalLine")
  CopyItemsPopup.buttonsPanelComp = mockUiComponent("ButtonsPanel")

  it should "set path when onChange in TextBox" in {
    //given
    val path = "initial path"
    val props = CopyItemsPopupProps(move = false, path, Seq(FileListItem("file 1")), _ => (), () => ())
    val renderer = createTestRenderer(<(CopyItemsPopup())(^.wrapped := props)())
    val textBox = findComponentProps(renderer.root, textBoxComp)
    textBox.value shouldBe path
    val newFolderName = "new path"

    //when
    textBox.onChange(newFolderName)

    //then
    findComponentProps(renderer.root, textBoxComp).value shouldBe newFolderName
  }
  
  it should "call onAction when onEnter in TextBox" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps(move = false, "test", Seq(FileListItem("file 1")), onAction, onCancel)
    val comp = testRender(<(CopyItemsPopup())(^.wrapped := props)())
    val textBox = findComponentProps(comp, textBoxComp)

    //then
    onAction.expects("test")
    onCancel.expects().never()

    //when
    textBox.onEnter()
  }
  
  it should "call onAction when press action button" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps(move = false, "test", Seq(FileListItem("file 1")), onAction, onCancel)
    val comp = testRender(<(CopyItemsPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions.head

    //then
    onAction.expects("test")
    onCancel.expects().never()
    
    //when
    onPress()
  }
  
  it should "not call onAction if path is empty" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps(move = false, "", Seq(FileListItem("file 1")), onAction, onCancel)
    val comp = testRender(<(CopyItemsPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions.head

    //then
    onAction.expects(*).never()
    onCancel.expects().never()
    
    //when
    onPress()
  }
  
  it should "call onCancel when press Cancel button" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps(move = false, "", Seq(FileListItem("file 1")), onAction, onCancel)
    val comp = testRender(<(CopyItemsPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions(1)

    //then
    onAction.expects(*).never()
    onCancel.expects()
    
    //when
    onPress()
  }
  
  it should "render component when copy" in {
    //given
    val props = CopyItemsPopupProps(move = false, "test folder", Seq(FileListItem("file 1")), _ => (), () => ())

    //when
    val result = testRender(<(CopyItemsPopup())(^.wrapped := props)())

    //then
    assertCopyItemsPopup(result, props, List("[ Copy ]", "[ Cancel ]"))
  }

  it should "render component when move" in {
    //given
    val props = CopyItemsPopupProps(move = true, "test folder", Seq(FileListItem("file 1")), _ => (), () => ())

    //when
    val result = testRender(<(CopyItemsPopup())(^.wrapped := props)())

    //then
    assertCopyItemsPopup(result, props, List("[ Rename ]", "[ Cancel ]"))
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

    val title = if (props.move) "Rename/Move" else "Copy"
    val text = if (props.move) "Rename or move" else "Copy"
    
    def assertComponents(label: TestInstance,
                         input: TestInstance,
                         sep: TestInstance,
                         actionsBox: TestInstance): Assertion = {

      assertTestComponent(label, textLineComp) {
        case TextLineProps(align, pos, resWidth, resText, resStyle, focused, padding) =>
          align shouldBe TextLine.Left
          pos shouldBe 2 -> 1
          resWidth shouldBe (width - (paddingHorizontal + 2) * 2)
          resText shouldBe s"$text $itemsText to:"
          resStyle shouldBe style
          focused shouldBe false
          padding shouldBe 0
      }
      assertTestComponent(input, textBoxComp) {
        case TextBoxProps(pos, resWidth, resValue, _, _) =>
          pos shouldBe 2 -> 2
          resWidth shouldBe (width - (paddingHorizontal + 2) * 2)
          resValue shouldBe props.path
      }

      assertTestComponent(sep, horizontalLineComp) {
        case HorizontalLineProps(pos, resLength, lineCh, resStyle, startCh, endCh) =>
          pos shouldBe 0 -> 3
          resLength shouldBe (width - paddingHorizontal * 2)
          lineCh shouldBe SingleBorder.horizontalCh
          resStyle shouldBe style
          startCh shouldBe Some(DoubleBorder.leftSingleCh)
          endCh shouldBe Some(DoubleBorder.rightSingleCh)
      }
      assertTestComponent(actionsBox, buttonsPanelComp) {
        case ButtonsPanelProps(top, resActions, resStyle, padding, margin) =>
          top shouldBe 4
          resActions.map(_._1) shouldBe actions
          resStyle shouldBe style
          padding shouldBe 0
          margin shouldBe 2
      }
    }
    
    assertTestComponent(result, modalComp)({ case ModalProps(resTitle, size, resStyle, onCancel) =>
      resTitle shouldBe title
      size shouldBe width -> height
      resStyle shouldBe style
      onCancel should be theSameInstanceAs props.onCancel
    }, inside(_) { case List(label, input, sep, actionsBox) =>
      assertComponents(label, input, sep, actionsBox)
    })
  }
}
