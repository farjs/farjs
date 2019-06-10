package scommons.farc.app.filelist

import scommons.nodejs._
import scommons.nodejs.test.AsyncTestSpec

class FileListApiImplSpec extends AsyncTestSpec {
  
  private val apiImp = new FileListApiImpl
  
  it should "return file list when listFiles()" in {
    //given
    val dir = os.homedir()
    
    //when & then
    apiImp.listFiles(dir).map { files =>
      files should not be empty
    }
  }
}
