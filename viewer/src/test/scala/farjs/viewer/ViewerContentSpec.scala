package farjs.viewer

import org.scalatest.Assertion
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.blessed._
import scommons.react.test._

import scala.concurrent.Future

class ViewerContentSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  //noinspection TypeAnnotation
  class ViewerFileReaderMock {
    val readPageAtMock = mockFunction[Double, String, Future[(String, Int)]]

    val fileReader = new ViewerFileReader {
      override def readPageAt(position: Double, encoding: String): Future[(String, Int)] = {
        readPageAtMock(position, encoding)
      }
    }
  }

  it should "re-read current page if encoding has changed" in {
    //given
    val fileReader = new ViewerFileReaderMock
    val props = ViewerContentProps(fileReader.fileReader, "utf-8", width = 60, height = 20)
    val content1 = "test \nfile content\n"
    val read1F = Future.successful((content1, content1.length))
    fileReader.readPageAtMock.expects(0.0, props.encoding).returning(read1F)
    val renderer = createTestRenderer(<(ViewerContent())(^.wrapped := props)())

    read1F.flatMap { _ =>
      assertViewerContent(renderer.root, props, content1)

      val updatedProps = props.copy(encoding = "utf-16")
      updatedProps.encoding should not be props.encoding
      val content2 = "test file content2"
      val read2F = Future.successful((content2, content2.length))

      //then
      fileReader.readPageAtMock.expects(0.0, updatedProps.encoding).returning(read2F)

      //when
      TestRenderer.act { () =>
        renderer.update(<(ViewerContent())(^.wrapped := updatedProps)())
      }

      read2F.map { _ =>
        assertViewerContent(renderer.root, updatedProps, content2)
      }
    }
  }
  
  it should "not re-read current page if encoding hasn't changed" in {
    //given
    val fileReader = new ViewerFileReaderMock
    val props = ViewerContentProps(fileReader.fileReader, "utf-8", width = 60, height = 20)
    val content = "test \nfile content\n"
    val readF = Future.successful((content, content.length))

    //then
    fileReader.readPageAtMock.expects(0.0, props.encoding).returning(readF)

    val renderer = createTestRenderer(<(ViewerContent())(^.wrapped := props)())
    readF.flatMap { _ =>
      assertViewerContent(renderer.root, props, content)

      val updatedProps = props.copy()
      updatedProps should not be theSameInstanceAs (props)
      updatedProps.encoding shouldBe props.encoding

      //when
      TestRenderer.act { () =>
        renderer.update(<(ViewerContent())(^.wrapped := updatedProps)())
      }

      readF.map { _ =>
        assertViewerContent(renderer.root, updatedProps, content)
      }
    }
  }
  
  it should "render initial component" in {
    //given
    val fileReader = new ViewerFileReaderMock
    val props = ViewerContentProps(fileReader.fileReader, "utf-8", width = 60, height = 20)
    val content = "test \nfile content\n"
    val readF = Future.successful((content, content.length))
    fileReader.readPageAtMock.expects(0.0, props.encoding).returning(readF)

    //when
    val renderer = createTestRenderer(<(ViewerContent())(^.wrapped := props)())

    //then
    assertViewerContent(renderer.root, props, content = "")
    readF.map { _ =>
      assertViewerContent(renderer.root, props, content)
    }
  }
  
  private def assertViewerContent(result: TestInstance,
                                  props: ViewerContentProps,
                                  content: String): Assertion = {


    assertComponents(result.children, List(
      <.text(
        ^.rbStyle := ViewerController.contentStyle,
        ^.content := content
      )()
    ))
  }
}
