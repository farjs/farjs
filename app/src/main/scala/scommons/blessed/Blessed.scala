package scommons.blessed

import scommons.blessed.raw._

object Blessed {

  def screen(config: BlessedScreenConfig): BlessedScreen = {
    BlessedNative.screen(config)
  }
}
