package scommons.blessed.react

import scommons.blessed.raw.BlessedScreen
import scommons.blessed.react.raw._
import scommons.react.ReactElement

object ReactBlessed {

  def render(element: ReactElement, screen: BlessedScreen): Unit = {
    ReactBlessedNative.render(element, screen)
  }
}
