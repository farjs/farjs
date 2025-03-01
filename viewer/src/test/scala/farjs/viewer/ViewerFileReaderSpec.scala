package farjs.viewer

import farjs.viewer.ViewerFileReaderSpec.assertViewerFileLines
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.Buffer
import scommons.nodejs.test.AsyncTestSpec

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSConverters.JSRichFutureNonThenable

class ViewerFileReaderSpec extends AsyncTestSpec {

  //noinspection TypeAnnotation
  class FileReader {
    val open = mockFunction[String, js.Promise[Unit]]
    val close = mockFunction[js.Promise[Unit]]
    val readBytes = mockFunction[Double, Buffer, js.Promise[Int]]

    val fileReader = new MockFileReader(
      openMock = open,
      closeMock = close,
      readBytesMock = readBytes
    )
  }
  
  private val bufferSize = 15
  private val maxLineLength = 10
  private val encoding = "utf-8"

  it should "call fileReader.open when open" in {
    //given
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val filePath = "test/filePath.txt"

    //then
    fileReader.open.expects(filePath).returning(js.Promise.resolve[Unit](()))

    //when
    val resultF = reader.open(filePath).toFuture

    //then
    resultF.map { _ =>
      Succeeded
    }
  }

  it should "call fileReader.close when close" in {
    //given
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)

    //then
    fileReader.close.expects().returning(js.Promise.resolve[Unit](()))

    //when
    val resultF = reader.close().toFuture

    //then
    resultF.map { _ =>
      Succeeded
    }
  }

  it should "do nothing if position=0 when readPrevLines" in {
    //given
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val position: js.UndefOr[Double] = 0.0
    val lines = 2

    //then
    fileReader.readBytes.expects(*, *).never()

    //when
    val resultF = reader.readPrevLines(lines, position.get, maxPos = 20, encoding)
    
    //then
    resultF.toFuture.map { linesData =>
      linesData.toList shouldBe Nil
    }
  }

  it should "read file content with new line at the end when readPrevLines" in {
    //given
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val position = 18
    val lines = 3

    //then
    fileReader.readBytes.expects(3, *).onCall { (_, buf) =>
      buf.length shouldBe bufferSize
      writeBuf(buf, "t file\ncontent\n").toJSPromise
    }
    fileReader.readBytes.expects(0, *).onCall { (_, buf) =>
      buf.length shouldBe 3
      writeBuf(buf, "tes").toJSPromise
    }

    //when
    val resultF = reader.readPrevLines(lines, position, maxPos = 18, encoding)
    
    //then
    resultF.toFuture.map { results =>
      assertViewerFileLines(results, List(
        ViewerFileLine("test file", 10),
        ViewerFileLine("content", 8),
        ViewerFileLine("", 0)
      ))
    }
  }

  it should "read file content without new line at the end when readPrevLines" in {
    //given
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val position = 18
    val lines = 3

    //then
    fileReader.readBytes.expects(3, *).onCall { (_, buf) =>
      buf.length shouldBe bufferSize
      writeBuf(buf, "st file\ncontent").toJSPromise
    }
    fileReader.readBytes.expects(0, *).onCall { (_, buf) =>
      buf.length shouldBe 3
      writeBuf(buf, "\nte").toJSPromise
    }

    //when
    val resultF = reader.readPrevLines(lines, position, maxPos = 18, encoding)
    
    //then
    resultF.toFuture.map { results =>
      assertViewerFileLines(results, List(
        ViewerFileLine("", 1),
        ViewerFileLine("test file", 10),
        ViewerFileLine("content", 7)
      ))
    }
  }

  it should "read content from the middle of file when readPrevLines" in {
    //given
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val position = 9
    val lines = 3

    //then
    fileReader.readBytes.expects(0, *).onCall { (_, buf) =>
      buf.length shouldBe 9
      writeBuf(buf, "test\nfile").toJSPromise
    }

    //when
    val resultF = reader.readPrevLines(lines, position, maxPos = 25, encoding)
    
    //then
    resultF.toFuture.map { results =>
      assertViewerFileLines(results, List(
        ViewerFileLine("test", 5),
        ViewerFileLine("file", 4)
      ))
    }
  }

  it should "read single empty line at the start when readPrevLines" in {
    //given
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val position = 1
    val lines = 2

    //then
    fileReader.readBytes.expects(0, *).onCall { (_, buf) =>
      buf.length shouldBe 1
      writeBuf(buf, "\n").toJSPromise
    }

    //when
    val resultF = reader.readPrevLines(lines, position, maxPos = 18, encoding)
    
    //then
    resultF.toFuture.map { results =>
      assertViewerFileLines(results, List(
        ViewerFileLine("", 1)
      ))
    }
  }

  it should "read long lines when readPrevLines" in {
    //given
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val position = 15
    val lines = 3

    //then
    fileReader.readBytes.expects(0, *).onCall { (_, buf) =>
      buf.length shouldBe bufferSize
      writeBuf(buf, "testfilecontent").toJSPromise
    }

    //when
    val resultF = reader.readPrevLines(lines, position, maxPos = 15, encoding)
    
    //then
    resultF.toFuture.map { results =>
      assertViewerFileLines(results, List(
        ViewerFileLine("testf", 5),
        ViewerFileLine("ilecontent", 10)
      ))
    }
  }

  it should "read file content with new lines when readNextLines" in {
    //given
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val lines = 3

    //then
    fileReader.readBytes.expects(0, *).onCall { (_, buf) =>
      buf.length shouldBe bufferSize
      writeBuf(buf, "\ntest file").toJSPromise
    }
    fileReader.readBytes.expects(10, *).onCall { (_, buf) =>
      buf.length shouldBe bufferSize
      writeBuf(buf, "\ncontent\n").toJSPromise
    }

    //when
    val resultF = reader.readNextLines(lines, 0, encoding)
    
    //then
    resultF.toFuture.map { results =>
      assertViewerFileLines(results, List(
        ViewerFileLine("", 1),
        ViewerFileLine("test file", 10),
        ViewerFileLine("content", 8)
      ))
    }
  }

  it should "read file content without new lines when readNextLines" in {
    //given
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val lines = 3

    //then
    fileReader.readBytes.expects(0, *).onCall { (_, buf) =>
      buf.length shouldBe bufferSize
      writeBuf(buf, "test fi").toJSPromise
    }
    fileReader.readBytes.expects(7, *).onCall { (_, buf) =>
      buf.length shouldBe bufferSize
      writeBuf(buf, "le\ncontent").toJSPromise
    }
    fileReader.readBytes.expects(17, *).onCall { (_, buf) =>
      buf.length shouldBe bufferSize
      writeBuf(buf, "").toJSPromise
    }

    //when
    val resultF = reader.readNextLines(lines, 0, encoding)
    
    //then
    resultF.toFuture.map { results =>
      assertViewerFileLines(results, List(
        ViewerFileLine("test file", 10),
        ViewerFileLine("content", 7)
      ))
    }
  }

  it should "read long lines when readNextLines" in {
    //given
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val lines = 3

    //then
    fileReader.readBytes.expects(0, *).onCall { (_, buf) =>
      buf.length shouldBe bufferSize
      writeBuf(buf, "testfilecontent").toJSPromise
    }
    fileReader.readBytes.expects(15, *).onCall { (_, buf) =>
      buf.length shouldBe bufferSize
      writeBuf(buf, "").toJSPromise
    }

    //when
    val resultF = reader.readNextLines(lines, 0, encoding)
    
    //then
    resultF.toFuture.map { results =>
      assertViewerFileLines(results, List(
        ViewerFileLine("testfileco", 10),
        ViewerFileLine("ntent", 5)
      ))
    }
  }
  
  private def writeBuf(buf: Buffer, content: String): Future[Int] = {
    buf.write(content, 0, content.length, encoding)
    Future.successful(content.length)
  }
}

object ViewerFileReaderSpec {
  
  def assertViewerFileLines(results: js.Array[ViewerFileLine], lines: List[ViewerFileLine]): Assertion = {
    results.length shouldBe lines.size
    results.toList.zip(lines).foreach { case (result, expected) =>
      assertViewerFileLine(result, expected)
    }
    Succeeded
  }
  
  def assertViewerFileLine(result: ViewerFileLine, expected: ViewerFileLine): Assertion = {
    inside(result) {
      case ViewerFileLine(line, bytes) =>
        line shouldBe expected.line
        bytes shouldBe expected.bytes
    }
  }
}
