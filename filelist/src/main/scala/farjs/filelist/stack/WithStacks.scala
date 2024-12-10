package farjs.filelist.stack

import scommons.react._
import scommons.react.raw.NativeContext

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@farjs/filelist/stack/WithStacks.mjs", JSImport.Default)
object WithStacks extends ReactClass {

  val Context: NativeContext = js.native
  
  def useStacks(): WithStacksProps = js.native
}
