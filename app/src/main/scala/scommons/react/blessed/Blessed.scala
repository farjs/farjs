package scommons.react.blessed

object Blessed {

  def screen(config: BlessedScreenConfig): BlessedScreen = {
    raw.BlessedNative.screen(config)
  }
}
