package farjs.filelist.copy

import farjs.filelist.api.FileListItem
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js

case class FileExistsPopupProps(newItem: FileListItem,
                                existing: FileListItem,
                                onAction: FileExistsAction => Unit,
                                onCancel: () => Unit)

object FileExistsPopup extends FunctionComponent[FileExistsPopupProps] {
  
  private[copy] var modalComp: UiComponent[ModalProps] = Modal
  private[copy] var textLineComp: UiComponent[TextLineProps] = TextLine
  private[copy] var horizontalLineComp: UiComponent[HorizontalLineProps] = HorizontalLine
  private[copy] var buttonsPanelComp: UiComponent[ButtonsPanelProps] = ButtonsPanel

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val size@(width, _) = (58, 11)
    val contentWidth = width - (paddingHorizontal + 2) * 2
    val contentLeft = 2
    val theme = Theme.current.popup.error

    def onButton(action: FileExistsAction) = { () =>
      props.onAction(action)
    }

    val actions = List(
      "Overwrite" -> onButton(FileExistsAction.Overwrite),
      "All" -> onButton(FileExistsAction.All),
      "Skip" -> onButton(FileExistsAction.Skip),
      "Skip all" -> onButton(FileExistsAction.SkipAll),
      "Append" -> onButton(FileExistsAction.Append),
      "Cancel" -> props.onCancel
    )

    <(modalComp())(^.wrapped := ModalProps("Warning", size, theme, props.onCancel))(
      <.text(
        ^.rbLeft := "center",
        ^.rbTop := 1,
        ^.rbStyle := theme,
        ^.content := "File already exists"
      )(),
      <(textLineComp())(^.wrapped := TextLineProps(
        align = TextLine.Center,
        pos = (contentLeft, 2),
        width = contentWidth,
        text = props.newItem.name,
        style = theme,
        padding = 0
      ))(),
      <(horizontalLineComp())(^.wrapped := HorizontalLineProps(
        pos = (0, 3),
        length = width - 6,
        lineCh = SingleBorder.horizontalCh,
        style = theme,
        startCh = Some(DoubleBorder.leftSingleCh),
        endCh = Some(DoubleBorder.rightSingleCh)
      ))(),
      
      <.text(
        ^.rbLeft := contentLeft,
        ^.rbTop := 4,
        ^.rbStyle := theme,
        ^.content :=
          """New
            |Existing""".stripMargin
      )(),
      <(textLineComp())(^.wrapped := TextLineProps(
        align = TextLine.Right,
        pos = (contentLeft, 4),
        width = contentWidth,
        text = {
          val date = new js.Date(props.newItem.mtimeMs)
          f"${props.newItem.size}%.0f ${date.toLocaleDateString()} ${date.toLocaleTimeString()}"
        },
        style = theme,
        padding = 0
      ))(),
      <(textLineComp())(^.wrapped := TextLineProps(
        align = TextLine.Right,
        pos = (contentLeft, 5),
        width = contentWidth,
        text = {
          val date = new js.Date(props.existing.mtimeMs)
          f"${props.existing.size}%.0f ${date.toLocaleDateString()} ${date.toLocaleTimeString()}"
        },
        style = theme,
        padding = 0
      ))(),
      
      <(horizontalLineComp())(^.wrapped := HorizontalLineProps(
        pos = (0, 6),
        length = width - paddingHorizontal * 2,
        lineCh = SingleBorder.horizontalCh,
        style = theme,
        startCh = Some(DoubleBorder.leftSingleCh),
        endCh = Some(DoubleBorder.rightSingleCh)
      ))(),
      <(buttonsPanelComp())(^.wrapped := ButtonsPanelProps(
        top = 7,
        actions = actions,
        style = theme,
        padding = 1
      ))()
    )
  }
}
