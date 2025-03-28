package farjs.copymove

import farjs.copymove.FileExistsPopup._
import farjs.filelist.api.FileListItem
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalProps
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import org.scalatest.Assertion
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class FileExistsPopupSpec extends TestSpec with TestRendererUtils {

  FileExistsPopup.modalComp = "Modal".asInstanceOf[ReactClass]
  FileExistsPopup.textLineComp = "TextLine".asInstanceOf[ReactClass]
  FileExistsPopup.horizontalLineComp = "HorizontalLine".asInstanceOf[ReactClass]
  FileExistsPopup.buttonsPanelComp = "ButtonsPanel".asInstanceOf[ReactClass]

  it should "call onCancel when onCancel in modal" in {
    //given
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), _ => (), onCancel)
    val comp = testRender(withThemeContext(<(FileExistsPopup())(^.plain := props)()))
    val modal = inside(findComponents(comp, modalComp)) {
      case List(modal) => modal.props.asInstanceOf[ModalProps]
    }

    //then
    onCancel.expects()
    
    //when
    modal.onCancel()
  }
  
  it should "call onAction(Overwrite) when press Overwrite button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(withThemeContext(<(FileExistsPopup())(^.plain := props)()))
    val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    val action = buttonsProps.actions.head

    //then
    onAction.expects(FileExistsAction.Overwrite)
    onCancel.expects().never()
    
    //when
    action.onAction()
  }
  
  it should "call onAction(All) when press All button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(withThemeContext(<(FileExistsPopup())(^.plain := props)()))
    val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    val action = buttonsProps.actions(1)

    //then
    onAction.expects(FileExistsAction.All)
    onCancel.expects().never()
    
    //when
    action.onAction()
  }
  
  it should "call onAction(Skip) when press Skip button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(withThemeContext(<(FileExistsPopup())(^.plain := props)()))
    val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    val action = buttonsProps.actions(2)

    //then
    onAction.expects(FileExistsAction.Skip)
    onCancel.expects().never()
    
    //when
    action.onAction()
  }
  
  it should "call onAction(SkipAll) when press Skip all button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(withThemeContext(<(FileExistsPopup())(^.plain := props)()))
    val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    val action = buttonsProps.actions(3)

    //then
    onAction.expects(FileExistsAction.SkipAll)
    onCancel.expects().never()
    
    //when
    action.onAction()
  }
  
  it should "call onAction(Append) when press Append button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(withThemeContext(<(FileExistsPopup())(^.plain := props)()))
    val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    val action = buttonsProps.actions(4)

    //then
    onAction.expects(FileExistsAction.Append)
    onCancel.expects().never()
    
    //when
    action.onAction()
  }
  
  it should "call onCancel when press Cancel button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(withThemeContext(<(FileExistsPopup())(^.plain := props)()))
    val buttonsProps = inside(findComponents(comp, buttonsPanelComp)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    val action = buttonsProps.actions.last

    //then
    onAction.expects(*).never()
    onCancel.expects()
    
    //when
    action.onAction()
  }
  
  it should "render component" in {
    //given
    val props = FileExistsPopupProps(
      newItem = FileListItem.copy(FileListItem("file 1"))(size = 1),
      existing = FileListItem.copy(FileListItem("file 1"))(size = 2),
      onAction = _ => (),
      onCancel = () => ()
    )

    //when
    val result = testRender(withThemeContext(<(FileExistsPopup())(^.plain := props)()))

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
    val theme = DefaultTheme.popup.error
    
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
      assertNativeComponent(item, <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
        case TextLineProps(align, left, top, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextAlign.center
          left shouldBe 2
          top shouldBe 2
          resWidth shouldBe (width - 10)
          text shouldBe props.newItem.name
          resStyle shouldBe theme
          focused shouldBe js.undefined
          padding shouldBe 0
      }))())
      assertNativeComponent(sep1, <(horizontalLineComp)(^.assertPlain[HorizontalLineProps](inside(_) {
        case HorizontalLineProps(resLeft, resTop, resLength, lineCh, resStyle, startCh, endCh) =>
          resLeft shouldBe 0
          resTop shouldBe 3
          resLength shouldBe (width - 6)
          lineCh shouldBe SingleChars.horizontal
          resStyle shouldBe theme
          startCh shouldBe DoubleChars.leftSingle
          endCh shouldBe DoubleChars.rightSingle
      }))())
      
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
      assertNativeComponent(newItem, <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
        case TextLineProps(align, left, top, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextAlign.right
          left shouldBe 2
          top shouldBe 4
          resWidth shouldBe (width - 10)
          text should startWith (props.newItem.size.toString)
          resStyle shouldBe theme
          focused shouldBe js.undefined
          padding shouldBe 0
      }))())
      assertNativeComponent(existing, <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
        case TextLineProps(align, left, top, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextAlign.right
          left shouldBe 2
          top shouldBe 5
          resWidth shouldBe (width - 10)
          text should startWith (props.existing.size.toString)
          resStyle shouldBe theme
          focused shouldBe js.undefined
          padding shouldBe 0
      }))())

      assertNativeComponent(sep2, <(horizontalLineComp)(^.assertPlain[HorizontalLineProps](inside(_) {
        case HorizontalLineProps(resLeft, resTop, resLength, lineCh, resStyle, startCh, endCh) =>
          resLeft shouldBe 0
          resTop shouldBe 6
          resLength shouldBe (width - 6)
          lineCh shouldBe SingleChars.horizontal
          resStyle shouldBe theme
          startCh shouldBe DoubleChars.leftSingle
          endCh shouldBe DoubleChars.rightSingle
      }))())
      assertNativeComponent(actionsBox,
        <(buttonsPanelComp)(^.assertPlain[ButtonsPanelProps](inside(_) {
          case ButtonsPanelProps(top, resActions, resStyle, padding, margin) =>
            top shouldBe 7
            resActions.map(_.label).toList shouldBe actions
            resStyle shouldBe theme
            padding shouldBe 1
            margin shouldBe js.undefined
        }))()
      )
    }
    
    assertNativeComponent(result, <(modalComp)(^.assertPlain[ModalProps](inside(_) {
      case ModalProps(title, resWidth, resHeight, resStyle, _) =>
        title shouldBe "Warning"
        resWidth shouldBe width
        resHeight shouldBe height
        resStyle shouldBe theme
    }))(), inside(_) {
      case List(label1, item, sep1, label2, newItem, existing, sep2, actionsBox) =>
        assertComponents(label1, item, sep1, label2, newItem, existing, sep2, actionsBox)
    })
  }
}
