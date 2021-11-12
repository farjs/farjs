package farjs.filelist.copy

import farjs.filelist.api.FileListItem
import farjs.filelist.copy.FileExistsPopup._
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalProps
import farjs.ui.theme.Theme
import org.scalatest.Assertion
import scommons.react.blessed._
import scommons.react.test._

class FileExistsPopupSpec extends TestSpec with TestRendererUtils {

  FileExistsPopup.modalComp = mockUiComponent("Modal")
  FileExistsPopup.textLineComp = mockUiComponent("TextLine")
  FileExistsPopup.horizontalLineComp = mockUiComponent("HorizontalLine")
  FileExistsPopup.buttonsPanelComp = mockUiComponent("ButtonsPanel")

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
    
    def assertComponents(label1: TestInstance,
                         item : TestInstance,
                         sep1: TestInstance,
                         label2: TestInstance,
                         newItem: TestInstance,
                         existing: TestInstance,
                         sep2: TestInstance,
                         actionsBox: TestInstance): Assertion = {

      assertNativeComponent(label1,
        <.text(
          ^.rbLeft := "center",
          ^.rbTop := 1,
          ^.rbStyle := theme,
          ^.content := "File already exists"
        )()
      )
      assertTestComponent(item, textLineComp) {
        case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextLine.Center
          pos shouldBe 2 -> 2
          resWidth shouldBe (width - 10)
          text shouldBe props.newItem.name
          resStyle shouldBe theme
          focused shouldBe false
          padding shouldBe 0
      }
      assertTestComponent(sep1, horizontalLineComp) {
        case HorizontalLineProps(pos, resLength, lineCh, resStyle, startCh, endCh) =>
          pos shouldBe 0 -> 3
          resLength shouldBe (width - 6)
          lineCh shouldBe SingleBorder.horizontalCh
          resStyle shouldBe theme
          startCh shouldBe Some(DoubleBorder.leftSingleCh)
          endCh shouldBe Some(DoubleBorder.rightSingleCh)
      }
      
      assertNativeComponent(label2,
        <.text(
          ^.rbLeft := 2,
          ^.rbTop := 4,
          ^.rbStyle := theme,
          ^.content :=
            """New
              |Existing""".stripMargin
        )()
      )
      assertTestComponent(newItem, textLineComp) {
        case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextLine.Right
          pos shouldBe 2 -> 4
          resWidth shouldBe (width - 10)
          text should startWith (props.newItem.size.toString)
          resStyle shouldBe theme
          focused shouldBe false
          padding shouldBe 0
      }
      assertTestComponent(existing, textLineComp) {
        case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextLine.Right
          pos shouldBe 2 -> 5
          resWidth shouldBe (width - 10)
          text should startWith (props.existing.size.toString)
          resStyle shouldBe theme
          focused shouldBe false
          padding shouldBe 0
      }

      assertTestComponent(sep2, horizontalLineComp) {
        case HorizontalLineProps(pos, resLength, lineCh, resStyle, startCh, endCh) =>
          pos shouldBe 0 -> 6
          resLength shouldBe (width - 6)
          lineCh shouldBe SingleBorder.horizontalCh
          resStyle shouldBe theme
          startCh shouldBe Some(DoubleBorder.leftSingleCh)
          endCh shouldBe Some(DoubleBorder.rightSingleCh)
      }
      assertTestComponent(actionsBox, buttonsPanelComp) {
        case ButtonsPanelProps(top, resActions, resStyle, padding, margin) =>
          top shouldBe 7
          resActions.map(_._1) shouldBe actions
          resStyle shouldBe theme
          padding shouldBe 1
          margin shouldBe 0
      }
    }
    
    assertTestComponent(result, modalComp)({ case ModalProps(title, size, resStyle, onCancel) =>
      title shouldBe "Warning"
      size shouldBe width -> height
      resStyle shouldBe theme
      onCancel should be theSameInstanceAs props.onCancel
    }, inside(_) {
      case List(label1, item, sep1, label2, newItem, existing, sep2, actionsBox) =>
        assertComponents(label1, item, sep1, label2, newItem, existing, sep2, actionsBox)
    })
  }
}
