package scommons.farc.ui.filelist

import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.farc.api.filelist._
import scommons.farc.ui._
import scommons.farc.ui.border._
import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js

case class FileListPanelProps(dispatch: Dispatch,
                              actions: FileListActions,
                              state: FileListState)

object FileListPanel extends FunctionComponent[FileListPanelProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    
    val styles = FileListView.styles
    val currItem = props.state.currentItem
    val selectedItems =
      if (props.state.selectedNames.nonEmpty) {
        props.state.items.filter(i => props.state.selectedNames.contains(i.name))
      }
      else Nil

    <(WithSize())(^.wrapped := WithSizeProps({ (width, height) =>
      <.box(^.rbStyle := styles.normalItem)(
        <(DoubleBorder())(^.wrapped := DoubleBorderProps((width, height), styles.normalItem))(),
        <(HorizontalLine())(^.wrapped := HorizontalLineProps(
          pos = (0, height - 4),
          length = width,
          lineCh = SingleBorder.horizontalCh,
          style = styles.normalItem,
          startCh = Some(DoubleBorder.leftSingleCh),
          endCh = Some(DoubleBorder.rightSingleCh)
        ))(),
        <(FileList())(^.wrapped := FileListProps(
          dispatch = props.dispatch,
          actions = props.actions,
          state = props.state,
          size = (width - 2, height - 5),
          columns = 2
        ))(),
        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Center,
          pos = (1, 0),
          width = width - 2,
          text = props.state.currDir.path,
          style = styles.normalItem,
          focused = true
        ))(),

        if (selectedItems.nonEmpty) Some(
          <(TextLine())(^.wrapped := TextLineProps(
            align = TextLine.Center,
            pos = (1, height - 4),
            width = width - 2,
            text = {
              val selectedSize = selectedItems.foldLeft(0.0)((res, f) => res + f.size)
              f"$selectedSize%,.0f in ${selectedItems.size}%d file(s)"
            },
            style = styles.selectedItem
          ))()
        )
        else None,

        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Left,
          pos = (1, height - 3),
          width = width - 2 - 12,
          text = currItem.map(_.name).getOrElse(""),
          style = styles.normalItem,
          padding = 0
        ))(),
        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Right,
          pos = (1 + width - 2 - 12, height - 3),
          width = 12,
          text = currItem.filter(i => i.size > 0.0 || !i.isDir).map { i =>
            f"${i.size}%,.0f"
          }.getOrElse(""),
          style = styles.normalItem,
          padding = 0
        ))(),

        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Left,
          pos = (1, height - 2),
          width = 10,
          text = currItem.map(_.permissions).getOrElse(""),
          style = styles.normalItem,
          padding = 0
        ))(),
        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Right,
          pos = (1 + width - 2 - 25, height - 2),
          width = 25,
          text = currItem.filter(i => i.name != FileListItem.up.name).map { i =>
            val date = new js.Date(i.mtimeMs)
            s"${date.toLocaleDateString()} ${date.toLocaleTimeString()}"
          }.getOrElse(""),
          style = styles.normalItem,
          padding = 0
        ))(),

        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Center,
          pos = (1, height - 1),
          width = width - 2,
          text = {
            val files = props.state.items.filter(!_.isDir)
            val filesSize = files.foldLeft(0.0)((res, f) => res + f.size)
            f"$filesSize%,.0f (${files.size}%d)"
          },
          style = styles.normalItem
        ))()
      )
    }))()
  }
}
