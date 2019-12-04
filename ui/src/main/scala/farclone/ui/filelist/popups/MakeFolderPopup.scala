package farclone.ui.filelist.popups

import farclone.ui._
import farclone.ui.border._
import farclone.ui.popup._
import scommons.react._
import scommons.react.hooks._
import scommons.react.blessed._

case class MakeFolderPopupProps(folderName: String,
                                multiple: Boolean,
                                onOk: (String, Boolean) => Unit,
                                onCancel: () => Unit)

object MakeFolderPopup extends FunctionComponent[MakeFolderPopupProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (multiple, setMultiple) = useState(props.multiple)
    val (width, height) = (75, 10)
    val style = Popup.Styles.normal

    val buttons = List(
      "[ OK ]" -> { () =>
        //TODO: handle OK action
      },
      "[ Cancel ]" -> props.onCancel
    ).foldLeft(List.empty[(String, () => Unit, Int)]) {
      case (result, (label, action)) =>
        val nextPos = result match {
          case Nil => 0
          case (content, _, pos) :: _ => pos + content.length + 2
        }
        (label, action, nextPos) :: result
    }.reverse.map {
      case (content, onAction, pos) => (content.length, <.button(
        ^.key := s"$pos",
        ^.rbMouse := true,
        ^.rbWidth := content.length,
        ^.rbHeight := 1,
        ^.rbLeft := pos,
        ^.rbStyle := style,
        ^.rbOnPress := onAction,
        ^.content := content
      )())
    }

    <(Popup())(^.wrapped := PopupProps(onClose = props.onCancel))(
      <.box(
        ^.rbClickable := true,
        ^.rbAutoFocus := false,
        ^.rbWidth := width,
        ^.rbHeight := height,
        ^.rbTop := "center",
        ^.rbLeft := "center",
        ^.rbShadow := true,
        ^.rbStyle := style
      )(
        <(DoubleBorder())(^.wrapped := DoubleBorderProps(
          size = (width - 6, height - 2),
          style = style,
          pos = (3, 1),
          title = Some("Make Folder")
        ))(),
        
        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Left,
          pos = (4, 2),
          width = width - 8,
          text = "Create the folder",
          style = style
        ))(),
        <(TextBox())(^.wrapped := TextBoxProps(
          pos = (5, 3),
          width = width - 10,
          value = "initial folder name",
          style = style.focus.getOrElse(null),
          onChange = { _ =>
          }
        ))(),
        <(HorizontalLine())(^.wrapped := HorizontalLineProps(
          pos = (3, 4),
          length = width - 6,
          lineCh = SingleBorder.horizontalCh,
          style = style,
          startCh = Some(DoubleBorder.leftSingleCh),
          endCh = Some(DoubleBorder.rightSingleCh)
        ))(),

        <(CheckBox())(^.wrapped := CheckBoxProps(
          pos = (5, 5),
          value = multiple,
          label = "Process multiple names",
          style = style,
          onChange = { () =>
            setMultiple(!multiple)
          }
        ))(),
        <(HorizontalLine())(^.wrapped := HorizontalLineProps(
          pos = (3, 6),
          length = width - 6,
          lineCh = SingleBorder.horizontalCh,
          style = style,
          startCh = Some(DoubleBorder.leftSingleCh),
          endCh = Some(DoubleBorder.rightSingleCh)
        ))(),

        <.box(
          ^.rbWidth := buttons.map(_._1).sum + 2,
          ^.rbHeight := 1,
          ^.rbTop := height - 3,
          ^.rbLeft := "center",
          ^.rbStyle := style
        )(
          buttons.map(_._2)
        )
      )
    )
  }
}
