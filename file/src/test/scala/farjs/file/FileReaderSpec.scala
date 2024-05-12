package farjs.file

import scommons.nodejs.Buffer
import scommons.nodejs.raw.FSConstants
import scommons.nodejs.test.AsyncTestSpec

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

class FileReaderSpec extends AsyncTestSpec {

  //noinspection TypeAnnotation
  class FSMocks {
    val openSync = mockFunction[String, Int, Int]
    val closeSync = mockFunction[Int, Unit]
    val read = mockFunction[Int, Uint8Array, Int, Int, js.UndefOr[Double], Future[Int]]

    val fs = new MockFS(
      openSyncMock = openSync,
      closeSyncMock = closeSync,
      readMock = read
    )
  }
  
  private val fd = 123
  private val fileBuf = Buffer.allocUnsafe(64)

  it should "call fs.openSync when open" in {
    //given
    val fs = new FSMocks
    val reader = new FileReader(fs.fs)
    val filePath = "test/file"

    //then
    fs.openSync.expects(filePath, FSConstants.O_RDONLY).returning(fd)

    //when
    val resultF = reader.open(filePath)
    
    //then
    resultF.map { _ =>
      reader.fd shouldBe fd
    }
  }

  it should "recover and log error if failed when close" in {
    //given
    val fs = new FSMocks
    val reader = new FileReader(fs.fs)
    reader.fd = fd

    val errorLogger = mockFunction[String, Unit]
    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    val ex = new Exception("test error")
    var capturedError = ""

    //then
    fs.closeSync.expects(fd).throws(ex)
    errorLogger.expects(*).onCall { error: String =>
      capturedError = error
    }

    //when
    val resultF = reader.close()
    
    //then
    resultF.map { _ =>
      capturedError shouldBe s"Failed to close file, error: $ex"
    }.andThen { _ =>
      js.Dynamic.global.console.error = savedConsoleError
    }
  }

  it should "log error if failed when readBytes" in {
    //given
    val fs = new FSMocks
    val reader = new FileReader(fs.fs)
    reader.fd = fd
    val position = 9.0
    val size = 5
    val buf = fileBuf.subarray(0, size)

    val errorLogger = mockFunction[String, Unit]
    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    val ex = new Exception("test error")

    //then
    fs.read.expects(fd, buf, 0, size, position: js.UndefOr[Double]).returning(Future.failed(ex))
    errorLogger.expects(s"Failed to read from file, error: $ex")

    //when
    val resultF = reader.readBytes(position, buf)
    
    //then
    resultF.failed.map { resEx =>
      resEx shouldBe ex
    }.andThen { _ =>
      js.Dynamic.global.console.error = savedConsoleError
    }
  }

  it should "read data when readBytes" in {
    //given
    val fs = new FSMocks
    val reader = new FileReader(fs.fs)
    reader.fd = fd
    val position = 9.0
    val size = 5
    val buf = fileBuf.subarray(0, size)

    //then
    fs.read.expects(fd, buf, 0, size, position: js.UndefOr[Double]).onCall { (_, buf, _, _, _) =>
      buf.length shouldBe size
      Future.successful(4)
    }

    //when
    val resultF = reader.readBytes(position, buf)
    
    //then
    resultF.map { bytesRead =>
      bytesRead shouldBe 4
    }
  }
}
