package farjs.filelist.copy

import farjs.filelist.api.FileListItem
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

case class CopyItemsPopupProps(path: String,
                               items: Seq[FileListItem],
                               onCopy: String => Unit,
                               onCancel: () => Unit)

object CopyItemsPopup extends FunctionComponent[CopyItemsPopupProps] {

  private[copy] var popupComp: UiComponent[PopupProps] = Popup
  private[copy] var doubleBorderComp: UiComponent[DoubleBorderProps] = DoubleBorder
  private[copy] var textLineComp: UiComponent[TextLineProps] = TextLine
  private[copy] var textBoxComp: UiComponent[TextBoxProps] = TextBox
  private[copy] var horizontalLineComp: UiComponent[HorizontalLineProps] = HorizontalLine
  private[copy] var buttonsPanelComp: UiComponent[ButtonsPanelProps] = ButtonsPanel

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (path, setPath) = useState(props.path)
    val (width, height) = (75, 8)
    val theme = Theme.current.popup.regular
    
    val count = props.items.size
    val itemsText =
      if (count > 1) s"$count items"
      else s"${props.items.headOption.map(i => s""""${i.name}"""").getOrElse("")}"

    val onCopy = { () =>
      if (path.nonEmpty) {
        props.onCopy(path)
      }
    }
    
    val actions = List(
      "[ Copy ]" -> onCopy,
      "[ Cancel ]" -> props.onCancel
    )

    <(popupComp())(^.wrapped := PopupProps(onClose = props.onCancel))(
      <.box(
        ^.rbClickable := true,
        ^.rbAutoFocus := false,
        ^.rbWidth := width,
        ^.rbHeight := height,
        ^.rbTop := "center",
        ^.rbLeft := "center",
        ^.rbShadow := true,
        ^.rbStyle := theme
      )(
        <(doubleBorderComp())(^.wrapped := DoubleBorderProps(
          size = (width - 6, height - 2),
          style = theme,
          pos = (3, 1),
          title = Some("Copy")
        ))(),
        
        <(textLineComp())(^.wrapped := TextLineProps(
          align = TextLine.Left,
          pos = (4, 2),
          width = width - 8,
          text = s"Copy $itemsText to:",
          style = theme
        ))(),
        <(textBoxComp())(^.wrapped := TextBoxProps(
          pos = (5, 3),
          width = width - 10,
          value = path,
          onChange = { value =>
            setPath(value)
          },
          onEnter = onCopy
        ))(),
        <(horizontalLineComp())(^.wrapped := HorizontalLineProps(
          pos = (3, 4),
          length = width - 6,
          lineCh = SingleBorder.horizontalCh,
          style = theme,
          startCh = Some(DoubleBorder.leftSingleCh),
          endCh = Some(DoubleBorder.rightSingleCh)
        ))(),

        <(buttonsPanelComp())(^.wrapped := ButtonsPanelProps(
          top = height - 3,
          actions = actions,
          style = theme,
          margin = 2
        ))()
      )
    )
  }
}
