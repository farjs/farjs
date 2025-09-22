package farjs

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object fs {

  @js.native
  @JSImport("../fs/FSFoldersHistory.mjs", JSImport.Default)
  object FSFoldersHistory extends ReactClass

  @js.native
  @JSImport("../fs/FSFreeSpace.mjs", JSImport.Default)
  object FSFreeSpace extends ReactClass
}
