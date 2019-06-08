package scommons.nodejs

import scala.concurrent.{Future, Promise}
import scala.scalajs.js

sealed trait FS {

  def readdir(path: URL): Future[Seq[String]] = {
    val p = Promise[Seq[String]]()
    raw.FS.readdir(path, { (error, files) =>
      if (error != null && !js.isUndefined(error)) p.failure(js.JavaScriptException(error))
      else p.success(files)
    })
    p.future
  }

  def lstatSync(path: URL): Stats = raw.FS.lstatSync(path)
}

object FS extends FS
