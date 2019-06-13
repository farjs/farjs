package scommons.farc.ui.filelist

import scommons.farc.api.filelist.FileListApi
import scommons.farc.ui._
import scommons.farc.ui.border._
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

case class FilePanelProps(api: FileListApi, size: (Int, Int))

object FilePanel extends FunctionComponent[FilePanelProps] {

  protected def render(compProps: Props): ReactElement = {
    val (state, setState) = useState(() => FileListState())
    
    val props = compProps.wrapped
    val (width, height) = props.size
    val styles = FileListView.styles

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
        api = props.api,
        size = (width - 2, height - 5),
        columns = 3,
        state = state,
        onStateChanged = setState
      ))(),
      <(TextLine())(^.wrapped := TextLineProps(
        align = TextLine.Center,
        pos = (1, 0),
        width = width - 2,
        text = state.currDir,
        style = styles.normalItem,
        focused = true
      ))(),
      <(TextLine())(^.wrapped := TextLineProps(
        align = TextLine.Left,
        pos = (1, height - 3),
        width = width - 2 - 12,
        text = state.currentItem.map(_.name).getOrElse(""),
        style = styles.normalItem,
        padding = 0
      ))(),
      <(TextLine())(^.wrapped := TextLineProps(
        align = TextLine.Right,
        pos = (1 + width - 2 - 12, height - 3),
        width = 12,
        text = "123456",
        style = styles.normalItem,
        padding = 0
      ))(),
      <(TextLine())(^.wrapped := TextLineProps(
        align = TextLine.Center,
        pos = (1, height - 1),
        width = (width - 2) / 2,
        text = "123 4567 890 (3)",
        style = styles.normalItem
      ))(),
      <(TextLine())(^.wrapped := TextLineProps(
        align = TextLine.Center,
        pos = ((width - 2) / 2, height - 1),
        width = (width - 2) / 2,
        text = "123 4567 890",
        style = styles.normalItem
      ))()
    )
  }
}
