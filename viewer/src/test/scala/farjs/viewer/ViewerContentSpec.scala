package farjs.viewer

import farjs.viewer.ViewerContent._
import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactRef
import scommons.react.blessed._
import scommons.react.test._

import scala.concurrent.{Future, Promise}

class ViewerContentSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  ViewerContent.viewerInput = mockUiComponent("ViewerInput")

  //noinspection TypeAnnotation
  class ViewerFileReaderMock {
    val readPrevLinesMock = mockFunction[Int, Double, Double, String, Future[List[(String, Int)]]]
    val readNextLinesMock = mockFunction[Int, Double, String, Future[List[(String, Int)]]]

    val fileReader = new ViewerFileReader(bufferSize = 15, maxLineLength = 10) {
      override def readPrevLines(lines: Int, position: Double, maxPos: Double, encoding: String): Future[List[(String, Int)]] = {
        readPrevLinesMock(lines, position, maxPos, encoding)
      }
      override def readNextLines(lines: Int, position: Double, encoding: String): Future[List[(String, Int)]] = {
        readNextLinesMock(lines, position, encoding)
      }
    }
  }

  //noinspection TypeAnnotation
  class TestContext(implicit pos: Position) {
    
    val inputRef = ReactRef.create[BlessedElement]
    val fileReader = new ViewerFileReaderMock
    val props = getViewerContentProps(inputRef, fileReader)
    val readF = Future.successful("test \nfile content".split('\n').map(c => (c, c.length)).toList)
    fileReader.readNextLinesMock.expects(props.height, 0.0, props.encoding).returning(readF)
    val renderer = createTestRenderer(<(ViewerContent())(^.wrapped := props)())
  
    assertViewerContent(renderer.root, props, content = "")
  }

  it should "not move viewport if not completed when onWheel(up/down)" in {
    //given
    val inputRef = ReactRef.create[BlessedElement]
    val fileReader = new ViewerFileReaderMock
    val props = getViewerContentProps(inputRef, fileReader)
    val readP = Promise[List[(String, Int)]]()
    fileReader.readNextLinesMock.expects(props.height, 0.0, props.encoding).returning(readP.future)
    val renderer = createTestRenderer(<(ViewerContent())(^.wrapped := props)())

    assertViewerContent(renderer.root, props, content = "")

    //then
    fileReader.readPrevLinesMock.expects(*, *, *, *).never()
    fileReader.readNextLinesMock.expects(*, *, *).never()

    //when
    findComponentProps(renderer.root, viewerInput).onWheel(true)
    findComponentProps(renderer.root, viewerInput).onWheel(false)
    findComponentProps(renderer.root, viewerInput).onWheel(true)
    findComponentProps(renderer.root, viewerInput).onWheel(false)

    //then
    assertViewerContent(renderer.root, props, content = "")
    readP.success("completed".split('\n').map(c => (c, c.length)).toList)
    eventually {
      assertViewerContent(renderer.root, props,
        """completed
          |""".stripMargin)
    }
  }

  it should "move viewport when onWheel" in {
    //given
    val ctx = new TestContext
    import ctx._

    def check(up: Boolean, lines: Int, position: Double, content: String, expected: String)
             (implicit pos: Position): () => Future[Unit] = { () =>

      val readF = Future.successful(content.split('\n').map(c => (c, c.length)).toList)

      //then
      if (up) fileReader.readPrevLinesMock.expects(lines, position, props.size, props.encoding).returning(readF)
      else fileReader.readNextLinesMock.expects(lines, position, props.encoding).returning(readF)

      //when
      findComponentProps(renderer.root, viewerInput).onWheel(up)

      //then
      eventually(assertViewerContent(renderer.root, props, expected))
    }

    eventually {
      assertViewerContent(renderer.root, props,
        """test 
          |file content
          |""".stripMargin)
    }.flatMap { _ =>
      List(
        //when & then
        check(up = false, lines = 1, position = 17.0, "end",
          """file content
            |end
            |""".stripMargin
        ),
        check(up = true, lines = 1, position = 5.0, "begin",
          """begin
            |file content
            |end
            |""".stripMargin
        )
      ).foldLeft(Future.unit)((res, f) => res.flatMap(_ => f())).map(_ => Succeeded)
    }
  }

  it should "re-load prev page if at the end when onKeypress(down)" in {
    //given
    val inputRef = ReactRef.create[BlessedElement]
    val fileReader = new ViewerFileReaderMock
    val props = getViewerContentProps(inputRef, fileReader).copy(size = 10)
    val readF = Future.successful("1\n2\n3\n4\n5\n".split('\n').map(c => (c, c.length + 1)).toList)
    fileReader.readNextLinesMock.expects(props.height, 0.0, props.encoding).returning(readF)
    val renderer = createTestRenderer(<(ViewerContent())(^.wrapped := props)())

    eventually {
      assertViewerContent(renderer.root, props,
        """1
          |2
          |3
          |4
          |5
          |""".stripMargin)
    }.flatMap { _ =>
      //then
      val resF = Future.successful("2\n3\n4\n5\n\n".split('\n').map(c => (c, c.length + 1)).toList)
      fileReader.readPrevLinesMock.expects(props.height, props.size, props.size, props.encoding).returning(resF)
  
      //when
      findComponentProps(renderer.root, viewerInput).onKeypress("down")
  
      //then
      eventually {
        assertViewerContent(renderer.root, props,
          """2
            |3
            |4
            |5
            |""".stripMargin)
      }
    }
  }

  it should "move viewport when onKeypress" in {
    //given
    val ctx = new TestContext
    import ctx._

    def check(key: String, lines: Int, position: Double, content: String, expected: String)
             (implicit pos: Position): () => Future[Unit] = { () =>

      val readF =
        if (content.isEmpty) Future.successful(Nil)
        else Future.successful(content.split('\n').map(c => (c, c.length)).toList)

      //then
      if (Set("end", "up", "pageup").contains(key)) {
        if (position > 0.0) {
          fileReader.readPrevLinesMock.expects(lines, position, props.size, props.encoding).returning(readF)
        }
      }
      else if (position < props.size) {
        fileReader.readNextLinesMock.expects(lines, position, props.encoding).returning(readF)
      }

      //when
      findComponentProps(renderer.root, viewerInput).onKeypress(key)

      //then
      eventually(assertViewerContent(renderer.root, props, expected))
    }

    eventually {
      assertViewerContent(renderer.root, props,
        """test 
          |file content
          |""".stripMargin)
    }.flatMap { _ =>
      List(
        //when & then
        check(key = "C-r", lines = props.height, position = 0.0, "new content",
          """new content
            |""".stripMargin
        ),
        check(key = "end", lines = props.height, position = props.size, "ending",
          """ending
            |""".stripMargin
        ),
        check(key = "home", lines = props.height, position = 0.0, "beginning",
          """beginning
            |""".stripMargin
        ),
        check(key = "up", lines = 1, position = 0.0, "already at the beginning", //noop
          """beginning
            |""".stripMargin
        ),
        check(key = "down", lines = 1, position = 9, "next line 1",
          """next line 1
            |""".stripMargin
        ),
        check(key = "down", lines = 1, position = 20, "", //noop
          """next line 1
            |""".stripMargin
        ),
        check(key = "down", lines = 1, position = 20, "line2",
          """line2
            |""".stripMargin
        ),
        check(key = "down", lines = 1, position = 25, "out of file size", //noop
          """line2
            |""".stripMargin
        ),
        check(key = "up", lines = 1, position = 20, "prev line",
          """prev line
            |line2
            |""".stripMargin
        ),
        check(key = "up", lines = 1, position = 11, "", //noop
          """prev line
            |line2
            |""".stripMargin
        ),
        check(key = "pageup", lines = props.height, position = 11, "1\n2\n3\n4",
          """1
            |2
            |3
            |4
            |prev line
            |""".stripMargin
        ),
        check(key = "pagedown", lines = props.height, position = 20, "next page",
          """next page
            |""".stripMargin
        )
      ).foldLeft(Future.unit)((res, f) => res.flatMap(_ => f())).map(_ => Succeeded)
    }
  }

  it should "do nothing when onKeypress(unknown)" in {
    //given
    val ctx = new TestContext
    import ctx._
    eventually {
      assertViewerContent(renderer.root, props,
        """test 
          |file content
          |""".stripMargin)
    }.flatMap { _ =>
      //when
      findComponentProps(renderer.root, viewerInput).onKeypress("unknown")
  
      //then
      eventually {
        assertViewerContent(renderer.root, props,
          """test 
            |file content
            |""".stripMargin)
      }
    }
  }

  it should "reload current page if props has changed" in {
    //given
    val ctx = new TestContext
    import ctx._
    eventually {
      assertViewerContent(renderer.root, props,
        """test 
          |file content
          |""".stripMargin)
    }.flatMap { _ =>
      val updatedProps = props.copy(
        encoding = "utf-16",
        size = 11,
        width = 61,
        height = 21
      )
      updatedProps.encoding should not be props.encoding
      updatedProps.size should not be props.size
      updatedProps.width should not be props.width
      updatedProps.height should not be props.height
      val content2 = "test file content2"
      val read2F = Future.successful(List((content2, content2.length)))

      //then
      fileReader.readNextLinesMock.expects(updatedProps.height, 0.0, updatedProps.encoding)
        .returning(read2F)

      //when
      TestRenderer.act { () =>
        renderer.update(<(ViewerContent())(^.wrapped := updatedProps)())
      }

      //then
      eventually {
        assertViewerContent(renderer.root, updatedProps,
          """test file content2
            |""".stripMargin)
      }
    }
  }
  
  it should "not reload current page if props hasn't changed" in {
    //given
    val ctx = new TestContext
    import ctx._
    eventually {
      assertViewerContent(renderer.root, props,
        """test 
          |file content
          |""".stripMargin)
    }.flatMap { _ =>
      val updatedProps = props.copy()
      updatedProps should not be theSameInstanceAs (props)
      updatedProps.encoding shouldBe props.encoding
      updatedProps.size shouldBe props.size
      updatedProps.width shouldBe props.width
      updatedProps.height shouldBe props.height

      //when
      TestRenderer.act { () =>
        renderer.update(<(ViewerContent())(^.wrapped := updatedProps)())
      }

      //then
      eventually {
        assertViewerContent(renderer.root, updatedProps,
          """test 
            |file content
            |""".stripMargin)
      }
    }
  }
  
  it should "call onViewProgress when non-empty file" in {
    //given
    val inputRef = ReactRef.create[BlessedElement]
    val fileReader = new ViewerFileReaderMock
    val onViewProgress = mockFunction[Int, Unit]
    val props = getViewerContentProps(inputRef, fileReader, onViewProgress)
    val readF = Future.successful("test \nfile content\n".split('\n').map(c => (c, c.length + 1)).toList)
    fileReader.readNextLinesMock.expects(props.height, 0.0, props.encoding).returning(readF)
    val percent = ((19 / props.size) * 100).toInt
    percent shouldBe 76

    //then
    onViewProgress.expects(percent)

    //when
    val renderer = createTestRenderer(<(ViewerContent())(^.wrapped := props)())

    //then
    assertViewerContent(renderer.root, props, content = "")
    eventually {
      assertViewerContent(renderer.root, props,
        """test 
          |file content
          |""".stripMargin)
    }
  }
  
  it should "call onViewProgress when empty file" in {
    //given
    val inputRef = ReactRef.create[BlessedElement]
    val fileReader = new ViewerFileReaderMock
    val onViewProgress = mockFunction[Int, Unit]
    val props = getViewerContentProps(inputRef, fileReader, onViewProgress).copy(size = 0)
    val readF = Future.successful("test content".split('\n').map(c => (c, c.length)).toList)
    fileReader.readNextLinesMock.expects(props.height, 0.0, props.encoding).returning(readF)
    val percent = 0

    //then
    onViewProgress.expects(percent)

    //when
    val renderer = createTestRenderer(<(ViewerContent())(^.wrapped := props)())

    //then
    assertViewerContent(renderer.root, props, content = "")
    eventually {
      assertViewerContent(renderer.root, props,
        """test content
          |""".stripMargin)
    }
  }
  
  private def getViewerContentProps(inputRef: ReactRef[BlessedElement],
                                    fileReader: ViewerFileReaderMock,
                                    onViewProgress: Int => Unit = _ => ()) = {
    ViewerContentProps(
      inputRef = inputRef,
      fileReader = fileReader.fileReader,
      encoding = "utf-8",
      size = 25,
      width = 60,
      height = 5,
      onViewProgress = onViewProgress
    )
  }

  private def assertViewerContent(result: TestInstance,
                                  props: ViewerContentProps,
                                  content: String)(implicit pos: Position): Assertion = {

    assertComponents(result.children, List(
      <(viewerInput())(^.assertWrapped(inside(_) {
        case ViewerInputProps(inputRef, _, _) =>
          inputRef shouldBe props.inputRef
      }))(
        <.text(
          ^.rbStyle := ViewerController.contentStyle,
          ^.content := content
        )()
      )
    ))
  }
}
