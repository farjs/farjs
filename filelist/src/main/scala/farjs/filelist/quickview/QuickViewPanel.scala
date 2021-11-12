package farjs.filelist.quickview

import farjs.filelist.api.FileListItem
import farjs.filelist.stack.PanelStack
import farjs.filelist.{FileListActions, FileListsStateDef}
import farjs.ui._
import farjs.ui.border._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._
import scommons.react.redux.Dispatch

case class QuickViewPanelProps(dispatch: Dispatch,
                               actions: FileListActions,
                               data: FileListsStateDef)

object QuickViewPanel extends FunctionComponent[QuickViewPanelProps] {

  private[quickview] var doubleBorderComp: UiComponent[DoubleBorderProps] = DoubleBorder
  private[quickview] var horizontalLineComp: UiComponent[HorizontalLineProps] = HorizontalLine
  private[quickview] var textLineComp: UiComponent[TextLineProps] = TextLine
  private[quickview] var quickViewDirComp: UiComponent[QuickViewDirProps] = QuickViewDir

  protected def render(compProps: Props): ReactElement = {
    val panelStack = PanelStack.usePanelStack
    val width = panelStack.width
    val height = panelStack.height
    
    val props = compProps.wrapped
    val theme = Theme.current.fileList
    val state =
      if (!panelStack.isRight) props.data.right
      else props.data.left

    val maybeCurrItem = state.currentItem.map {
      case i if i == FileListItem.up => FileListItem.currDir
      case i => i
    }

    <.box(^.rbStyle := theme.regularItem)(
      <(doubleBorderComp())(^.wrapped := DoubleBorderProps((width, height), theme.regularItem))(),
      <(horizontalLineComp())(^.wrapped := HorizontalLineProps(
        pos = (0, height - 4),
        length = width,
        lineCh = SingleBorder.horizontalCh,
        style = theme.regularItem,
        startCh = Some(DoubleBorder.leftSingleCh),
        endCh = Some(DoubleBorder.rightSingleCh)
      ))(),
      <(textLineComp())(^.wrapped := TextLineProps(
        align = TextLine.Center,
        pos = (1, 0),
        width = width - 2,
        text = "Quick view",
        style = theme.regularItem,
        focused = !state.isActive
      ))(),

      maybeCurrItem.map { currItem =>
        <.>()(
          if (currItem.isDir) {
            <(quickViewDirComp())(^.wrapped := QuickViewDirProps(
              dispatch = props.dispatch,
              actions = props.actions,
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
