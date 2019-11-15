package farclone.ui.popup

import farclone.ui._
import farclone.ui.border._
import scommons.react._
import scommons.react.blessed._

case class MessageBoxProps(title: String,
                           message: String,
                           actions: List[MessageBoxAction],
                           style: BlessedStyle = Popup.Styles.normal)

object MessageBox extends FunctionComponent[MessageBoxProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val width = 60
    val textWidth = width - 8
    val textLines = splitText(props.message, textWidth - 2) //exclude padding
    val height = 5 + textLines.size
    val onClose = props.actions.find(_.triggeredOnClose).map(_.onAction)

    val buttons = props.actions.foldLeft(List.empty[(String, () => Unit, Int)]) {
      case (result, action) =>
        val nextPos = result match {
          case Nil => 0
          case (content, _, pos) :: _ => pos + content.length
        }
        (s" ${action.label} ", action.onAction, nextPos) :: result
    }.reverse.map {
      case (content, onAction, pos) => (content.length, <.button(
        ^.key := s"$pos",
        ^.rbMouse := true,
        ^.rbHeight := 1,
        ^.rbLeft := pos,
        ^.rbStyle := props.style,
        ^.rbOnPress := onAction,
        ^.content := content
      )())
    }

    <(Popup())(^.wrapped := PopupProps(
      onClose = onClose.getOrElse(() => ()),
      closable = onClose.isDefined
    ))(
      <.box(
        ^.rbClickable := true,
        ^.rbAutoFocus := false,
        ^.rbWidth := width,
        ^.rbHeight := height,
        ^.rbTop := "center",
        ^.rbLeft := "center",
        ^.rbShadow := true,
        ^.rbStyle := props.style
      )(
        <(DoubleBorder())(^.wrapped := DoubleBorderProps(
          size = (width - 6, height - 2),
          style = props.style,
          pos = (3, 1),
          title = Some(props.title)
        ))(),
        
        textLines.zipWithIndex.map { case (text, index) =>
          <(TextLine())(^.key := s"$index", ^.wrapped := TextLineProps(
            align = TextLine.Center,
            pos = (4, 2 + index),
            width = textWidth,
            text = text,
            style = props.style
          ))()
        },
        
        <.box(
          ^.rbWidth := buttons.map(_._1).sum,
          ^.rbHeight := 1,
          ^.rbTop := height - 3,
          ^.rbLeft := "center",
          ^.rbStyle := props.style
        )(
          buttons.map(_._2)
        )
      )
    )
  }
  
  private[popup] def splitText(text: String, maxLen: Int): List[String] = {
    val parts = text.split(" ")
    
    parts.foldLeft(List.empty[String]) {
      case (Nil, item) => List(item)
      case (head::tail, item) =>
        if ((head.length + item.length + 1) > maxLen) {
          item :: head :: tail
        } else {
          s"$head $item" :: tail
        }
    }.reverse
  }
}
