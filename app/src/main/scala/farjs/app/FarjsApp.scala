package farjs.app

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel, JSImport}

@JSExportTopLevel(name = "FarjsApp")
object FarjsApp {

  @JSExport("start")
  def start(showDevTools: Boolean = false,
            onReady: js.UndefOr[js.Function0[Unit]] = js.undefined,
            onExit: js.UndefOr[js.Function0[Unit]] = js.undefined): Unit = {

    FarjsAppNative.start(showDevTools, onReady, onExit)
  }

}

@js.native
@JSImport("../app/FarjsApp.mjs", JSImport.Default)
object FarjsAppNative extends js.Object {

  def start(showDevTools: Boolean,
            onReady: js.UndefOr[js.Function0[Unit]],
            onExit: js.UndefOr[js.Function0[Unit]]): Unit = js.native
}
