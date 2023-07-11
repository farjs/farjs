package farjs.filelist

import farjs.filelist.api.FileListItem
import farjs.filelist.theme.FileListTheme
import farjs.ui._
import scommons.react._
import scommons.react.blessed._

case class FileListColumnProps(size: (Int, Int),
                               left: Int,
                               borderCh: String,
                               items: Seq[FileListItem],
                               focusedIndex: Int,
                               selectedNames: Set[String])

object FileListColumn extends FunctionComponent[FileListColumnProps] {

  private[filelist] var textLineComp: ReactClass = TextLine

  override protected def create(): ReactClass = {
    ReactMemo[Props](super.create(), { (prevProps, nextProps) =>
      prevProps.wrapped == nextProps.wrapped
    })
  }
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (width, height) = props.size
    val theme = FileListTheme.useTheme.fileList
    
    val borderEnd = UI.renderText2(theme.regularItem, props.borderCh)
    val overlapEnd = UI.renderText2(theme.regularItem, "}")

    def renderItems(): Seq[String] = props.items.zipWithIndex.map {
      case (item, index) =>
        val name = item.name
        val style = {
          val style =
            if (props.selectedNames.contains(name)) theme.selectedItem
            else if (name.startsWith(".") && name != FileListItem.up.name) theme.hiddenItem
            else if (item.isDir && name != FileListItem.up.name) theme.dirItem
            else {
              val nameLower = name.toLowerCase
              if (nameLower.endsWith(".zip") || nameLower.endsWith(".jar")) theme.archiveItem
              else theme.regularItem
            }
          
          val focused = props.focusedIndex == index
          if (focused) style.focus.getOrElse(null)
          else style
        }

        val text = UiString(name
          .replace("\n", "")
          .replace("\r", "")
          .replace('\t', ' '))
        val content = UI.renderText(
          isBold = style.bold.getOrElse(false),
          fgColor = style.fg.orNull,
          bgColor = style.bg.orNull,
          text = text.ensureWidth(width, ' ')
        )
        val ending = if (text.strWidth > width) overlapEnd else borderEnd
        s"$content$ending"
    }

    val itemsContent = renderItems().mkString("\n")

    <.box(
      ^.rbWidth := width,
      ^.rbHeight := height,
      ^.rbLeft := props.left,
      ^.rbStyle := theme.regularItem
    )(
      <(textLineComp)(^.plain := TextLineProps(
        align = TextAlign.center,
        left = 0,
        top = 0,
        width = width,
        text = "Name",
        style = theme.header,
        padding = 0
      ))(),
      
      if (itemsContent.nonEmpty) Some(
        <.text(
          ^.rbWidth := width + 1,
          ^.rbTop := 1,
          ^.rbTags := true,
          ^.rbWrap := false,
          ^.content := itemsContent
        )()
      )
      else None,

      compProps.children // just for testing memo/re-render
    )
  }
}
