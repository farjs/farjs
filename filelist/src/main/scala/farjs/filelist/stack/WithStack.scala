package farjs.filelist.stack

import scommons.react._
import scommons.react.raw.NativeContext

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@farjs/filelist/stack/WithStack.mjs", JSImport.Default)
object WithStack extends ReactClass {

  val Context: NativeContext = js.native

  def useStack(): WithStackProps = js.native
}
