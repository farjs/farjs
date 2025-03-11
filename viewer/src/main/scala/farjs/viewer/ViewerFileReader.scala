package farjs.viewer

import farjs.file.FileReader

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../viewer/ViewerFileReader.mjs", JSImport.Default)
class ViewerFileReader(fileReader: FileReader,
                       bufferSize: Int = js.native,
                       maxLineLength: Int = js.native) extends js.Object {
  
  def open(filePath: String): js.Promise[Unit] = js.native

  def close(): js.Promise[Unit] = js.native

  def readPrevLines(lines: Int, position: Double, maxPos: Double, encoding: String): js.Promise[js.Array[ViewerFileLine]] = js.native
  
  def readNextLines(lines: Int, position: Double, encoding: String): js.Promise[js.Array[ViewerFileLine]] = js.native
}
