package farjs.app.filelist.zip

import farjs.app.filelist.fs.MockChildProcess
import scommons.nodejs.test.TestSpec

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

class ZipPluginSpec extends TestSpec {

  private val plugin = new ZipPlugin(new MockChildProcess, (_, _) => Future.successful(Map.empty))

  it should "trigger plugin on .zip and .jar file extensions" in {
    //given
    val header = new Uint8Array(5)
    
    //when & then
    plugin.onFileTrigger("filePath.txt", header, () => ()) shouldBe None
    plugin.onFileTrigger("filePath.zip", header, () => ()) should not be None
    plugin.onFileTrigger("filePath.ZIP", header, () => ()) should not be None
    plugin.onFileTrigger("filePath.jar", header, () => ()) should not be None
    plugin.onFileTrigger("filePath.Jar", header, () => ()) should not be None
  }

  it should "trigger plugin on PK34 file header" in {
    //given
    val header = new Uint8Array(js.Array[Short]('P', 'K', 0x03, 0x04, 0x01))
    
    //when & then
    plugin.onFileTrigger("filePath.txt", new Uint8Array(2), () => ()) shouldBe None
    plugin.onFileTrigger("filePath.txt", header, () => ()) should not be None
  }
}
