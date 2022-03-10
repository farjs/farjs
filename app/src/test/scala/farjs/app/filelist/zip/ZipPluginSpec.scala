package farjs.app.filelist.zip

import scommons.nodejs.test.TestSpec

class ZipPluginSpec extends TestSpec {

  it should "trigger plugin on .zip and .jar files" in {
    //when & then
    ZipPlugin.onFileTrigger("filePath.txt", () => ()) shouldBe None
    ZipPlugin.onFileTrigger("filePath.zip", () => ()) should not be None
    ZipPlugin.onFileTrigger("filePath.ZIP", () => ()) should not be None
    ZipPlugin.onFileTrigger("filePath.jar", () => ()) should not be None
    ZipPlugin.onFileTrigger("filePath.Jar", () => ()) should not be None
  }
}
