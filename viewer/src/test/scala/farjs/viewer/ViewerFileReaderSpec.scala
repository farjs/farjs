package farjs.viewer

import org.scalatest.Succeeded
import scommons.nodejs.raw.FSConstants
import scommons.nodejs.test.AsyncTestSpec
import scommons.nodejs.{Buffer, FS}

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

class ViewerFileReaderSpec extends AsyncTestSpec {

  //noinspection TypeAnnotation
  class FSMocks {
    val openSyncMock = mockFunction[String, Int, Int]
    val readMock = mockFunction[Int, Uint8Array, Int, Int, js.UndefOr[Double], Future[Int]]
    val closeSyncMock = mockFunction[Int, Unit]

    val fs = new FS {
      override def openSync(path: String, flags: Int): Int = openSyncMock(path, flags)
      override def read(fd: Int,
                        buffer: Uint8Array,
                        offset: Int,
                        length: Int,
                        position: js.UndefOr[Double] = js.undefined): Future[Int] = {

        readMock(fd, buffer, offset, length, position)
      }
      override def closeSync(fd: Int): Unit = closeSyncMock(fd)
    }
  }
  
  private val fd = 123
  private val reader = new ViewerFileReader

  it should "call fs.openSync when open" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val filePath = "test/file"

    //then
    fs.openSyncMock.expects(filePath, FSConstants.O_RDONLY).returning(fd)

    //when
    val resultF = reader.open(filePath)
    
    //then
    resultF.map(_ => Succeeded)
  }

  it should "recover and log error if failed when close" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs

    val errorLogger = mockFunction[String, Unit]
    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    val ex = new Exception("test error")
    var capturedError = ""

    //then
    fs.closeSyncMock.expects(fd).throws(ex)
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

  it should "log error if failed when readPageAt" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val position: js.UndefOr[Double] = 2.0

    val errorLogger = mockFunction[String, Unit]
    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    val ex = new Exception("test error")

    //then
    fs.readMock.expects(fd, *, 0, 64 * 1024, position).returning(Future.failed(ex))
    errorLogger.expects(s"Failed to read from file, error: $ex")

    //when
    val resultF = reader.readPageAt(position.get, "utf-8")
    
    //then
    resultF.failed.map { resEx =>
      resEx shouldBe ex
    }.andThen { _ =>
      js.Dynamic.global.console.error = savedConsoleError
    }
  }

  it should "call fs.read when readPageAt" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val position: js.UndefOr[Double] = 12345.0
    val encoding = "utf-8"
    val expectedContent = "test file\ncontent\n"
    var bytesWritten = 0

    //then
    fs.readMock.expects(fd, *, 0, 64 * 1024, position).onCall { (_, buff, _, _, _) =>
      bytesWritten =
        buff.asInstanceOf[Buffer].write(expectedContent, 0, expectedContent.length, encoding)
      Future.successful(bytesWritten)
    }

    //when
    val resultF = reader.readPageAt(position.get, encoding)
    
    //then
    resultF.map { case (page, pageBytes) =>
      page shouldBe expectedContent
      pageBytes shouldBe bytesWritten
    }
  }
}
