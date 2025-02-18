package farjs

import farjs.filelist.FileListPlugin

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object file {

  @js.native
  @JSImport("../file/FilePlugin.mjs", JSImport.Default)
  object FilePlugin extends FileListPlugin(js.native)
}
