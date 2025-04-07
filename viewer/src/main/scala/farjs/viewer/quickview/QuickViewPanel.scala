package farjs.viewer.quickview

import farjs.filelist.{FileListData, FileListState}
import farjs.filelist.api.FileListItem
import farjs.filelist.stack.{WithStack, WithStacks}
import farjs.filelist.theme.FileListTheme
import farjs.ui._
import farjs.ui.border._
import scommons.nodejs.path
import scommons.react._
import scommons.react.blessed._

object QuickViewPanel extends FunctionComponent[Unit] {

  private[quickview] var doubleBorderComp: ReactClass = DoubleBorder
  private[quickview] var horizontalLineComp: ReactClass = HorizontalLine
  private[quickview] var textLineComp: ReactClass = TextLine
  private[quickview] var quickViewDirComp: ReactClass = QuickViewDir
  private[quickview] var quickViewFileComp: UiComponent[QuickViewFileProps] = QuickViewFile

  protected def render(compProps: Props): ReactElement = {
    val stacks = WithStacks.useStacks()
    val panelStack = WithStack.useStack()
    val width = panelStack.width
    val height = panelStack.height
    
    val theme = FileListTheme.useTheme().fileList
    val stack =
      if (!panelStack.isRight) stacks.right.stack
      else stacks.left.stack
    val stackItem = stack.peek[FileListState]()

    val maybeCurrData = stackItem.getData().toOption.flatMap {
      case FileListData(dispatch, actions, state) =>
        FileListState.currentItem(state).toOption.map {
          case i if i == FileListItem.up => (dispatch, actions, state, FileListItem.currDir)
          case i => (dispatch, actions, state, i)
        }
    }

    <.box(^.rbStyle := theme.regularItem)(
      <(doubleBorderComp)(^.plain := DoubleBorderProps(width, height, theme.regularItem))(),
      <(horizontalLineComp)(^.plain := HorizontalLineProps(
        left = 0,
        top = height - 4,
        length = width,
        lineCh = SingleChars.horizontal,
        style = theme.regularItem,
        startCh = DoubleChars.leftSingle,
        endCh = DoubleChars.rightSingle
      ))(),
      <(textLineComp)(^.plain := TextLineProps(
        align = TextAlign.center,
        left = 1,
        top = 0,
        width = width - 2,
        text = "Quick view",
        style = theme.regularItem,
        focused = panelStack.stack.isActive
      ))(),

      maybeCurrData.map { case (dispatch, actions, state, currItem) =>
        <.>()(
          if (currItem.isDir) {
            <(quickViewDirComp)(^.plain := QuickViewDirProps(
              dispatch = dispatch,
              actions = actions,
              state = state,
              stack = panelStack.stack,
              width = width,
              currItem = currItem
            ))()
          }
          else {
            val filePath = path.join(state.currDir.path, currItem.name)
            <.box(
              ^.rbLeft := 1,
              ^.rbTop := 1,
              ^.rbWidth := width - 2,
              ^.rbHeight := height - 5,
              ^.rbStyle := theme.regularItem
            )(
              <(quickViewFileComp())(
                ^.key := filePath,
                ^.plain := QuickViewFileProps(
                  dispatch = dispatch,
                  panelStack = panelStack,
                  filePath = filePath,
                  size = currItem.size
                )
              )()
            )
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
