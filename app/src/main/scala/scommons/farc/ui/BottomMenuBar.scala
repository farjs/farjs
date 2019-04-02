package scommons.farc.ui

import scommons.react._
import scommons.react.blessed._

case class BottomMenuBarProps(onClick: String => Unit)

object BottomMenuBar extends FunctionComponent[BottomMenuBarProps] {
  
  protected def render(props: Props): ReactElement = {
    val data = props.wrapped
    
    <.box(^.rbTop := "100%-1")(
      items.map { case (id, key, name, pos) =>
        <.button(
          ^.key := id,
          ^.tags := true,
          ^.mouse := true,
          ^.rbLeft := pos,
          ^.onClick := { () =>
            data.onClick(id)
          },
          ^.content := s"{white-fg}{black-bg}$key{/}{black-fg}{cyan-bg}$name{/}"
        )()
      }
    )
  }

  private val items = List(
    (" 1", "       "),
    (" 2", "       "),
    (" 3", "       "),
    (" 4", "       "),
    (" 5", "       "),
    (" 6", "       "),
    (" 7", "       "),
    (" 8", "       "),
    (" 9", "       "),
    ("10", " Exit ")
  ).foldLeft(List.empty[(String, String, String, Int)]) { case (res, (key, name)) =>
    val pos =
      if (res.isEmpty) 0
      else {
        val (_, key, name, pos) = res.last
        pos + key.length + name.length
      }

    res :+ ((key.trim, key, name, pos))
  }
}
