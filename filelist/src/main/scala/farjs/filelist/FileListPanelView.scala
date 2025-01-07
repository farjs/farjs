package farjs.filelist

import farjs.filelist.api.FileListItem
import farjs.filelist.sort.{SortIndicator, SortIndicatorProps}
import farjs.filelist.stack.WithStack
import farjs.filelist.theme.FileListTheme
import farjs.ui._
import farjs.ui.border._
import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js

object FileListPanelView extends FunctionComponent[FileListPanelViewProps] {

  private[filelist] var doubleBorderComp: ReactClass = DoubleBorder
  private[filelist] var horizontalLineComp: ReactClass = HorizontalLine
  private[filelist] var fileListComp: UiComponent[FileListProps] = FileList
  private[filelist] var textLineComp: ReactClass = TextLine
  private[filelist] var sortIndicator: ReactClass = SortIndicator

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val panelStack = WithStack.useStack()
    val width = panelStack.width
    val height = panelStack.height
    val theme = FileListTheme.useTheme().fileList
    
    val currItem = FileListState.currentItem(props.state)
    val selectedItems = FileListState.selectedItems(props.state)

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
      <(fileListComp())(^.plain := FileListProps(
        dispatch = props.dispatch,
        actions = props.actions,
        state = props.state,
        width = width - 2,
        height = height - 5,
        columns = 2,
        onKeypress = props.onKeypress
      ))(),
      <(textLineComp)(^.plain := TextLineProps(
        align = TextAlign.center,
        left = 1,
        top = 0,
        width = width - 2,
        text = props.state.currDir.path,
        style = theme.regularItem,
        focused = panelStack.stack.isActive
      ))(),
      <(sortIndicator)(^.plain := SortIndicatorProps(
        sort = props.state.sort
      ))(),

      if (selectedItems.nonEmpty) Some(
        <(textLineComp)(^.plain := TextLineProps(
          align = TextAlign.center,
          left = 1,
          top = height - 4,
          width = width - 2,
          text = {
            val selectedSize = selectedItems.foldLeft(0.0)((res, f) => res + f.size)
            val count = selectedItems.size
            val files = if (count == 1) "file" else "files"
            f"$selectedSize%,.0f in $count%d $files"
          },
          style = theme.selectedItem
        ))()
      )
      else None,

      <(textLineComp)(^.plain := TextLineProps(
        align = TextAlign.left,
        left = 1,
        top = height - 3,
        width = width - 2 - 12,
        text = currItem.map(_.name).getOrElse(""),
        style = theme.regularItem,
        padding = 0
      ))(),
      <(textLineComp)(^.plain := TextLineProps(
        align = TextAlign.right,
        left = 1 + width - 2 - 12,
        top = height - 3,
        width = 12,
        text = currItem.filter(i => i.size > 0.0 || !i.isDir).map { i =>
          if (i.size >= 1000000000) {
            f"~${i.size/1000000000}%,.0f G"
          } else f"${i.size}%,.0f"
        }.getOrElse(""),
        style = theme.regularItem,
        padding = 0
      ))(),

      <(textLineComp)(^.plain := TextLineProps(
        align = TextAlign.left,
        left = 1,
        top = height - 2,
        width = 10,
        text = currItem.map(_.permissions).getOrElse(""),
        style = theme.regularItem,
        padding = 0
      ))(),
      <(textLineComp)(^.plain := TextLineProps(
        align = TextAlign.right,
        left = 1 + width - 2 - 25,
        top = height - 2,
        width = 25,
        text = currItem.filter(i => i.name != FileListItem.up.name).map { i =>
          val date = new js.Date(i.mtimeMs)
          s"${date.toLocaleDateString()} ${date.toLocaleTimeString()}"
        }.getOrElse(""),
        style = theme.regularItem,
        padding = 0
      ))(),

      <(textLineComp)(^.plain := TextLineProps(
        align = TextAlign.center,
        left = 1,
        top = height - 1,
        width = if (props.state.diskSpace.isEmpty) width - 2 else (width - 2) / 2,
        text = {
          val files = props.state.currDir.items.filter(!_.isDir)
          val filesSize = files.foldLeft(0.0)((res, f) => res + f.size)
          f"$filesSize%,.0f (${files.size}%d)"
        },
        style = theme.regularItem
      ))(),
      props.state.diskSpace.map { bytes =>
        <(textLineComp)(^.plain := TextLineProps(
          align = TextAlign.center,
          left = (width - 2) / 2 + 1,
          top = height - 1,
          width = (width - 2) / 2,
          text = f"$bytes%,.0f",
          style = theme.regularItem
        ))()
      }
    )
  }
}
