package farjs.filelist.quickview

import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.quickview.QuickViewPanel._
import farjs.filelist.stack._
import farjs.ui.border._
import farjs.ui.theme.Theme
import farjs.ui.{TextAlign, TextLineProps}
import org.scalatest.Assertion
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.blessed._
import scommons.react.redux.Dispatch
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
    var stackState = List[PanelStackItem[QuickViewParams]](
      PanelStackItem(currComp, None, None, Some(QuickViewParams()))
    )
    val leftStack = new PanelStack(isActive = false, stackState, { f =>
      stackState = f(stackState).asInstanceOf[List[PanelStackItem[QuickViewParams]]]
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)
    val rightStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(state))
    ), null)

    //when
    val result = testRender(
      withContext(isRight = false, leftStack, rightStack, <(QuickViewPanel())()())
    )

    //then
    assertFileListQuickView(result, dispatch, actions, state, leftStack, FileListItem.currDir)
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
    var stackState = List[PanelStackItem[QuickViewParams]](
      PanelStackItem(currComp, None, None, Some(QuickViewParams()))
    )
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(state))
    ), null)
    val rightStack = new PanelStack(isActive = false, stackState, { f =>
      stackState = f(stackState).asInstanceOf[List[PanelStackItem[QuickViewParams]]]
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)

    //when
    val result = testRender(
      withContext(isRight = true, leftStack, rightStack, <(QuickViewPanel())()())
    )

    //then
    assertFileListQuickView(result, dispatch, actions, state, rightStack, file)
  }

  private def withContext(isRight: Boolean,
                          leftStack: PanelStack,
                          rightStack: PanelStack,
                          element: ReactElement): ReactElement = {

    WithPanelStacksSpec.withContext(
      PanelStackSpec.withContext(
        element = element,
        isRight = isRight,
        stack = if (isRight) rightStack else leftStack,
        width = width,
        height = height
      ),
      leftStack,
      rightStack
    )
  }
  
  private def assertFileListQuickView(result: TestInstance,
                                      dispatch: Dispatch,
                                      actions: FileListActions,
                                      state: FileListState,
                                      stack: PanelStack,
                                      currItem: FileListItem): Assertion = {

    val theme = Theme.current.fileList

    def assertComponents(border: TestInstance,
                         line: TestInstance,
                         header: TestInstance,
                         content: TestInstance,
                         item: TestInstance): Assertion = {

      assertTestComponent(border, doubleBorderComp, plain = true) {
        case DoubleBorderProps(resWidth, resHeight, style, resLeft, resTop, title, footer) =>
          resWidth shouldBe width
          resHeight shouldBe height
          style shouldBe theme.regularItem
          resLeft shouldBe js.undefined
          resTop shouldBe js.undefined
          title shouldBe js.undefined
          footer shouldBe js.undefined
      }
      assertTestComponent(line, horizontalLineComp, plain = true) {
        case HorizontalLineProps(resLeft, resTop, len, lineCh, style, startCh, endCh) =>
          resLeft shouldBe 0
          resTop shouldBe (height - 4)
          len shouldBe width
          lineCh shouldBe SingleChars.horizontal
          style shouldBe theme.regularItem
          startCh shouldBe DoubleChars.leftSingle
          endCh shouldBe DoubleChars.rightSingle
      }
      assertTestComponent(header, textLineComp, plain = true) {
        case TextLineProps(align, left, top, resWidth, text, style, focused, padding) =>
          align shouldBe TextAlign.center
          left shouldBe 1
          top shouldBe 0
          resWidth shouldBe (width - 2)
          text shouldBe "Quick view"
          style shouldBe theme.regularItem
          focused shouldBe !state.isActive
          padding shouldBe js.undefined
      }

      if (currItem.isDir) {
        assertTestComponent(content, quickViewDirComp) {
          case QuickViewDirProps(resDispatch, resActions, resState, resStack, resWidth, resCurrItem) =>
            resDispatch shouldBe dispatch
            resActions shouldBe actions
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
