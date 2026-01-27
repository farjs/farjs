package farjs.filelist.util

import scommons.nodejs.Buffer
import scommons.nodejs.raw.Readable

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@farjs/filelist/util/StreamReader.mjs", JSImport.Default)
class StreamReader(val readable: Readable) extends js.Object {

  def readNextBytes(size: Int): js.Promise[js.UndefOr[Buffer]] = js.native

  def readAllLines(onNextLine: js.Function1[String, Unit]): js.Promise[Unit] = js.native
}
