package farjs.fs

import farjs.filelist.api.MockFileListApi
import scommons.nodejs.test.AsyncTestSpec

import scala.concurrent.Future

class FSFileListActionsSpec extends AsyncTestSpec {

  //noinspection TypeAnnotation
  class FsService {
    val readDisk = mockFunction[String, Future[Option[FSDisk]]]

    val fsService = new MockFSService(
      readDiskMock = readDisk
    )
  }
  
  it should "call fsService.readDisk when getDriveRoot" in {
    //given
    val api = MockFileListApi()
    val fsService = new FsService
    val actions = new FSFileListActions(api, fsService.fsService)
    val path = "test path"
    val drive = FSDisk("/some/path", 0, 0, "SomeDrive")
    
    //then
    fsService.readDisk.expects(path).returning(Future.successful(Some(drive)))

    //when
    val resultF = actions.getDriveRoot(path)
    
    //then
    resultF.map { result =>
      result shouldBe Some(drive.root)
    }
  }
}
