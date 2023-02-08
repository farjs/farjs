package farjs.viewer

import org.scalatest.Succeeded
import scommons.nodejs.Buffer
import scommons.nodejs.raw.FSConstants
import scommons.nodejs.test.AsyncTestSpec

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

class ViewerFileReaderSpec extends AsyncTestSpec {

  //noinspection TypeAnnotation
  class FSMocks {
    val openSync = mockFunction[String, Int, Int]
    val read = mockFunction[Int, Uint8Array, Int, Int, js.UndefOr[Double], Future[Int]]
    val closeSync = mockFunction[Int, Unit]

    val fs = new MockFS(
      openSyncMock = openSync,
      readMock = read,
      closeSyncMock = closeSync
    )
  }
  
  private val fd = 123
  private val bufferSize = 25
  private val maxLineLength = 10
  private val reader = new ViewerFileReader(bufferSize, maxLineLength)
  private val encoding = "utf-8"

  it should "call fs.openSync when open" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val filePath = "test/file"

    //then
    fs.openSync.expects(filePath, FSConstants.O_RDONLY).returning(fd)

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

  it should "log error if failed when readPrevLines" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val position: js.UndefOr[Double] = 9.0

    val errorLogger = mockFunction[String, Unit]
    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    val ex = new Exception("test error")
    val lines = 1

    //then
    fs.read.expects(fd, *, 0, 9, 0: js.UndefOr[Double]).returning(Future.failed(ex))
    errorLogger.expects(s"Failed to read from file, error: $ex")

    //when
    val resultF = reader.readPrevLines(lines, position.get, encoding)
    
    //then
    resultF.failed.map { resEx =>
      resEx shouldBe ex
    }.andThen { _ =>
      js.Dynamic.global.console.error = savedConsoleError
    }
  }

  it should "do nothing if position=0 when readPrevLines" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val position: js.UndefOr[Double] = 0.0
    val lines = 2

    //then
    fs.read.expects(*, *, *, *, *).never()

    //when
    val resultF = reader.readPrevLines(lines, position.get, encoding)
    
    //then
    resultF.map { linesData =>
      linesData shouldBe Nil
    }
  }

  it should "call fs.read when readPrevLines" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val position: js.UndefOr[Double] = 20.0
    val expectedContent = "test file\ncontent\n"
    var bytesWritten = 0
    val lines = 2

    //then
    fs.read.expects(fd, *, 0, *, 0: js.UndefOr[Double]).onCall { (_, buff, _, bufSize, _) =>
      bufSize shouldBe position.get
      bytesWritten =
        buff.asInstanceOf[Buffer].write(expectedContent, 0, expectedContent.length, encoding)
      Future.successful(bytesWritten)
    }

    //when
    val resultF = reader.readPrevLines(lines, position.get, encoding)
    
    //then
    resultF.map { linesData =>
      linesData shouldBe List(
        "test file" -> 10,
        "content" -> 8
      )
    }
  }

  it should "log error if failed when readNextLines" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val position: js.UndefOr[Double] = 2.0

    val errorLogger = mockFunction[String, Unit]
    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = errorLogger

    val ex = new Exception("test error")
    val lines = 1

    //then
    fs.read.expects(fd, *, 0, maxLineLength, position).returning(Future.failed(ex))
    errorLogger.expects(s"Failed to read from file, error: $ex")

    //when
    val resultF = reader.readNextLines(lines, position.get, encoding)
    
    //then
    resultF.failed.map { resEx =>
      resEx shouldBe ex
    }.andThen { _ =>
      js.Dynamic.global.console.error = savedConsoleError
    }
  }

  it should "call fs.read when readNextLines" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val position: js.UndefOr[Double] = 12.0
    val expectedContent = "test file\ncontent\n"
    var bytesWritten = 0
    val lines = 2

    //then
    fs.read.expects(fd, *, 0, *, position).onCall { (_, buff, _, bufSize, _) =>
      bufSize shouldBe bufferSize
      bytesWritten =
        buff.asInstanceOf[Buffer].write(expectedContent, 0, expectedContent.length, encoding)
      Future.successful(bytesWritten)
    }

    //when
    val resultF = reader.readNextLines(lines, position.get, encoding)
    
    //then
    resultF.map { linesData =>
      linesData shouldBe List(
        "test file" -> 10,
        "content" -> 8
      )
    }
  }
}
