package farjs.archiver.zip

import farjs.filelist.api.FileListItem

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
sealed trait ZipEntry extends FileListItem {
  val parent: String
}

@js.native
@JSImport("../archiver/zip/ZipEntry.mjs", JSImport.Default)
object ZipEntry extends js.Function6[String, String, js.UndefOr[Boolean], js.UndefOr[Double], js.UndefOr[Double], js.UndefOr[String], ZipEntry] {

  def apply(parent: String,
            name: String,
            isDir: js.UndefOr[Boolean] = js.native,
            size: js.UndefOr[Double] = js.native,
            datetimeMs: js.UndefOr[Double] = js.native,
            permissions: js.UndefOr[String] = js.native
           ): ZipEntry = js.native

  def fromUnzipCommand(output: String): js.Array[ZipEntry] = js.native

  def groupByParent(entries: js.Array[ZipEntry]): js.Map[String, js.Array[FileListItem]] = js.native
}
