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
    
    <.box(
      ^.label := "react-blessed hooks demo",
      ^.rbBorder := new BlessedBorder {
        override val `type` = "line"
      },
      ^.rbStyle := new BlessedStyle {
        override val border = new BlessedBorderStyle {
          override val fg = "cyan"
        }
      }
    )(
      s"$demo",
      
      <.button(
        ^.mouse := true,
        ^.rbBorder := new BlessedBorder {
          override val `type` = "line"
        },
        ^.rbHeight := 3, ^.rbWidth := 3, ^.rbTop := 2, ^.rbLeft := 4,
        ^.onPress := { () =>
          setDemo(demo + 1)
        }
      )("+"),

      <.button(
        ^.mouse := true,
        ^.rbBorder := new BlessedBorder {
          override val `type` = "line"
        },
        ^.rbHeight := 3, ^.rbWidth := 3, ^.rbTop := 2,
        ^.onPress := { () =>
          setDemo(demo - 1)
        }
      )("-")
    )
  }
}
