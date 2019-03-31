package scommons.react.blessed

import scommons.react.ReactElement

object ReactBlessed {

  def render(element: ReactElement, screen: BlessedScreen): Unit = {
    raw.ReactBlessedNative.render(element, screen)
  }
}
