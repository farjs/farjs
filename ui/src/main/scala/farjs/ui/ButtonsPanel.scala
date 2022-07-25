package farjs.ui

import scommons.react._
import scommons.react.blessed._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

object ButtonsPanel extends FunctionComponent[ButtonsPanelProps] {

  private[ui] var buttonComp: UiComponent[ButtonProps] = Button
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val padding = " " * props.padding.getOrElse(0)
    val margin = props.margin.getOrElse(0)

    val buttons = props.actions.foldLeft(List.empty[(String, js.Function0[Unit], Int)]) {
      case (result, action) =>
        val nextPos = result match {
          case Nil => 0
          case (content, _, pos) :: _ => pos + content.length + margin
        }
        (s"$padding${action.label}$padding", action.onAction, nextPos) :: result
    }.reverse.map {
      case (label, onAction, pos) =>
        (label.length, <(buttonComp())(^.key := label, ^.plain := ButtonProps(
          left = pos,
          top = 0,
          label = label,
          style = props.style,
          onPress = { () =>
            Future(onAction()) //execute on the next tick
          }
        ))())
    }

    <.box(
      ^.rbWidth := buttons.map(_._1).sum + (buttons.size - 1) * margin,
      ^.rbHeight := 1,
      ^.rbLeft := "center",
      ^.rbTop := props.top,
      ^.rbStyle := props.style
    )(
      buttons.map(_._2)
    )
  }
}
