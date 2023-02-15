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
  private val bufferSize = 15
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
    val resultF = reader.readPrevLines(lines, position.get, maxPos = 20, encoding = encoding)
    
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
    val resultF = reader.readPrevLines(lines, position.get, maxPos = 20, encoding)
    
    //then
    resultF.map { linesData =>
      linesData shouldBe Nil
    }
  }

  it should "read file content with new line at the end when readPrevLines" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val position = 18
    val lines = 3

    //then
    fs.read.expects(fd, *, 0, *, *).onCall { (_, buff, _, bufSize, position) =>
      bufSize shouldBe bufferSize
      position shouldBe 3
      val content = "t file\ncontent\n"
      Future.successful(buff.asInstanceOf[Buffer].write(content, 0, content.length, encoding))
    }
    fs.read.expects(fd, *, 0, *, *).onCall { (_, buff, _, bufSize, position) =>
      bufSize shouldBe 3
      position shouldBe 0
      val content = "tes"
      Future.successful(buff.asInstanceOf[Buffer].write(content, 0, content.length, encoding))
    }

    //when
    val resultF = reader.readPrevLines(lines, position, maxPos = 18, encoding)
    
    //then
    resultF.map { linesData =>
      linesData shouldBe List(
        "test file" -> 10,
        "content" -> 8,
        "" -> 0
      )
    }
  }

  it should "read file content without new line at the end when readPrevLines" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val position = 18
    val lines = 3

    //then
    fs.read.expects(fd, *, 0, *, *).onCall { (_, buff, _, bufSize, position) =>
      bufSize shouldBe bufferSize
      position shouldBe 3
      val content = "st file\ncontent"
      Future.successful(buff.asInstanceOf[Buffer].write(content, 0, content.length, encoding))
    }
    fs.read.expects(fd, *, 0, *, *).onCall { (_, buff, _, bufSize, position) =>
      bufSize shouldBe 3
      position shouldBe 0
      val content = "\nte"
      Future.successful(buff.asInstanceOf[Buffer].write(content, 0, content.length, encoding))
    }

    //when
    val resultF = reader.readPrevLines(lines, position, maxPos = 18, encoding)
    
    //then
    resultF.map { linesData =>
      linesData shouldBe List(
        "" -> 1,
        "test file" -> 10,
        "content" -> 7
      )
    }
  }

  it should "read single empty line at the start when readPrevLines" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val position = 1
    val lines = 2

    //then
    fs.read.expects(fd, *, 0, *, *).onCall { (_, buff, _, bufSize, position) =>
      bufSize shouldBe 1
      position shouldBe 0
      val content = "\n"
      Future.successful(buff.asInstanceOf[Buffer].write(content, 0, content.length, encoding))
    }

    //when
    val resultF = reader.readPrevLines(lines, position, maxPos = 18, encoding)
    
    //then
    resultF.map { linesData =>
      linesData shouldBe List(
        "" -> 1
      )
    }
  }

  it should "read long lines when readPrevLines" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val position = 15
    val lines = 3

    //then
    fs.read.expects(fd, *, 0, *, *).onCall { (_, buff, _, bufSize, position) =>
      bufSize shouldBe bufferSize
      position shouldBe 0
      val content = "testfilecontent"
      Future.successful(buff.asInstanceOf[Buffer].write(content, 0, content.length, encoding))
    }

    //when
    val resultF = reader.readPrevLines(lines, position, maxPos = 15, encoding)
    
    //then
    resultF.map { linesData =>
      linesData shouldBe List(
        "testf" -> 5,
        "ilecontent" -> 10
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

  it should "read file content when readNextLines" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val lines = 3

    //then
    fs.read.expects(fd, *, 0, *, *).onCall { (_, buff, _, bufSize, position) =>
      bufSize shouldBe bufferSize
      position shouldBe 0
      val content = "\ntest file\ncon"
      Future.successful(buff.asInstanceOf[Buffer].write(content, 0, content.length, encoding))
    }
    fs.read.expects(fd, *, 0, *, *).onCall { (_, buff, _, bufSize, position) =>
      bufSize shouldBe bufferSize
      position shouldBe 14
      val content = "tent\n"
      Future.successful(buff.asInstanceOf[Buffer].write(content, 0, content.length, encoding))
    }

    //when
    val resultF = reader.readNextLines(lines, 0, encoding)
    
    //then
    resultF.map { linesData =>
      linesData shouldBe List(
        "" -> 1,
        "test file" -> 10,
        "content" -> 8
      )
    }
  }

  it should "read long lines when readNextLines" in {
    //given
    val fs = new FSMocks
    ViewerFileReader.fs = fs.fs
    val lines = 3

    //then
    fs.read.expects(fd, *, 0, *, *).onCall { (_, buff, _, bufSize, position) =>
      bufSize shouldBe bufferSize
      position shouldBe 0
      val content = "testfilecontent"
      Future.successful(buff.asInstanceOf[Buffer].write(content, 0, content.length, encoding))
    }
    fs.read.expects(fd, *, 0, *, *).onCall { (_, buff, _, bufSize, position) =>
      bufSize shouldBe bufferSize
      position shouldBe 15
      Future.successful(buff.asInstanceOf[Buffer].write("", 0, 0, encoding))
    }

    //when
    val resultF = reader.readNextLines(lines, 0, encoding)
    
    //then
    resultF.map { linesData =>
      linesData shouldBe List(
        "testfileco" -> 10,
        "ntent" -> 5
      )
    }
  }
}
