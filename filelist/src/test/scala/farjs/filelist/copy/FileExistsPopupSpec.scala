package farjs.filelist.copy

import farjs.filelist.api.FileListItem
import farjs.filelist.copy.FileExistsPopup._
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.PopupProps
import farjs.ui.theme.Theme
import org.scalatest.{Assertion, Succeeded}
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

class FileExistsPopupSpec extends TestSpec with TestRendererUtils {

  FileExistsPopup.popupComp = () => "Popup".asInstanceOf[ReactClass]
  FileExistsPopup.doubleBorderComp = () => "DoubleBorder".asInstanceOf[ReactClass]
  FileExistsPopup.textLineComp = () => "TextLine".asInstanceOf[ReactClass]
  FileExistsPopup.horizontalLineComp = () => "HorizontalLine".asInstanceOf[ReactClass]

  it should "call onAction(Overwrite) when press Overwrite button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(<(FileExistsPopup())(^.wrapped := props)())
    val button = findComponents(comp, "button").head

    //then
    onAction.expects(FileExistsAction.Overwrite)
    onCancel.expects().never()
    
    //when
    button.props.onPress()
  }
  
  it should "call onAction(All) when press All button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(<(FileExistsPopup())(^.wrapped := props)())
    val button = findComponents(comp, "button")(1)

    //then
    onAction.expects(FileExistsAction.All)
    onCancel.expects().never()
    
    //when
    button.props.onPress()
  }
  
  it should "call onAction(Skip) when press Skip button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(<(FileExistsPopup())(^.wrapped := props)())
    val button = findComponents(comp, "button")(2)

    //then
    onAction.expects(FileExistsAction.Skip)
    onCancel.expects().never()
    
    //when
    button.props.onPress()
  }
  
  it should "call onAction(SkipAll) when press Skip all button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(<(FileExistsPopup())(^.wrapped := props)())
    val button = findComponents(comp, "button")(3)

    //then
    onAction.expects(FileExistsAction.SkipAll)
    onCancel.expects().never()
    
    //when
    button.props.onPress()
  }
  
  it should "call onAction(Append) when press Append button" in {
    //given
    val onAction = mockFunction[FileExistsAction, Unit]
    val onCancel = mockFunction[Unit]
    val props = FileExistsPopupProps(FileListItem("file 1"), FileListItem("file 1"), onAction, onCancel)
    val comp = testRender(<(FileExistsPopup())(^.wrapped := props)())
    val button = findComponents(comp, "button")(4)

    //then
    onAction.expects(FileExistsAction.Append)
    onCancel.expects().never()
    
    //when
    button.props.onPress()
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
    val cancelButton = findComponents(comp, "button").last

    //then
    onAction.expects(*).never()
    onCancel.expects()
    
    //when
    cancelButton.props.onPress()
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
      " Overwrite " -> 0,
      " All " -> 11,
      " Skip " -> 16,
      " Skip all " -> 22,
      " Append " -> 32,
      " Cancel " -> 40
    ))
  }

  private def assertFileExistsPopup(result: TestInstance,
                                    props: FileExistsPopupProps,
                                    actions: List[(String, Int)]): Unit = {
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

      val buttonsWidth = actions.map(_._1.length).sum
      assertNativeComponent(actionsBox,
        <.box(
          ^.rbWidth := buttonsWidth,
          ^.rbHeight := 1,
          ^.rbLeft := 5,
          ^.rbTop := height - 3,
          ^.rbStyle := theme
        )(), { buttons: List[TestInstance] =>
          buttons.size shouldBe actions.size
          buttons.zip(actions).foreach { case (btn, (action, pos)) =>
            assertNativeComponent(btn,
              <.button(
                ^.key := s"$pos",
                ^.rbMouse := true,
                ^.rbHeight := 1,
                ^.rbLeft := pos,
                ^.rbStyle := theme,
                ^.content := action
              )()
            )
          }
          Succeeded
        }
      )
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
