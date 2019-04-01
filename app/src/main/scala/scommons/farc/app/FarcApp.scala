package scommons.farc.app

import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

object FarcApp {

  def main(args: Array[String]): Unit = {

    val screen = Blessed.screen(new BlessedScreenConfig {
      override val autoPadding = true
      override val smartCSR = true
      override val title = "FARc"
    })

    screen.key(js.Array("C-c", "f10"), { (_, _) =>
      process.exit(0)
    })
    
    ReactBlessed.render(<(FarcAppRoot())()(), screen)
  }
}

object FarcAppRoot extends FunctionComponent[Unit] {
  
  protected def render(props: Props): ReactElement = {
    val (demo, setDemo) = useState(0)
    val (logs, setLogs) = useState(List.empty[String])

    <.>()(
      <.box(
        ^.rbWidth := "50%",
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
          ^.mouse := true,
          ^.rbStyle := new BlessedStyle {
            override val fg = "black"
            override val bg = "cyan"
            override val focus = new BlessedStyle {
              override val fg = "white"
            }
          },
          ^.shadow := true,
          ^.rbHeight := 1, ^.rbWidth := 3, ^.rbTop := 2, ^.rbLeft := 6,
          ^.onPress := { () =>
            setDemo(demo + 1)
            setLogs(logs :+ "increment")
          },
          ^.content := " + "
        )(),
  
        <.button(
          ^.mouse := true,
          ^.rbStyle := new BlessedStyle {
            override val fg = "black"
            override val bg = "cyan"
            override val focus = new BlessedStyle {
              override val fg = "white"
            }
          },
          ^.shadow := true,
          ^.rbHeight := 1, ^.rbWidth := 3, ^.rbTop := 2,
          ^.onPress := { () =>
            setDemo(demo - 1)
            setLogs(logs :+ "decrement")
          },
          ^.content := " - "
        )()
      ),

      <.log(
        ^.mouse := true,
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1",
        ^.rbLeft := "50%",
        ^.rbStyle := new BlessedStyle {
          override val scrollbar = new BlessedScrollBarStyle {
            override val bg = "cyan"
          }
        },
        ^.scrollbar := true,
        ^.scrollable := true,
        ^.alwaysScroll := true,
        ^.content := logs.mkString("\n")
      )(),

      <.box(
        ^.rbTop := "100%-1",
        ^.scrollable := true
      )(
        "menu bar"
      )
    )
  }
}
