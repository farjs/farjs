package farjs.filelist.quickview

import farjs.filelist.FileListState
import farjs.filelist.api.FileListItem
import farjs.filelist.stack.{PanelStack, WithPanelStacks}
import farjs.ui._
import farjs.ui.border._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

object QuickViewPanel extends FunctionComponent[Unit] {

  private[quickview] var doubleBorderComp: UiComponent[DoubleBorderProps] = DoubleBorder
  private[quickview] var horizontalLineComp: UiComponent[HorizontalLineProps] = HorizontalLine
  private[quickview] var textLineComp: UiComponent[TextLineProps] = TextLine
  private[quickview] var quickViewDirComp: UiComponent[QuickViewDirProps] = QuickViewDir

  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val panelStack = PanelStack.usePanelStack
    val width = panelStack.width
    val height = panelStack.height
    
    val theme = Theme.current.fileList
    val stack =
      if (!panelStack.isRight) stacks.rightStack
      else stacks.leftStack
    val stackItem = stack.peek[FileListState]

    val maybeCurrData = stackItem.getActions.zip(stackItem.state).flatMap {
      case ((dispatch, actions), state) =>
        state.currentItem.map {
          case i if i == FileListItem.up => (dispatch, actions, state, FileListItem.currDir)
          case i => (dispatch, actions, state, i)
        }
    }

    <.box(^.rbStyle := theme.regularItem)(
      <(doubleBorderComp())(^.plain := DoubleBorderProps(width, height, theme.regularItem))(),
      <(horizontalLineComp())(^.wrapped := HorizontalLineProps(
        pos = (0, height - 4),
        length = width,
        lineCh = SingleBorder.horizontalCh,
        style = theme.regularItem,
        startCh = Some(DoubleChars.leftSingle),
        endCh = Some(DoubleChars.rightSingle)
      ))(),
      <(textLineComp())(^.plain := TextLineProps(
        align = TextAlign.center,
        left = 1,
        top = 0,
        width = width - 2,
        text = "Quick view",
        style = theme.regularItem,
        focused = !stackItem.state.exists(_.isActive)
      ))(),

      maybeCurrData.map { case (dispatch, actions, state, currItem) =>
        <.>()(
          if (currItem.isDir) {
            <(quickViewDirComp())(^.wrapped := QuickViewDirProps(
              dispatch = dispatch,
              actions = actions,
              state = state,
              stack = panelStack.stack,
              width = width,
              currItem = currItem
            ))()
          }
          else {
            <.text(
              ^.rbLeft := 2,
              ^.rbTop := 4,
              ^.rbStyle := theme.regularItem,
              ^.content := "TODO: Display file's content here"
            )()
          },

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
    )
  }
}
