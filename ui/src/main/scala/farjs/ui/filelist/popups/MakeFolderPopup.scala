package farjs.ui.filelist.popups

import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

case class MakeFolderPopupProps(folderName: String,
                                multiple: Boolean,
                                onOk: (String, Boolean) => Unit,
                                onCancel: () => Unit)

object MakeFolderPopup extends FunctionComponent[MakeFolderPopupProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (folderName, setFolderName) = useState(props.folderName)
    val (multiple, setMultiple) = useState(props.multiple)
    val (width, height) = (75, 10)
    val theme = Theme.current.popup.regular

    val onOk = { () =>
      if (folderName.nonEmpty) {
        props.onOk(folderName, multiple)
      }
    }
    
    val buttons = List(
      "[ OK ]" -> onOk,
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
        ^.rbStyle := theme,
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
        ^.rbStyle := theme
      )(
        <(DoubleBorder())(^.wrapped := DoubleBorderProps(
          size = (width - 6, height - 2),
          style = theme,
          pos = (3, 1),
          title = Some("Make Folder")
        ))(),
        
        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Left,
          pos = (4, 2),
          width = width - 8,
          text = "Create the folder",
          style = theme
        ))(),
        <(TextBox())(^.wrapped := TextBoxProps(
          pos = (5, 3),
          width = width - 10,
          value = folderName,
          onChange = { value =>
            setFolderName(value)
          },
          onEnter = onOk
        ))(),
        <(HorizontalLine())(^.wrapped := HorizontalLineProps(
          pos = (3, 4),
          length = width - 6,
          lineCh = SingleBorder.horizontalCh,
          style = theme,
          startCh = Some(DoubleBorder.leftSingleCh),
          endCh = Some(DoubleBorder.rightSingleCh)
        ))(),

        <(CheckBox())(^.wrapped := CheckBoxProps(
          pos = (5, 5),
          value = multiple,
          label = "Process multiple names",
          style = theme,
          onChange = { () =>
            setMultiple(!multiple)
          }
        ))(),
        <(HorizontalLine())(^.wrapped := HorizontalLineProps(
          pos = (3, 6),
          length = width - 6,
          lineCh = SingleBorder.horizontalCh,
          style = theme,
          startCh = Some(DoubleBorder.leftSingleCh),
          endCh = Some(DoubleBorder.rightSingleCh)
        ))(),

        <.box(
          ^.rbWidth := buttons.map(_._1).sum + 2,
          ^.rbHeight := 1,
          ^.rbTop := height - 3,
          ^.rbLeft := "center",
          ^.rbStyle := theme
        )(
          buttons.map(_._2)
        )
      )
    )
  }
}
