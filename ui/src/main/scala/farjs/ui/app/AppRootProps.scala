package farjs.ui.app

import farjs.ui.theme.Theme
import farjs.ui.tool.DevTool

import scala.scalajs.js

sealed trait AppRootProps extends js.Object {
  val loadMainUi: js.Function1[js.Function1[js.Any, Unit], js.Promise[LoadResult]]
  val initialDevTool: DevTool
  val defaultTheme: Theme
}

object AppRootProps {

  def apply(loadMainUi: js.Function1[js.Function1[js.Any, Unit], js.Promise[LoadResult]],
            initialDevTool: DevTool,
            defaultTheme: Theme): AppRootProps = {

    js.Dynamic.literal(
      loadMainUi = loadMainUi,
      initialDevTool = initialDevTool,
      defaultTheme = defaultTheme
    ).asInstanceOf[AppRootProps]
  }
}
