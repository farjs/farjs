package farjs.ui.app

import farjs.ui.theme.Theme
import scommons.react.ReactClass

import scala.scalajs.js

sealed trait LoadResult extends js.Object {
  val theme: Theme
  val mainUi: ReactClass
}

object LoadResult {

  def apply(theme: Theme, mainUi: ReactClass): LoadResult = {
    js.Dynamic.literal(
      theme = theme,
      mainUi = mainUi
    ).asInstanceOf[LoadResult]
  }
}
