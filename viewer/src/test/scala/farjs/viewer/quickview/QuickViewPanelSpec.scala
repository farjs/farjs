package farjs.viewer.quickview

import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack._
import farjs.filelist.theme.FileListTheme
import farjs.filelist.theme.FileListThemeSpec.withThemeContext
import farjs.ui.border._
import farjs.ui.{Dispatch, TextAlign, TextLineProps}
import farjs.viewer.quickview.QuickViewPanel._
import org.scalatest.Assertion
import scommons.nodejs.path
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class QuickViewPanelSpec extends TestSpec with TestRendererUtils {

  QuickViewPanel.doubleBorderComp = "DoubleBorder".asInstanceOf[ReactClass]
  QuickViewPanel.horizontalLineComp = "HorizontalLine".asInstanceOf[ReactClass]
  QuickViewPanel.textLineComp = "TextLine".asInstanceOf[ReactClass]
  QuickViewPanel.quickViewDirComp = "QuickViewDir".asInstanceOf[ReactClass]
  QuickViewPanel.quickViewFileComp = "QuickViewFile".asInstanceOf[ReactClass]

  private val currComp = "QuickViewPanel".asInstanceOf[ReactClass]
  private val (width, height) = (25, 15)

  it should "render dir view" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem.up,
      FileListItem.copy(FileListItem("file"))(size = 2),
      FileListItem.copy(FileListItem("dir", isDir = true))(size = 1)
    )))
    var stackState = js.Array[PanelStackItem[_]](
      PanelStackItem(currComp, js.undefined, js.undefined, QuickViewParams())
    )
    val leftPanelStack = WithStackProps(
      isRight = false,
      panelInput = js.Dynamic.literal().asInstanceOf[BlessedElement],
      stack = new PanelStack(isActive = false, stackState, { f =>
        stackState = f(stackState)
      }: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] => Unit),
      width = width,
      height = height
    )
    val rightPanelStack = WithStackProps(
      isRight = true,
      panelInput = js.Dynamic.literal().asInstanceOf[BlessedElement],
      stack = new PanelStack(isActive = true, js.Array(
        PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, actions, state)
      ), null),
      width = width,
      height = height
    )

    //when
    val result = testRender(
      withContext(isRight = false, leftPanelStack, rightPanelStack, withThemeContext(<(QuickViewPanel())()()))
    )

    //then
    assertFileListQuickView(result, dispatch, actions, state, leftPanelStack, FileListItem.currDir)
  }

  it should "render file view" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val file = FileListItem.copy(FileListItem("file"))(size = 2)
    val state = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem.up,
      file,
      FileListItem.copy(FileListItem("dir", isDir = true))(size = 1)
    )), index = 1)
    var stackState = js.Array[PanelStackItem[_]](
      PanelStackItem(currComp, js.undefined, js.undefined, QuickViewParams())
    )
    val leftPanelStack = WithStackProps(
      isRight = false,
      panelInput = js.Dynamic.literal().asInstanceOf[BlessedElement],
      stack = new PanelStack(isActive = true, js.Array(
        PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, actions, state)
      ), null),
      width = width,
      height = height
    )
    val rightPanelStack = WithStackProps(
      isRight = true,
      panelInput = js.Dynamic.literal().asInstanceOf[BlessedElement],
      stack = new PanelStack(isActive = false, stackState, { f =>
        stackState = f(stackState)
      }: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] => Unit),
      width = width,
      height = height
    )

    //when
    val result = testRender(
      withContext(isRight = true, leftPanelStack, rightPanelStack, withThemeContext(<(QuickViewPanel())()()))
    )

    //then
    assertFileListQuickView(result, dispatch, actions, state, rightPanelStack, file)
  }

  private def withContext(isRight: Boolean,
                          leftPanelStack: WithStackProps,
                          rightPanelStack: WithStackProps,
                          element: ReactElement): ReactElement = {

    WithStacksSpec.withContext(
      <(WithStack.Context.Provider)(^.contextValue := {
        if (isRight) rightPanelStack else leftPanelStack
      })(
        element
      ),
      left = WithStacksData(leftPanelStack.stack, leftPanelStack.panelInput),
      right = WithStacksData(rightPanelStack.stack, rightPanelStack.panelInput)
    )
  }
  
  private def assertFileListQuickView(result: TestInstance,
                                      dispatch: Dispatch,
                                      actions: FileListActions,
                                      state: FileListState,
                                      panelStack: WithStackProps,
                                      currItem: FileListItem): Assertion = {

    val theme = FileListTheme.defaultTheme.fileList

    def assertComponents(border: TestInstance,
                         line: TestInstance,
                         header: TestInstance,
                         content: TestInstance,
                         item: TestInstance): Assertion = {

      assertNativeComponent(border, <(doubleBorderComp)(^.assertPlain[DoubleBorderProps](inside(_) {
        case DoubleBorderProps(resWidth, resHeight, style, resLeft, resTop, title, footer) =>
          resWidth shouldBe width
          resHeight shouldBe height
          style shouldBe theme.regularItem
          resLeft shouldBe js.undefined
          resTop shouldBe js.undefined
          title shouldBe js.undefined
          footer shouldBe js.undefined
      }))())
      assertNativeComponent(line, <(horizontalLineComp)(^.assertPlain[HorizontalLineProps](inside(_) {
        case HorizontalLineProps(resLeft, resTop, len, lineCh, style, startCh, endCh) =>
          resLeft shouldBe 0
          resTop shouldBe (height - 4)
          len shouldBe width
          lineCh shouldBe SingleChars.horizontal
          style shouldBe theme.regularItem
          startCh shouldBe DoubleChars.leftSingle
          endCh shouldBe DoubleChars.rightSingle
      }))())
      assertNativeComponent(header, <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
        case TextLineProps(align, left, top, resWidth, text, style, focused, padding) =>
          align shouldBe TextAlign.center
          left shouldBe 1
          top shouldBe 0
          resWidth shouldBe (width - 2)
          text shouldBe "Quick view"
          style shouldBe theme.regularItem
          focused shouldBe panelStack.stack.isActive
          padding shouldBe js.undefined
      }))())

      if (currItem.isDir) {
        assertNativeComponent(content, <(quickViewDirComp)(^.assertPlain[QuickViewDirProps](inside(_) {
          case QuickViewDirProps(resDispatch, resActions, resState, resStack, resWidth, resCurrItem) =>
            resDispatch shouldBe dispatch
            resActions shouldBe actions
            resState shouldBe state
            resStack shouldBe panelStack.stack
            resWidth shouldBe width
            resCurrItem shouldBe currItem
        }))())
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
            <(quickViewFileComp)(^.assertPlain[QuickViewFileProps](inside(_) {
              case QuickViewFileProps(resDispatch, inputRef, isRight, filePath, size) =>
                resDispatch shouldBe dispatch
                inputRef.current should be theSameInstanceAs panelStack.panelInput
                isRight shouldBe panelStack.isRight
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
