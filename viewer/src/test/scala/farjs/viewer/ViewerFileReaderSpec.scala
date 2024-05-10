package farjs.viewer

import scommons.nodejs.Buffer
import scommons.nodejs.test.AsyncTestSpec

import scala.concurrent.Future
import scala.scalajs.js

class ViewerFileReaderSpec extends AsyncTestSpec {

  //noinspection TypeAnnotation
  class FileReader {
    val readBytes = mockFunction[Double, Int, Future[Buffer]]

    val fileReader = new MockFileReader(
      readBytesMock = readBytes
    )
  }
  
  private val bufferSize = 15
  private val maxLineLength = 10
  private val encoding = "utf-8"

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
    resultF.map { linesData =>
      linesData shouldBe Nil
    }
  }

  it should "read file content with new line at the end when readPrevLines" in {
    //given
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val position = 18
    val lines = 3

    //then
    fileReader.readBytes.expects(3, bufferSize)
      .returning(Future.successful(Buffer.from("t file\ncontent\n", encoding)))
    fileReader.readBytes.expects(0, 3)
      .returning(Future.successful(Buffer.from("tes", encoding)))

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
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val position = 18
    val lines = 3

    //then
    fileReader.readBytes.expects(3, bufferSize)
      .returning(Future.successful(Buffer.from("st file\ncontent", encoding)))
    fileReader.readBytes.expects(0, 3)
      .returning(Future.successful(Buffer.from("\nte", encoding)))

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

  it should "read content from the middle of file when readPrevLines" in {
    //given
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val position = 9
    val lines = 3

    //then
    fileReader.readBytes.expects(0, 9).returning(Future.successful(Buffer.from("test\nfile", encoding)))

    //when
    val resultF = reader.readPrevLines(lines, position, maxPos = 25, encoding)
    
    //then
    resultF.map { linesData =>
      linesData shouldBe List(
        "test" -> 5,
        "file" -> 4
      )
    }
  }

  it should "read single empty line at the start when readPrevLines" in {
    //given
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val position = 1
    val lines = 2

    //then
    fileReader.readBytes.expects(0, 1).returning(Future.successful(Buffer.from("\n", encoding)))

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
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val position = 15
    val lines = 3

    //then
    fileReader.readBytes.expects(0, bufferSize)
      .returning(Future.successful(Buffer.from("testfilecontent", encoding)))

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

  it should "read file content when readNextLines" in {
    //given
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val lines = 3

    //then
    fileReader.readBytes.expects(0, bufferSize)
      .returning(Future.successful(Buffer.from("\ntest file\ncon", encoding)))
    fileReader.readBytes.expects(14, bufferSize)
      .returning(Future.successful(Buffer.from("tent\n", encoding)))

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
    val fileReader = new FileReader
    val reader = new ViewerFileReader(fileReader.fileReader, bufferSize, maxLineLength)
    val lines = 3

    //then
    fileReader.readBytes.expects(0, bufferSize)
      .returning(Future.successful(Buffer.from("testfilecontent", encoding)))
    fileReader.readBytes.expects(15, bufferSize)
      .returning(Future.successful(Buffer.from("", encoding)))

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
