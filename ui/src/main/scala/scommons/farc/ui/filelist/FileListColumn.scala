package scommons.farc.ui.filelist

import scommons.farc.api.filelist.FileListItem
import scommons.farc.ui._
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.raw.Blessed

case class FileListColumnProps(size: (Int, Int),
                               left: Int,
                               borderCh: String,
                               items: Seq[FileListItem],
                               focusedIndex: Int,
                               selectedNames: Set[String])

object FileListColumn extends FunctionComponent[FileListColumnProps] {

  override protected def create(): ReactClass = {
    ReactMemo[Props](super.create(), { (prevProps, nextProps) =>
      prevProps.wrapped == nextProps.wrapped
    })
  }
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (width, height) = props.size
    val styles = FileListView.styles
    
    val borderEnd = renderText(
      isBold = false,
      fgColor = styles.normalItem.fg.orNull,
      bgColor = styles.normalItem.bg.orNull,
      text = props.borderCh
    )
    val overlapEnd = renderText(
      isBold = false,
      fgColor = styles.overlapColor,
      bgColor = styles.normalItem.bg.orNull,
      text = "}"
    )

    def renderItems(): Seq[String] = props.items.zipWithIndex.map {
      case (item, index) =>
        val name = item.name
        val style = {
          val style =
            if (props.selectedNames.contains(name)) styles.selectedItem
            else if (name.startsWith(".") && name != FileListItem.up.name) styles.hiddenItem
            else if (item.isDir && name != FileListItem.up.name) styles.dirItem
            else styles.normalItem
          
          val focused = props.focusedIndex == index
          if (focused) style.focus.getOrElse(null)
          else style
        }

        val text = name.trim
        renderText(
          isBold = style.bold.getOrElse(false),
          fgColor = style.fg.orNull,
          bgColor = style.bg.orNull,
          text = text.take(width).padTo(width, ' '),
          ending = if (text.length > width) overlapEnd else borderEnd
        )
    }

    val itemsContent = renderItems().mkString("\n")

    <.box(
      ^.rbWidth := width,
      ^.rbHeight := height,
      ^.rbLeft := props.left,
      ^.rbStyle := styles.normalItem
    )(
      <(TextLine())(^.wrapped := TextLineProps(
        align = TextLine.Center,
        pos = (0, 0),
        width = width,
        text = "Name",
        style = styles.headerStyle,
        padding = 0
      ))(),
      
      if (itemsContent.nonEmpty) Some(
        <.text(
          ^.rbWidth := width + 1,
          ^.rbTop := 1,
          ^.rbTags := true,
          ^.content := itemsContent
        )()
      )
      else None,

      compProps.children // just for testing memo/re-render
    )
  }

  private def renderText(isBold: Boolean,
                         fgColor: String,
                         bgColor: String,
                         text: String,
                         ending: String = ""): String = {

    val bold = if (isBold) "{bold}" else ""

    s"$bold{$fgColor-fg}{$bgColor-bg}${Blessed.escape(text)}{/}$ending"
  }
}
