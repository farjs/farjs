package farjs.app

import farjs.app.FarjsData.appName
import scommons.nodejs.Process.Platform
import scommons.nodejs.raw.NodeJs.{process => nodeProc}
import scommons.nodejs.{path => nodePath, _}

import scala.scalajs.js

class FarjsData(platform: Platform) {

  def getDBFilePath: String = nodePath.join((getDataDir :+ "farjs.db"): _*)

  def getDataDir: List[String] = {
    val home = os.homedir()

    if (platform == Platform.darwin) {
      List(home, "Library", "Application Support", appName)
    }
    else if (platform == Platform.win32) {
      val appData = nodeProc.asInstanceOf[js.Dynamic].env.APPDATA.asInstanceOf[js.UndefOr[String]]
      appData.toOption match {
        case Some(dataDir) => List(dataDir, appName)
        case None => List(home, s".$appName")
      }
    }
    else List(home, ".local", "share", appName)
  }
}

object FarjsData extends FarjsData(process.platform) {

  private val appName = "FAR.js"
}
