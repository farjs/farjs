package farjs.app.filelist.zip

import farjs.app.filelist.fs.MockChildProcess
import scommons.nodejs.test.TestSpec

import scala.concurrent.Future

class ZipPluginSpec extends TestSpec {

  it should "trigger plugin on .zip and .jar files" in {
    //given
    val plugin = new ZipPlugin(new MockChildProcess, (_, _) => Future.successful(Map.empty))
    
    //when & then
    plugin.onFileTrigger("filePath.txt", () => ()) shouldBe None
    plugin.onFileTrigger("filePath.zip", () => ()) should not be None
    plugin.onFileTrigger("filePath.ZIP", () => ()) should not be None
    plugin.onFileTrigger("filePath.jar", () => ()) should not be None
    plugin.onFileTrigger("filePath.Jar", () => ()) should not be None
  }
}
