package farjs.app.filelist.zip

import farjs.filelist.api.{FileListDir, FileListItem}
import scommons.nodejs.test.AsyncTestSpec

class ZipApiSpec extends AsyncTestSpec {

  it should "return root dir content when readDir(.)" in {
    //given
    val filePath = "filePath.zip"
    val api = new ZipApi(filePath)
    
    //when
    val resultF = api.readDir(None, FileListItem.currDir.name)

    //then
    resultF.map(inside(_) { case FileListDir(path, isRoot, items) =>
      path shouldBe s"ZIP://$filePath"
      isRoot shouldBe false
      items.exists(_.name == "dir 1") shouldBe true
    })
  }

  it should "return root dir content when readDir(..)" in {
    //given
    val filePath = "filePath.zip"
    val api = new ZipApi(filePath)
    
    //when
    val resultF = api.readDir(Some(s"ZIP://$filePath/dir 1"), FileListItem.up.name)

    //then
    resultF.map(inside(_) { case FileListDir(path, isRoot, items) =>
      path shouldBe s"ZIP://$filePath"
      isRoot shouldBe false
      items.exists(_.name == "dir 1") shouldBe true
    })
  }

  it should "return sub-dir content when readDir" in {
    //given
    val filePath = "filePath.zip"
    val api = new ZipApi(filePath)
    
    //when
    val resultF = api.readDir(Some(s"ZIP://$filePath"), "dir 1")

    //then
    resultF.map(inside(_) { case FileListDir(path, isRoot, items) =>
      path shouldBe s"ZIP://$filePath/dir 1"
      isRoot shouldBe false
      items.exists(_.name == "file 2") shouldBe true
    })
  }
}
