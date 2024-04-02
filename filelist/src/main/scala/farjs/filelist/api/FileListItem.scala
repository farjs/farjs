package farjs.filelist.api

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
sealed trait FileListItem extends js.Object {

  val name: String = js.native
  val isDir: Boolean = js.native
  val isSymLink: Boolean = js.native
  val size: Double = js.native
  val atimeMs: Double = js.native
  val mtimeMs: Double = js.native
  val ctimeMs: Double = js.native
  val birthtimeMs: Double = js.native
  val permissions: String = js.native //optional, format: drwx---rwx
  
  def nameNormalized(): String = js.native
  def ext(): String = js.native
  def extNormalized(): String = js.native
}

@js.native
@JSImport("@farjs/filelist/api/FileListItem.mjs", JSImport.Default)
object NativeFileListItem extends js.Function2[String, js.UndefOr[Boolean], FileListItem] {

  val up: FileListItem = js.native
  val currDir: FileListItem = js.native

  def apply(name: String, isDir: js.UndefOr[Boolean]): FileListItem = js.native
}

object FileListItem {

  val up: FileListItem = NativeFileListItem.up
  val currDir: FileListItem = NativeFileListItem.currDir

  def apply(name: String): FileListItem = NativeFileListItem(name, js.undefined)
  
  def apply(name: String, isDir: Boolean): FileListItem = NativeFileListItem(name, isDir)

  def unapply(arg: FileListItem): Option[(String, Boolean, Boolean, Double, Double, Double, Double, Double, String)] = {
    Some((
      arg.name,
      arg.isDir,
      arg.isSymLink,
      arg.size,
      arg.atimeMs,
      arg.mtimeMs,
      arg.ctimeMs,
      arg.birthtimeMs,
      arg.permissions
    ))
  }

  def copy(p: FileListItem)(name: String = p.name,
                            isDir: Boolean = p.isDir,
                            isSymLink: Boolean = p.isSymLink,
                            size: Double = p.size,
                            atimeMs: Double = p.atimeMs,
                            mtimeMs: Double = p.mtimeMs,
                            ctimeMs: Double = p.ctimeMs,
                            birthtimeMs: Double = p.birthtimeMs,
                            permissions: String = p.permissions): FileListItem = {

    val res = NativeFileListItem(name, isDir)
    val dynRes = res.asInstanceOf[js.Dynamic]
    dynRes.isSymLink = isSymLink
    dynRes.size = size
    dynRes.atimeMs = atimeMs
    dynRes.mtimeMs = mtimeMs
    dynRes.ctimeMs = ctimeMs
    dynRes.birthtimeMs = birthtimeMs
    dynRes.permissions = permissions
    res
  }
}
