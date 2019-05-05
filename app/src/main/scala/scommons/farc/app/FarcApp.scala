package scommons.farc.app

import scommons.farc.ui._
import scommons.farc.ui.list._
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.raw.{Blessed, ReactBlessed}
import scommons.react.hooks._

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
      val (demo, setDemo) = useState(0)

      <.>()(
        <.box(
          ^.rbWidth := "25%",
          ^.rbHeight := "100%-1",
          ^.rbBorder := new BlessedBorder {
            override val `type` = "line"
          },
          ^.rbStyle := new BlessedStyle {
            override val fg = "white"
            override val bg = "blue"
            override val border = new BlessedBorderStyle {
              override val fg = "white"
              override val bg = "blue"
            }
          }
        )(
          <.text(
            ^.rbStyle := new BlessedStyle {
              override val fg = "black"
              override val bg = "cyan"
            },
            ^.content := s" $demo "
          )(),

          <.button(
            ^.rbMouse := true,
            ^.rbStyle := new BlessedStyle {
              override val fg = "black"
              override val bg = "cyan"
              override val focus = new BlessedStyle {
                override val fg = "white"
              }
            },
            ^.rbShadow := true,
            ^.rbHeight := 1, ^.rbWidth := 3, ^.rbTop := 2, ^.rbLeft := 6,
            ^.rbOnPress := { () =>
              setDemo(demo + 1)
              println("increment")
            },
            ^.content := " + "
          )(),

          <.button(
            ^.rbMouse := true,
            ^.rbStyle := new BlessedStyle {
              override val fg = "black"
              override val bg = "cyan"
              override val focus = new BlessedStyle {
                override val fg = "white"
              }
            },
            ^.rbShadow := true,
            ^.rbHeight := 1, ^.rbWidth := 3, ^.rbTop := 2,
            ^.rbOnPress := { () =>
              setDemo(demo - 1)
              println("decrement")
            },
            ^.content := " - "
          )()
        ),
        <.box(
          ^.rbWidth := "25%",
//          ^.rbHeight := "100%-1",
          ^.rbHeight := "20%",
          ^.rbLeft := "25%"
        )(
          <(WithSize())(^.wrapped := WithSizeProps({ (width, height) =>
            <(VerticalList())(^.wrapped := VerticalListProps(
              size = (width, height),
              columns = 2,
              items = List(
                "item 1",
                "item 2",
                "item 3",
                "item 4",
                "item 5",
                "item 6",
                "item 7"
              )
            ))()
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
  }
}
