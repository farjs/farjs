package scommons.farc.app

import scommons.blessed._
import scommons.blessed.react._
import scommons.nodejs._
import scommons.react._

import scala.scalajs.js

object FarcApp {

  def main(args: Array[String]): Unit = {

    val screen = Blessed.screen(new BlessedScreenConfig {
      override val autoPadding = true
      override val smartCSR = true
      override val title = "react-blessed hello world"
    })

    screen.key(js.Array("C-c", "f10"), { (_, _) =>
      process.exit(0)
    })
    
    ReactBlessed.render(<(FarcAppRoot())()(), screen)
  }
}

object FarcAppRoot extends FunctionComponent[Unit] {
  
  protected def render(props: Props): ReactElement = {
    <("box")(
      ^("mouse") := "true",
      ^("top") := "center",
      ^("left") := "center",
      ^("width") := "50%",
      ^("height") := "50%"
    )(
      "Hello World!"
    )
  }
}
