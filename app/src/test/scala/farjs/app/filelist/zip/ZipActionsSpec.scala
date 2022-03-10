package farjs.app.filelist.zip

import scommons.nodejs.test.AsyncTestSpec

class ZipActionsSpec extends AsyncTestSpec {

  it should "return None when getDriveRoot" in {
    //given
    val actions = new ZipActions("filePath.zip")
    
    //when
    val resultF = actions.getDriveRoot("path")

    //then
    resultF.map { res =>
      res shouldBe None
    }
  }
}
