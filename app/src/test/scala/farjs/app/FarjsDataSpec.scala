package farjs.app

import scommons.nodejs.Process.Platform
import scommons.nodejs.raw.NodeJs.{process => nodeProc}
import scommons.nodejs.test._
import scommons.nodejs.{path => nodePath, _}

import scala.scalajs.js

class FarjsDataSpec extends TestSpec {

  it should "return DB file path on Mac OS" in {
    //given
    val data = new FarjsData(Platform.darwin)
    
    //when & then
    data.getDBFilePath shouldBe {
      nodePath.join(os.homedir(), "Library", "Application Support", "FAR.js", "farjs.db")
    }
  }

  it should "return DB file path on Windows when APPDATA is set" in {
    //given
    val data = new FarjsData(Platform.win32)
    val dataDir = "test"
    nodeProc.asInstanceOf[js.Dynamic].env.APPDATA = dataDir
    
    //when & then
    data.getDBFilePath shouldBe {
      nodePath.join(dataDir, "FAR.js", "farjs.db")
    }
  }

  it should "return DB file path on Windows when APPDATA is not set" in {
    //given
    val data = new FarjsData(Platform.win32)
    nodeProc.asInstanceOf[js.Dynamic].env.asInstanceOf[js.Dictionary[String]] -= "APPDATA"
    
    //when & then
    data.getDBFilePath shouldBe {
      nodePath.join(os.homedir(), ".FAR.js", "farjs.db")
    }
  }

  it should "return DB file path on Linux" in {
    //given
    val data = new FarjsData(Platform.linux)

    //when & then
    data.getDBFilePath shouldBe {
      nodePath.join(os.homedir(), ".local", "share", "FAR.js", "farjs.db")
    }
  }
}
