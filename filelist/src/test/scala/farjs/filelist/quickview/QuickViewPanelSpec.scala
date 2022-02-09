package farjs.filelist.quickview

import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.quickview.QuickViewPanel._
import farjs.filelist.stack.PanelStack.StackItem
import farjs.filelist.stack._
import farjs.ui.{TextLine, TextLineProps}
import farjs.ui.border._
import farjs.ui.theme.Theme
import org.scalatest.Assertion
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class QuickViewPanelSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  QuickViewPanel.doubleBorderComp = mockUiComponent("DoubleBorder")
  QuickViewPanel.horizontalLineComp = mockUiComponent("HorizontalLine")
  QuickViewPanel.textLineComp = mockUiComponent("TextLine")
  QuickViewPanel.quickViewDirComp = mockUiComponent("QuickViewDir")

  private val currComp = "QuickViewPanel".asInstanceOf[ReactClass]
  private val (width, height) = (25, 15)

  it should "render dir view" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val state = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem.up,
      FileListItem("file", size = 2),
      FileListItem("dir", isDir = true, size = 1)
    )))
    val props = QuickViewPanelProps(dispatch, actions, FileListsState(right = state))
    var stackState = List[StackItem](
      (currComp, QuickViewParams().asInstanceOf[js.Any])
    )
    val stack = new PanelStack(stackState.headOption, { f: js.Function1[List[StackItem], List[StackItem]] =>
      stackState = f(stackState)
    })

    //when
    val result = testRender(
      withContext(isRight = false, stack, <(QuickViewPanel())(^.wrapped := props)())
    )

    //then
    assertFileListQuickView(result, props, state, stack, FileListItem.currDir)
  }

  it should "render file view" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val file = FileListItem("file", size = 2)
    val state = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem.up,
      file,
      FileListItem("dir", isDir = true, size = 1)
    )), index = 1, isActive = true)
    val props = QuickViewPanelProps(dispatch, actions, FileListsState(left = state))
    var stackState = List[StackItem](
      (currComp, QuickViewParams().asInstanceOf[js.Any])
    )
    val stack = new PanelStack(stackState.headOption, { f: js.Function1[List[StackItem], List[StackItem]] =>
      stackState = f(stackState)
    })

    //when
    val result = testRender(
      withContext(isRight = true, stack, <(QuickViewPanel())(^.wrapped := props)())
    )

    //then
    assertFileListQuickView(result, props, state, stack, file)
  }

  private def withContext(isRight: Boolean, stack: PanelStack, element: ReactElement): ReactElement = {
    PanelStackSpec.withContext(
      element = element,
      isRight = isRight,
      stack = stack,
      width = width,
      height = height
    )
  }
  
  private def assertFileListQuickView(result: TestInstance,
                                      props: QuickViewPanelProps,
                                      state: FileListState,
                                      stack: PanelStack,
                                      currItem: FileListItem): Assertion = {

    val theme = Theme.current.fileList

    def assertComponents(border: TestInstance,
                         line: TestInstance,
                         header: TestInstance,
                         content: TestInstance,
                         item: TestInstance): Assertion = {

      assertTestComponent(border, doubleBorderComp) {
        case DoubleBorderProps(resSize, style, pos, title) =>
          resSize shouldBe width -> height
          style shouldBe theme.regularItem
          pos shouldBe 0 -> 0
          title shouldBe None
      }
      assertTestComponent(line, horizontalLineComp) {
        case HorizontalLineProps(pos, len, lineCh, style, startCh, endCh) =>
          pos shouldBe 0 -> (height - 4)
          len shouldBe width
          lineCh shouldBe SingleBorder.horizontalCh
          style shouldBe theme.regularItem
          startCh shouldBe Some(DoubleBorder.leftSingleCh)
          endCh shouldBe Some(DoubleBorder.rightSingleCh)
      }
      assertTestComponent(header, textLineComp) {
        case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
          align shouldBe TextLine.Center
          pos shouldBe 1 -> 0
          resWidth shouldBe (width - 2)
          text shouldBe "Quick view"
          style shouldBe theme.regularItem
          focused shouldBe !state.isActive
          padding shouldBe 1
      }

      if (currItem.isDir) {
        assertTestComponent(content, quickViewDirComp) {
          case QuickViewDirProps(dispatch, actions, resState, resStack, resWidth, resCurrItem) =>
            dispatch shouldBe props.dispatch
            actions shouldBe props.actions
            resState shouldBe state
            resStack shouldBe stack
            resWidth shouldBe width
            resCurrItem shouldBe currItem
        }
      }
      else {
        assertNativeComponent(content,
          <.text(
            ^.rbLeft := 2,
            ^.rbTop := 4,
            ^.rbStyle := theme.regularItem,
            ^.content := "TODO: Display file's content here"
          )()
        )
      }
      
      assertNativeComponent(item,
        <.text(
          ^.rbWidth := width - 2,
          ^.rbHeight := 2,
          ^.rbLeft := 1,
          ^.rbTop := height - 3,
          ^.rbStyle := theme.regularItem,
          ^.content := currItem.name
        )()
      )
    }

    assertNativeComponent(result, <.box(^.rbStyle := theme.regularItem)(), inside(_) {
      case List(border, line, header, content, item)  =>
        assertComponents(border, line, header, content, item)
    })
  }
}
