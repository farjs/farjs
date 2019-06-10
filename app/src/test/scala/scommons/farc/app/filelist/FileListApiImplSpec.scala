package scommons.farc.app.filelist

import org.scalatest.{AsyncFlatSpec, Matchers}
import scommons.nodejs._

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext

class FileListApiImplSpec extends AsyncFlatSpec with Matchers {

  implicit override val executionContext: ExecutionContext = JSExecutionContext.queue
  
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
