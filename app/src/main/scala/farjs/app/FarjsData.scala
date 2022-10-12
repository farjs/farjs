package farjs.app

import farjs.app.FarjsData.appName
import scommons.nodejs.Process.Platform
import scommons.nodejs.raw.NodeJs.{process => nodeProc}
import scommons.nodejs.{path => nodePath, _}

import scala.scalajs.js

class FarjsData(platform: Platform) {

  def getDBFilePath: String = nodePath.join(getDataDir, "farjs.db")

  private def getDataDir: String = {
    val home = os.homedir()

    if (platform == Platform.darwin) {
      nodePath.join(home, "Library", "Application Support", appName)
    }
    else if (platform == Platform.win32) {
      val appData = nodeProc.asInstanceOf[js.Dynamic].env.APPDATA.asInstanceOf[js.UndefOr[String]]
      appData.toOption match {
        case Some(dataDir) => nodePath.join(dataDir, appName)
        case None => nodePath.join(home, s".$appName")
      }
    }
    else {
      nodePath.join(home, ".local", "share", appName)
    }
  }
}

object FarjsData {

  private val appName = "FAR.js"

  lazy val instance: FarjsData = new FarjsData(process.platform)
}
