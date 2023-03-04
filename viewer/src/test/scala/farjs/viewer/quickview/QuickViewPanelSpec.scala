package farjs.viewer.quickview

import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack._
import farjs.ui.border._
import farjs.ui.theme.Theme
import farjs.ui.{TextAlign, TextLineProps}
import farjs.viewer.quickview.QuickViewPanel._
import org.scalatest.Assertion
import scommons.nodejs.path
import scommons.react._
import scommons.react.blessed._
import scommons.react.redux.Dispatch
import scommons.react.test._

import scala.scalajs.js

class QuickViewPanelSpec extends TestSpec with TestRendererUtils {

  QuickViewPanel.doubleBorderComp = mockUiComponent("DoubleBorder")
  QuickViewPanel.horizontalLineComp = mockUiComponent("HorizontalLine")
  QuickViewPanel.textLineComp = mockUiComponent("TextLine")
  QuickViewPanel.quickViewDirComp = mockUiComponent("QuickViewDir")
  QuickViewPanel.quickViewFileComp = mockUiComponent("QuickViewFile")

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
    val leftPanelStack = PanelStackProps(
      isRight = false,
      panelInput = js.Dynamic.literal().asInstanceOf[BlessedElement],
      stack = new PanelStack(isActive = false, stackState, { f =>
        stackState = f(stackState).asInstanceOf[List[PanelStackItem[QuickViewParams]]]
      }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit),
      width = width,
      height = height
    )
    val rightPanelStack = PanelStackProps(
      isRight = true,
      panelInput = js.Dynamic.literal().asInstanceOf[BlessedElement],
      stack = new PanelStack(isActive = true, List(
        PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(state))
      ), null),
      width = width,
      height = height
    )

    //when
    val result = testRender(
      withContext(isRight = false, leftPanelStack, rightPanelStack, <(QuickViewPanel())()())
    )

    //then
    assertFileListQuickView(result, dispatch, actions, state, leftPanelStack, FileListItem.currDir)
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
    val leftPanelStack = PanelStackProps(
      isRight = false,
      panelInput = js.Dynamic.literal().asInstanceOf[BlessedElement],
      stack = new PanelStack(isActive = true, List(
        PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(state))
      ), null),
      width = width,
      height = height
    )
    val rightPanelStack = PanelStackProps(
      isRight = true,
      panelInput = js.Dynamic.literal().asInstanceOf[BlessedElement],
      stack = new PanelStack(isActive = false, stackState, { f =>
        stackState = f(stackState).asInstanceOf[List[PanelStackItem[QuickViewParams]]]
      }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit),
      width = width,
      height = height
    )

    //when
    val result = testRender(
      withContext(isRight = true, leftPanelStack, rightPanelStack, <(QuickViewPanel())()())
    )

    //then
    assertFileListQuickView(result, dispatch, actions, state, rightPanelStack, file)
  }

  private def withContext(isRight: Boolean,
                          leftPanelStack: PanelStackProps,
                          rightPanelStack: PanelStackProps,
                          element: ReactElement): ReactElement = {

    WithPanelStacksSpec.withContext(
      <(PanelStack.Context.Provider)(^.contextValue := {
        if (isRight) rightPanelStack else leftPanelStack
      })(
        element
      ),
      leftStack = leftPanelStack.stack,
      rightStack = rightPanelStack.stack,
      leftInput = leftPanelStack.panelInput,
      rightInput = rightPanelStack.panelInput
    )
  }
  
  private def assertFileListQuickView(result: TestInstance,
                                      dispatch: Dispatch,
                                      actions: FileListActions,
                                      state: FileListState,
                                      panelStack: PanelStackProps,
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
            resStack shouldBe panelStack.stack
            resWidth shouldBe width
            resCurrItem shouldBe currItem
        }
      }
      else {
        assertNativeComponent(content,
          <.box(
            ^.rbLeft := 1,
            ^.rbTop := 1,
            ^.rbWidth := width - 2,
            ^.rbHeight := height - 5,
            ^.rbStyle := theme.regularItem
          )(
            <(quickViewFileComp())(^assertWrapped(inside(_) {
              case QuickViewFileProps(resDispatch, resPanelStack, filePath, size) =>
                resDispatch shouldBe dispatch
                resPanelStack shouldBe panelStack
                filePath shouldBe path.join(state.currDir.path, currItem.name)
                size shouldBe currItem.size
            }))()
          )
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
