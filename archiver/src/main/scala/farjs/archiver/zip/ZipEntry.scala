package farjs.archiver.zip

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

case class ZipEntry(
  parent: String,
  name: String,
  isDir: Boolean = false,
  size: Double = 0.0,
  datetimeMs: Double = 0.0,
  permissions: String = ""
)

object ZipEntry {

  def fromUnzipCommand(output: String): js.Array[ZipEntry] = {
    ZipEntryNative.fromUnzipCommand(output).map { entry =>
      ZipEntry(
        parent = entry.parent,
        name = entry.name,
        isDir = entry.isDir,
        size = entry.size,
        datetimeMs = entry.datetimeMs,
        permissions = entry.permissions
      )
    }
  }
}

sealed trait ZipEntryNative extends js.Object {
  val parent: String
  val name: String
  val isDir: Boolean
  val size: Double
  val datetimeMs: Double
  val permissions: String
}

@js.native
@JSImport("../archiver/zip/ZipEntry.mjs", JSImport.Default)
object ZipEntryNative extends js.Function6[String, String, js.UndefOr[Boolean], js.UndefOr[Double], js.UndefOr[Double], js.UndefOr[String], ZipEntryNative] {

  def apply(parent: String,
            name: String,
            isDir: js.UndefOr[Boolean] = js.native,
            size: js.UndefOr[Double] = js.native,
            datetimeMs: js.UndefOr[Double] = js.native,
            permissions: js.UndefOr[String] = js.native
           ): ZipEntryNative = js.native

  def fromUnzipCommand(output: String): js.Array[ZipEntryNative] = js.native
}
