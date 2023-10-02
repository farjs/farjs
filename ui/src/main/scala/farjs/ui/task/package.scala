package farjs.ui

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object task {

  @js.native
  @JSImport("@farjs/ui/task/TaskManagerUi.mjs", JSImport.Default)
  object TaskManagerUi extends ReactClass

}
