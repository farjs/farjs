package scommons.farc.app

import scommons.farc.ui._
import scommons.farc.ui.border._
import scommons.farc.ui.list._
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.raw.{Blessed, ReactBlessed}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel(name = "FarcApp")
object FarcApp {

  @JSExport("start")
  def start(): BlessedScreen = {
    val screen = Blessed.screen(new BlessedScreenConfig {
      override val autoPadding = true
      override val smartCSR = true
      override val title = "FARc"
    })

    screen.key(js.Array("C-c", "f10"), { (_, _) =>
      process.exit(0)
    })

    ReactBlessed.render(<(FarcAppRoot())()(), screen)
    screen
  }

  object FarcAppRoot extends FunctionComponent[Unit] {

    protected def render(props: Props): ReactElement = {
      <.>()(
        <.box(
          ^.rbWidth := "50%",
          ^.rbHeight := "100%-1"
//          ^.rbBorder := new BlessedBorder {
//            override val `type` = "bg"
//            override val ch = "\u2502"
//          },
//          ^.rbStyle := new BlessedStyle {
////            override val fg = "white"
////            override val bg = "blue"
//            override val border = new BlessedBorderStyle {
//              override val fg = "white"
//              override val bg = "blue"
//            }
//          }
        )(
          <(WithSize())(^.wrapped := WithSizeProps({ (width, height) =>
            <.>()(
              <(DoubleBorder())(^.wrapped := DoubleBorderProps((width, height), borderStyle))(),
              <.box(
                ^.rbWidth := width - 2,
                ^.rbHeight := height - 2,
                ^.rbLeft := 1,
                ^.rbTop := 1
              )(
                <(VerticalList())(^.wrapped := VerticalListProps(
                  size = (width - 2, height - 2),
                  columns = 3,
                  items = (1 to 1000).toList.map { i =>
                    if (i % 10 == 0) s"item $i tooo loooooooooooooooooooooooooooooooooooooong"
                    else s"item $i"
                  }
                ))()
              )
            )
          }))()
        ),

        <.box(
          ^.rbWidth := "50%",
          ^.rbHeight := "100%-1",
          ^.rbLeft := "50%"
        )(
          <(LogPanel())()()
//          <(ColorPanel())()()
        ),

        <.box(^.rbTop := "100%-1")(
          <(BottomMenuBar())(^.wrapped := BottomMenuBarProps(
            onClick = { msg =>
              println(msg)
            }
          ))()
        )
      )
    }

    private val borderStyle = new BlessedStyle {
      override val bg = "blue"
      override val fg = "white"
    }
  }
}
