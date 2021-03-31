package farjs.ui

import scommons.react._
import scommons.react.blessed._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ButtonsPanelProps(top: Int,
                             actions: List[(String, () => Unit)],
                             style: BlessedStyle,
                             padding: Int = 0,
                             margin: Int = 0)

object ButtonsPanel extends FunctionComponent[ButtonsPanelProps] {

  private[ui] var buttonComp: UiComponent[ButtonProps] = Button
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val padding = " " * props.padding

    val buttons = props.actions.foldLeft(List.empty[(String, () => Unit, Int)]) {
      case (result, (label, onAction)) =>
        val nextPos = result match {
          case Nil => 0
          case (content, _, pos) :: _ => pos + content.length + props.margin
        }
        (s"$padding$label$padding", onAction, nextPos) :: result
    }.reverse.map {
      case (label, onAction, pos) =>
        (label.length, <(buttonComp())(^.key := label, ^.wrapped := ButtonProps(
          pos = (pos, 0),
          label = label,
          style = props.style,
          onPress = { () =>
            Future(onAction()) //execute on the next tick
          }
        ))())
    }

    <.box(
      ^.rbWidth := buttons.map(_._1).sum + (buttons.size - 1) * props.margin,
      ^.rbHeight := 1,
      ^.rbLeft := "center",
      ^.rbTop := props.top,
      ^.rbStyle := props.style
    )(
      buttons.map(_._2)
    )
  }
}
