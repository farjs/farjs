package farjs.app.filelist.zip

import scommons.nodejs.test.TestSpec

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

class ZipPluginSpec extends TestSpec {

  ZipPlugin.readZip = _ => Future.successful(Map.empty)

  it should "trigger plugin on .zip and .jar file extensions" in {
    //given
    val header = new Uint8Array(5)
    
    //when & then
    ZipPlugin.onFileTrigger("filePath.txt", header, () => ()) shouldBe None
    ZipPlugin.onFileTrigger("filePath.zip", header, () => ()) should not be None
    ZipPlugin.onFileTrigger("filePath.ZIP", header, () => ()) should not be None
    ZipPlugin.onFileTrigger("filePath.jar", header, () => ()) should not be None
    ZipPlugin.onFileTrigger("filePath.Jar", header, () => ()) should not be None
  }

  it should "trigger plugin on PK34 file header" in {
    //given
    val header = new Uint8Array(js.Array[Short]('P', 'K', 0x03, 0x04, 0x01))
    
    //when & then
    ZipPlugin.onFileTrigger("filePath.txt", new Uint8Array(2), () => ()) shouldBe None
    ZipPlugin.onFileTrigger("filePath.txt", header, () => ()) should not be None
  }
}
