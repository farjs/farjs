package farjs.filelist.copy

import farjs.filelist.api.FileListItem
import farjs.filelist.copy.FileExistsPopup._
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.PopupProps
import farjs.ui.theme.Theme
import org.scalatest.Assertion
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

class FileExistsPopupSpec extends TestSpec with TestRendererUtils {

  FileExistsPopup.popupComp = () => "Popup".asInstanceOf[ReactClass]
  FileExistsPopup.doubleBorderComp = () => "DoubleBorder".asInstanceOf[ReactClass]
  FileExistsPopup.textLineComp = () => "TextLine".asInstanceOf[ReactClass]
  FileExistsPopup.horizontalLineComp = () => "HorizontalLine".asInstanceOf[ReactClass]
  FileExistsPopup.buttonsPanelComp = () => "ButtonsPanel".asInstanceOf[ReactClass]

  it should "call onAction(Overwrite) when press Overwrite button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(<(FileExistsPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions.head

    //then
    onAction.expects(FileExistsAction.Overwrite)
    onCancel.expects().never()
    
    //when
    onPress()
  }
  
  it should "call onAction(All) when press All button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(<(FileExistsPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions(1)

    //then
    onAction.expects(FileExistsAction.All)
    onCancel.expects().never()
    
    //when
    onPress()
  }
  
  it should "call onAction(Skip) when press Skip button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(<(FileExistsPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions(2)

    //then
    onAction.expects(FileExistsAction.Skip)
    onCancel.expects().never()
    
    //when
    onPress()
  }
  
  it should "call onAction(SkipAll) when press Skip all button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(<(FileExistsPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions(3)

    //then
    onAction.expects(FileExistsAction.SkipAll)
    onCancel.expects().never()
    
    //when
    onPress()
  }
  
  it should "call onAction(Append) when press Append button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(<(FileExistsPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions(4)

    //then
    onAction.expects(FileExistsAction.Append)
    onCancel.expects().never()
    
    //when
    onPress()
  }
  
  it should "call onCancel when close popup" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(<(FileExistsPopup())(^.wrapped := props)())
    val popup = findComponentProps(comp, popupComp)

    //then
    onAction.expects(*).never()
    onCancel.expects()
    
    //when
    popup.onClose()
  }
  
  it should "call onCancel when press Cancel button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(<(FileExistsPopup())(^.wrapped := props)())
    val (_, onPress) = findComponentProps(comp, buttonsPanelComp).actions.last

    //then
    onAction.expects(*).never()
    onCancel.expects()
    
    //when
    onPress()
  }
  
  it should "render component" in {
    //given
    val props = FileExistsPopupProps(
      newItem = FileListItem("file 1", size = 1),
      existing = FileListItem("file 1", size = 2),
      onAction = _ => (),
      onCancel = () => ()
    )

    //when
    val result = testRender(<(FileExistsPopup())(^.wrapped := props)())

    //then
    assertFileExistsPopup(result, props, List(
      "Overwrite",
      "All",
      "Skip",
      "Skip all",
      "Append",
      "Cancel"
    ))
  }

  private def assertFileExistsPopup(result: TestInstance,
                                    props: FileExistsPopupProps,
                                    actions: List[String]): Unit = {
    val (width, height) = (58, 11)
    val theme = Theme.current.popup.error
    
    def assertComponents(border: TestInstance,
                         label1: TestInstance,
                         item : TestInstance,
                         sep1: TestInstance,
                         label2: TestInstance,
                         newItem: TestInstance,
                         existing: TestInstance,
                         sep2: TestInstance,
                         actionsBox: TestInstance): Assertion = {

      assertTestComponent(border, doubleBorderComp) {
        case DoubleBorderProps(resSize, resStyle, pos, title) =>
          resSize shouldBe (width - 6) -> (height - 2)
          resStyle shouldBe theme
          pos shouldBe 3 -> 1
          title shouldBe Some("Warning")
      }

      assertNativeComponent(label1,
        <.text(
          ^.rbLeft := "center",
          ^.rbTop := 2,
          ^.rbStyle := theme,
          ^.content := "File already exists"
        )()
      )
      assertTestComponent(item, textLineComp) {
        case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextLine.Center
          pos shouldBe 5 -> 3
          resWidth shouldBe (width - 10)
          text shouldBe props.newItem.name
          resStyle shouldBe theme
          focused shouldBe false
          padding shouldBe 0
      }

      assertTestComponent(sep1, horizontalLineComp) {
        case HorizontalLineProps(pos, resLength, lineCh, resStyle, startCh, endCh) =>
          pos shouldBe 3 -> 4
          resLength shouldBe (width - 6)
          lineCh shouldBe SingleBorder.horizontalCh
          resStyle shouldBe theme
          startCh shouldBe Some(DoubleBorder.leftSingleCh)
          endCh shouldBe Some(DoubleBorder.rightSingleCh)
      }
      assertNativeComponent(label2,
        <.text(
          ^.rbLeft := 5,
          ^.rbTop := 5,
          ^.rbStyle := theme,
          ^.content :=
            """New
              |Existing""".stripMargin
        )()
      )
      assertTestComponent(newItem, textLineComp) {
        case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextLine.Right
          pos shouldBe 5 -> 5
          resWidth shouldBe (width - 10)
          text should startWith (props.newItem.size.toString)
          resStyle shouldBe theme
          focused shouldBe false
          padding shouldBe 0
      }
      assertTestComponent(existing, textLineComp) {
        case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextLine.Right
          pos shouldBe 5 -> 6
          resWidth shouldBe (width - 10)
          text should startWith (props.existing.size.toString)
          resStyle shouldBe theme
          focused shouldBe false
          padding shouldBe 0
      }

      assertTestComponent(sep2, horizontalLineComp) {
        case HorizontalLineProps(pos, resLength, lineCh, resStyle, startCh, endCh) =>
          pos shouldBe 3 -> 7
          resLength shouldBe (width - 6)
          lineCh shouldBe SingleBorder.horizontalCh
          resStyle shouldBe theme
          startCh shouldBe Some(DoubleBorder.leftSingleCh)
          endCh shouldBe Some(DoubleBorder.rightSingleCh)
      }

      assertTestComponent(actionsBox, buttonsPanelComp) {
        case ButtonsPanelProps(top, resActions, resStyle, padding, margin) =>
          top shouldBe (height - 3)
          resActions.map(_._1) shouldBe actions
          resStyle shouldBe theme
          padding shouldBe 1
          margin shouldBe 0
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
          ^.rbStyle := theme
        )(), inside(_) {
          case List(border, label1, item, sep1, label2, newItem, existing, sep2, actionsBox) =>
            assertComponents(border, label1, item, sep1, label2, newItem, existing, sep2, actionsBox)
        }
      )
    })
  }
}
