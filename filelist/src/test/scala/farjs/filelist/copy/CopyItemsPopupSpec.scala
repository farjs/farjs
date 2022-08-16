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

import scala.scalajs.js

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
    val textBox = findComponentProps(renderer.root, textBoxComp, plain = true)
    textBox.value shouldBe path
    val newFolderName = "new path"

    //when
    textBox.onChange(newFolderName)

    //then
    findComponentProps(renderer.root, textBoxComp, plain = true).value shouldBe newFolderName
  }
  
  it should "call onAction when onEnter in TextBox" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps(move = false, "test", Seq(FileListItem("file 1")), onAction, onCancel)
    val comp = testRender(<(CopyItemsPopup())(^.wrapped := props)())
    val textBox = findComponentProps(comp, textBoxComp, plain = true)

    //then
    onAction.expects("test")
    onCancel.expects().never()

    //when
    textBox.onEnter.get.apply()
  }
  
  it should "call onAction when press action button" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps(move = false, "test", Seq(FileListItem("file 1")), onAction, onCancel)
    val comp = testRender(<(CopyItemsPopup())(^.wrapped := props)())
    val action = findComponentProps(comp, buttonsPanelComp, plain = true).actions.head

    //then
    onAction.expects("test")
    onCancel.expects().never()
    
    //when
    action.onAction()
  }
  
  it should "not call onAction if path is empty" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps(move = false, "", Seq(FileListItem("file 1")), onAction, onCancel)
    val comp = testRender(<(CopyItemsPopup())(^.wrapped := props)())
    val action = findComponentProps(comp, buttonsPanelComp, plain = true).actions.head

    //then
    onAction.expects(*).never()
    onCancel.expects().never()
    
    //when
    action.onAction()
  }
  
  it should "call onCancel when press Cancel button" in {
    //given
    val onAction = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = CopyItemsPopupProps(move = false, "", Seq(FileListItem("file 1")), onAction, onCancel)
    val comp = testRender(<(CopyItemsPopup())(^.wrapped := props)())
    val action = findComponentProps(comp, buttonsPanelComp, plain = true).actions(1)

    //then
    onAction.expects(*).never()
    onCancel.expects()
    
    //when
    action.onAction()
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

      assertTestComponent(label, textLineComp, plain = true) {
        case TextLineProps(align, left, top, resWidth, resText, resStyle, focused, padding) =>
          align shouldBe TextAlign.left
          left shouldBe 2
          top shouldBe 1
          resWidth shouldBe (width - (paddingHorizontal + 2) * 2)
          resText shouldBe s"$text $itemsText to:"
          resStyle shouldBe style
          focused shouldBe js.undefined
          padding shouldBe 0
      }
      assertTestComponent(input, textBoxComp, plain = true) {
        case TextBoxProps(left, top, resWidth, resValue, _, _) =>
          left shouldBe 2
          top shouldBe 2
          resWidth shouldBe (width - (paddingHorizontal + 2) * 2)
          resValue shouldBe props.path
      }

      assertTestComponent(sep, horizontalLineComp, plain = true) {
        case HorizontalLineProps(resLeft, resTop, resLength, lineCh, resStyle, startCh, endCh) =>
          resLeft shouldBe 0
          resTop shouldBe 3
          resLength shouldBe (width - paddingHorizontal * 2)
          lineCh shouldBe SingleBorder.horizontalCh
          resStyle shouldBe style
          startCh shouldBe DoubleChars.leftSingle
          endCh shouldBe DoubleChars.rightSingle
      }
      assertTestComponent(actionsBox, buttonsPanelComp, plain = true) {
        case ButtonsPanelProps(top, resActions, resStyle, padding, margin) =>
          top shouldBe 4
          resActions.map(_.label).toList shouldBe actions
          resStyle shouldBe style
          padding shouldBe js.undefined
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
