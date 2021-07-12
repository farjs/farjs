package farjs.app.util

import farjs.ui.TextBox
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

case class DevToolPanelProps(devTool: DevTool,
                             logContent: String,
                             onActivate: DevTool => Unit)

object DevToolPanel extends FunctionComponent[DevToolPanelProps] {

  private[util] var logPanelComp: UiComponent[LogPanelProps] = LogPanel
  private[util] var colorPanelComp: UiComponent[Unit] = ColorPanel

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val theme = Theme.current.popup.menu

    val comp = props.devTool match {
      case DevTool.Hidden => null
      case DevTool.Logs => <(logPanelComp())(^.wrapped := LogPanelProps(props.logContent))()
      case DevTool.Colors => <(colorPanelComp())()()
    }

    val tabs = List[(DevTool, String)](
      (DevTool.Logs, "Logs"),
      (DevTool.Colors, "Colors")
    ).foldLeft(List.empty[(DevTool, String, Int)]) {
      case (result, (tool, label)) =>
        val nextPos = result match {
          case Nil => 0
          case (_, content, pos) :: _ => pos + content.length
        }
        (tool, s" $label ", nextPos) :: result
    }.reverse.map {
      case (tool, label, pos) =>
        (label.length, <.text(
          ^.key := s"$pos",
          ^.rbAutoFocus := false,
          ^.rbClickable := true,
          ^.rbTags := true,
          ^.rbMouse := true,
          ^.rbLeft := pos,
          ^.rbOnClick := { _ =>
            props.onActivate(tool)
          },
          ^.content := {
            val style =
              if (tool == props.devTool) theme.focus.getOrElse(theme)
              else theme

            TextBox.renderText(style, label)
          }
        )())
    }
    
    <.>()(
      <.box(
        ^.rbWidth := "100%",
        ^.rbHeight := 1,
        ^.rbStyle := theme
      )(
        <.box(
          ^.rbWidth := tabs.map(_._1).sum,
          ^.rbHeight := 1,
          ^.rbLeft := "center"
        )(tabs.map(_._2))
      ),

      <.box(^.rbTop := 1)(comp)
    )
  }
}
