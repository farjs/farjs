package farjs.viewer

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../viewer/ViewerFileViewport.mjs", "createViewerFileViewport")
object ViewerFileViewportNative extends js.Function9[ViewerFileReader, String, Double, Int, Int, Boolean, Int, Double, js.Array[ViewerFileLine], ViewerFileViewport] {

  def apply(fileReader: ViewerFileReader,
            encoding: String,
            size: Double,
            width: Int,
            height: Int,
            wrap: Boolean,
            column: Int,
            position: Double,
            linesData: js.Array[ViewerFileLine]): ViewerFileViewport = js.native
}

object ViewerFileViewport {

  def apply(fileReader: ViewerFileReader,
            encoding: String,
            size: Double,
            width: Int,
            height: Int,
            wrap: Boolean = false,
            column: Int = 0,
            position: Double = 0.0,
            linesData: js.Array[ViewerFileLine] = new js.Array()): ViewerFileViewport = {

    ViewerFileViewportNative(
      fileReader,
      encoding,
      size,
      width,
      height,
      wrap,
      column,
      position,
      linesData
    )
  }

  def unapply(arg: ViewerFileViewport): Option[(ViewerFileReader, String, Double, Int, Int, Boolean, Int, Double, js.Array[ViewerFileLine])] = {
    Some((
      arg.fileReader,
      arg.encoding,
      arg.size,
      arg.width,
      arg.height,
      arg.wrap,
      arg.column,
      arg.position,
      arg.linesData
    ))
  }
}

trait ViewerFileViewportData extends js.Object {

  val encoding: js.UndefOr[String] = js.undefined
  val size: js.UndefOr[Double] = js.undefined
  val width: js.UndefOr[Int] = js.undefined
  val height: js.UndefOr[Int] = js.undefined
  val wrap: js.UndefOr[Boolean] = js.undefined
  val column: js.UndefOr[Int] = js.undefined
  val position: js.UndefOr[Double] = js.undefined
  val linesData: js.UndefOr[js.Array[ViewerFileLine]] = js.undefined
}

@js.native
sealed trait ViewerFileViewport extends js.Object {

  val fileReader: ViewerFileReader = js.native
  val encoding: String = js.native
  val size: Double = js.native
  val width: Int = js.native
  val height: Int = js.native
  val wrap: Boolean = js.native
  val column: Int = js.native
  val position: Double = js.native
  val linesData: js.Array[ViewerFileLine] = js.native

  val content: String = js.native
  val scrollIndicators: js.Array[Int] = js.native
  val progress: Int = js.native
  
  def moveUp(lines: Int, from: Double = position): js.Promise[ViewerFileViewport] = js.native

  def moveDown(lines: Int): js.Promise[ViewerFileViewport] = js.native

  def reload(from: Double = position): js.Promise[ViewerFileViewport] = js.native
  
  def updated(data: ViewerFileViewportData): ViewerFileViewport = js.native
}
