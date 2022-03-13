package farjs.app.filelist.zip

import scommons.nodejs.test.AsyncTestSpec

import scala.concurrent.Future

class ZipActionsSpec extends AsyncTestSpec {

  it should "return None when getDriveRoot" in {
    //given
    val api = new ZipApi("file.zip", "root.path", Future.successful(Nil))
    val actions = new ZipActions(api)
    
    //when
    val resultF = actions.getDriveRoot("path")

    //then
    resultF.map { res =>
      res shouldBe None
    }
  }
}
