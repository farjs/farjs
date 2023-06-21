package farjs.viewer

import farjs.file.popups.EncodingsPopupProps
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
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
  ViewerContent.encodingsPopup = mockUiComponent("EncodingsPopup")

  //noinspection TypeAnnotation
  class ViewerFileReader {
    val readPrevLines = mockFunction[Int, Double, Double, String, Future[List[(String, Int)]]]
    val readNextLines = mockFunction[Int, Double, String, Future[List[(String, Int)]]]

    val fileReader = new MockViewerFileReader(
      readPrevLinesMock = readPrevLines,
      readNextLinesMock = readNextLines
    )
  }

  //noinspection TypeAnnotation
  class TestContext(implicit pos: Position) {
    
    val inputRef = ReactRef.create[BlessedElement]
    val fileReader = new ViewerFileReader
    val setViewport = mockFunction[Option[ViewerFileViewport], Unit]
    var props = getViewerContentProps(inputRef, fileReader, setViewport)
    var viewport = props.viewport
    val readF = Future.successful("test \nfile content".split('\n').map(c => (c, c.length)).toList)
    fileReader.readNextLines.expects(viewport.height, 0.0, viewport.encoding).returning(readF)
    val renderer = createTestRenderer(withThemeContext(<(ViewerContent())(^.wrapped := props)()))
  
    setViewport.expects(*).onCall { maybeViewport: Option[ViewerFileViewport] =>
      inside(maybeViewport) { case Some(vp) =>
        viewport = vp
        TestRenderer.act { () =>
          props = props.copy(viewport = vp)
          renderer.update(withThemeContext(<(ViewerContent())(^.wrapped := props)()))
        }
      }
    }.anyNumberOfTimes()
    assertViewerContent(renderer.root, props, content = Nil)
  }

  it should "not move viewport if not completed when onWheel(up/down)" in {
    //given
    val inputRef = ReactRef.create[BlessedElement]
    val fileReader = new ViewerFileReader
    val setViewport = mockFunction[Option[ViewerFileViewport], Unit]
    var props = getViewerContentProps(inputRef, fileReader, setViewport)
    var viewport = props.viewport
    val readP = Promise[List[(String, Int)]]()
    fileReader.readNextLines.expects(viewport.height, 0.0, viewport.encoding).returning(readP.future)
    val renderer = createTestRenderer(withThemeContext(<(ViewerContent())(^.wrapped := props)()))

    setViewport.expects(*).onCall { maybeViewport: Option[ViewerFileViewport] =>
      inside(maybeViewport) { case Some(vp) =>
        viewport = vp
        TestRenderer.act { () =>
          props = props.copy(viewport = vp)
          renderer.update(withThemeContext(<(ViewerContent())(^.wrapped := props)()))
        }
      }
    }
    assertViewerContent(renderer.root, props, content = Nil)

    //then
    fileReader.readPrevLines.expects(*, *, *, *).never()
    fileReader.readNextLines.expects(*, *, *).never()

    //when
    findComponentProps(renderer.root, viewerInput).onWheel(true)
    findComponentProps(renderer.root, viewerInput).onWheel(false)
    findComponentProps(renderer.root, viewerInput).onWheel(true)
    findComponentProps(renderer.root, viewerInput).onWheel(false)

    //then
    assertViewerContent(renderer.root, props, content = Nil)
    readP.success("completed".split('\n').map(c => (c, c.length)).toList)
    eventually {
      assertViewerContent(renderer.root, props, List(
        "completed"
      ))
    }
  }

  it should "move viewport when onWheel" in {
    //given
    val ctx = new TestContext
    import ctx._

    def check(up: Boolean, lines: Int, position: Double, content: String, expected: List[String])
             (implicit pos: Position): () => Future[Unit] = { () =>

      val readF = Future.successful(content.split('\n').map(c => (c, c.length)).toList)

      //then
      if (up) fileReader.readPrevLines.expects(lines, position, viewport.size, viewport.encoding).returning(readF)
      else fileReader.readNextLines.expects(lines, position, viewport.encoding).returning(readF)

      //when
      findComponentProps(renderer.root, viewerInput).onWheel(up)

      //then
      eventually(assertViewerContent(renderer.root, props, expected))
    }

    eventually {
      assertViewerContent(renderer.root, props, List(
        "test ",
        "file content"
      ))
    }.flatMap { _ =>
      List(
        //when & then
        check(up = false, lines = 1, position = 17.0, "end", List(
          "file content",
          "end"
        )),
        check(up = true, lines = 1, position = 5.0, "begin", List(
          "begin",
          "file content",
          "end"
        ))
      ).foldLeft(Future.unit)((res, f) => res.flatMap(_ => f())).map(_ => Succeeded)
    }
  }

  it should "show popup and switch encoding when onKeypress(F8)" in {
    //given
    val ctx = new TestContext
    import ctx._

    def check(encoding: String, content: String, expected: List[String])
             (implicit pos: Position): () => Future[Unit] = { () =>

      val readF = Future.successful(content.split('\n').map(c => (c, c.length)).toList)

      //then
      fileReader.readNextLines.expects(viewport.height, viewport.position, encoding).returning(readF)

      //when
      findComponentProps(renderer.root, encodingsPopup).onApply(encoding)

      //then
      eventually(assertViewerContent(renderer.root, props, expected, hasEncodingsPopup = true))
    }

    eventually {
      assertViewerContent(renderer.root, props, List(
        "test ",
        "file content"
      ))
    }.flatMap { _ =>
      findComponentProps(renderer.root, viewerInput).onKeypress("f8")
      eventually(findComponentProps(renderer.root, encodingsPopup))
    }.flatMap { _ =>
      List(
        //when & then
        check("latin1", "reload1", List(
          "reload1"
        )),
        check("utf-8", "reload2", List(
          "reload2"
        )), { () =>
          Future.successful {
            findComponentProps(renderer.root, encodingsPopup).onClose()
            findProps(renderer.root, encodingsPopup) should be (empty)
            ()
          }
        }
      ).foldLeft(Future.unit)((res, f) => res.flatMap(_ => f())).map(_ => Succeeded)
    }
  }

  it should "re-load prev page if at the end when onKeypress(down)" in {
    //given
    val inputRef = ReactRef.create[BlessedElement]
    val fileReader = new ViewerFileReader
    val setViewport = mockFunction[Option[ViewerFileViewport], Unit]
    var props = {
      val p = getViewerContentProps(inputRef, fileReader, setViewport)
      p.copy(viewport = p.viewport.copy(size = 10))
    }
    var viewport = props.viewport
    val readF = Future.successful("1\n2\n3\n4\n5\n".split('\n').map(c => (c, c.length + 1)).toList)
    fileReader.readNextLines.expects(viewport.height, 0.0, viewport.encoding).returning(readF)
    val renderer = createTestRenderer(withThemeContext(<(ViewerContent())(^.wrapped := props)()))

    setViewport.expects(*).onCall { maybeViewport: Option[ViewerFileViewport] =>
      inside(maybeViewport) { case Some(vp) =>
        viewport = vp
        TestRenderer.act { () =>
          props = props.copy(viewport = vp)
          renderer.update(withThemeContext(<(ViewerContent())(^.wrapped := props)()))
        }
      }
    }.anyNumberOfTimes()
    eventually {
      assertViewerContent(renderer.root, props, List(
        "1",
        "2",
        "3",
        "4",
        "5"
      ))
    }.flatMap { _ =>
      //then
      val resF = Future.successful("2\n3\n4\n5\n\n".split('\n').map(c => (c, c.length + 1)).toList)
      fileReader.readPrevLines.expects(viewport.height, viewport.size, viewport.size, viewport.encoding).returning(resF)
  
      //when
      findComponentProps(renderer.root, viewerInput).onKeypress("down")
  
      //then
      eventually {
        assertViewerContent(renderer.root, props, List(
          "2",
          "3",
          "4",
          "5"
        ))
      }
    }
  }

  it should "move viewport when onKeypress" in {
    //given
    val ctx = new TestContext
    import ctx._

    def check(key: String, lines: Int, position: Double, content: String, expected: List[String], noop: Boolean = false)
             (implicit pos: Position): () => Future[Unit] = { () =>

      val readF =
        if (content.isEmpty) Future.successful(Nil)
        else Future.successful(content.split('\n').map(c => (c, c.length)).toList)

      //then
      if (!noop) {
        if (Set("end", "up", "pageup").contains(key)) {
          fileReader.readPrevLines.expects(lines, position, viewport.size, viewport.encoding).returning(readF)
        }
        else {
          fileReader.readNextLines.expects(lines, position, viewport.encoding).returning(readF)
        }
      }

      //when
      findComponentProps(renderer.root, viewerInput).onKeypress(key)

      //then
      eventually(assertViewerContent(renderer.root, props, expected))
    }

    eventually {
      assertViewerContent(renderer.root, props, List(
        "test ",
        "file content"
      ))
    }.flatMap { _ =>
      List(
        //when & then
        check(key = "C-r", lines = viewport.height, position = 0.0, "new content", List(
          "new content"
        )),
        check(key = "end", lines = viewport.height, position = viewport.size, "ending", List(
          "ending"
        )),
        check(key = "home", lines = viewport.height, position = 0.0, "beginning", List(
          "beginning"
        )),
        check(key = "up", lines = 1, position = 0.0, "already at the beginning", List(
          "beginning"
        ), noop = true),
        check(key = "down", lines = 1, position = 9, "next line 1", List(
          "next line 1"
        )),
        check(key = "down", lines = 1, position = 20, "", List(
          "next line 1"
        )),
        check(key = "down", lines = 1, position = 20, "line2", List(
          "line2"
        )),
        check(key = "down", lines = 1, position = 25, "out of file size", List(
          "line2"
        ), noop = true),
        check(key = "up", lines = 1, position = 20, "prev line", List(
          "prev line",
          "line2"
        )),
        check(key = "up", lines = 1, position = 11, "", List(
          "prev line",
            "line2"
        )),
        check(key = "pageup", lines = viewport.height, position = 11, "1\n2\n3\n4", List(
          "1",
          "2",
          "3",
          "4",
          "prev line"
        )),
        check(key = "pagedown", lines = viewport.height, position = 20, "next paaaaaaage", List(
          "next paaaaaa"
        )),
        check(key = "left", lines = viewport.height, position = 20, "", List(
          "next paaaaaa"
        ), noop = true),
        check(key = "right", lines = viewport.height, position = 20, "", List(
          "ext paaaaaaa"
        ), noop = true),
        check(key = "right", lines = viewport.height, position = 20, "", List(
          "xt paaaaaaag"
        ), noop = true),
        check(key = "left", lines = viewport.height, position = 20, "", List(
          "ext paaaaaaa"
        ), noop = true),
        check(key = "f2", lines = viewport.height, position = 20, "loooooooooooong line\n", List(
          "looooooooooo",
          "ong line"
        )),
        check(key = "up", lines = 1, position = 20, "prev liiiiiiiiine 1\n", List(
          "iiine 1",
          "looooooooooo",
          "ong line"
        )),
        check(key = "home", lines = viewport.height, position = 0.0, "beginning", List(
          "beginning",
        )),
        check(key = "down", lines = 1, position = 9, "next liiiiiiiiine 1\n", List(
          "next liiiiii"
        )),
        check(key = "right", lines = viewport.height, position = 9, "", List(
          "ext liiiiii"
        ), noop = true),
        check(key = "f2", lines = viewport.height, position = 9, "next liiiiiiiiine 1\n", List(
          "ext liiiiiii"
        ))
      ).foldLeft(Future.unit)((res, f) => res.flatMap(_ => f())).map(_ => Succeeded)
    }
  }

  it should "do nothing if onKeypress callback returns true when onKeypress(f2)" in {
    //given
    val inputRef = ReactRef.create[BlessedElement]
    val fileReader = new ViewerFileReader
    val setViewport = mockFunction[Option[ViewerFileViewport], Unit]
    var props = getViewerContentProps(inputRef, fileReader, setViewport, onKeypress = _ => true)
    var viewport = props.viewport
    val readF = Future.successful("test \nfile content".split('\n').map(c => (c, c.length)).toList)
    fileReader.readNextLines.expects(viewport.height, 0.0, viewport.encoding).returning(readF)
    val renderer = createTestRenderer(withThemeContext(<(ViewerContent())(^.wrapped := props)()))

    setViewport.expects(*).onCall { maybeViewport: Option[ViewerFileViewport] =>
      inside(maybeViewport) { case Some(vp) =>
        viewport = vp
        TestRenderer.act { () =>
          props = props.copy(viewport = vp)
          renderer.update(withThemeContext(<(ViewerContent())(^.wrapped := props)()))
        }
      }
    }.anyNumberOfTimes()
    assertViewerContent(renderer.root, props, content = Nil)

    eventually {
      assertViewerContent(renderer.root, props, List(
        "test ",
        "file content"
      ))
    }.flatMap { _ =>
      //when
      findComponentProps(renderer.root, viewerInput).onKeypress("f2")
  
      //then
      eventually {
        assertViewerContent(renderer.root, props, List(
          "test ",
          "file content"
        ))
      }
    }
  }

  it should "do nothing when onKeypress(unknown)" in {
    //given
    val ctx = new TestContext
    import ctx._
    eventually {
      assertViewerContent(renderer.root, props, List(
        "test ",
        "file content"
      ))
    }.flatMap { _ =>
      //when
      findComponentProps(renderer.root, viewerInput).onKeypress("unknown")
  
      //then
      eventually {
        assertViewerContent(renderer.root, props, List(
          "test ",
          "file content"
        ))
      }
    }
  }

  it should "reload current page if props has changed" in {
    //given
    val ctx = new TestContext
    import ctx._
    eventually {
      assertViewerContent(renderer.root, props, List(
        "test ",
        "file content"
      ))
    }.flatMap { _ =>
      val updatedProps = props.copy(
        viewport = viewport.copy(
          encoding = "utf-16",
          size = 11,
          width = 61,
          height = 21
        )
      )
      updatedProps.viewport.encoding should not be viewport.encoding
      updatedProps.viewport.size should not be viewport.size
      updatedProps.viewport.width should not be viewport.width
      updatedProps.viewport.height should not be viewport.height
      val content2 = "test file content2"
      val read2F = Future.successful(List((content2, content2.length)))

      //then
      fileReader.readNextLines.expects(updatedProps.viewport.height, 0.0, updatedProps.viewport.encoding)
        .returning(read2F)

      //when
      TestRenderer.act { () =>
        renderer.update(withThemeContext(<(ViewerContent())(^.wrapped := updatedProps)()))
      }

      //then
      eventually {
        assertViewerContent(renderer.root, updatedProps, List(
          "test file content2"
        ))
      }
    }
  }
  
  it should "not reload current page if props hasn't changed" in {
    //given
    val ctx = new TestContext
    import ctx._
    eventually {
      assertViewerContent(renderer.root, props, List(
        "test ",
        "file content"
      ))
    }.flatMap { _ =>
      val updatedProps = props.copy()
      updatedProps should not be theSameInstanceAs (props)
      updatedProps.viewport.encoding shouldBe viewport.encoding
      updatedProps.viewport.size shouldBe viewport.size
      updatedProps.viewport.width shouldBe viewport.width
      updatedProps.viewport.height shouldBe viewport.height

      //when
      TestRenderer.act { () =>
        renderer.update(withThemeContext(<(ViewerContent())(^.wrapped := updatedProps)()))
      }

      //then
      eventually {
        assertViewerContent(renderer.root, updatedProps, List(
          "test ",
          "file content"
        ))
      }
    }
  }
  
  it should "call setViewport when non-empty file" in {
    //given
    val inputRef = ReactRef.create[BlessedElement]
    val fileReader = new ViewerFileReader
    val setViewport = mockFunction[Option[ViewerFileViewport], Unit]
    var props = getViewerContentProps(inputRef, fileReader, setViewport)
    var viewport = props.viewport
    val readF = Future.successful("test \nfile content\n".split('\n').map(c => (c, c.length + 1)).toList)
    fileReader.readNextLines.expects(viewport.height, 0.0, viewport.encoding).returning(readF)
    val percent = ((19 / viewport.size) * 100).toInt
    percent shouldBe 76

    //when
    val renderer = createTestRenderer(withThemeContext(<(ViewerContent())(^.wrapped := props)()))

    //then
    setViewport.expects(*).onCall { maybeViewport: Option[ViewerFileViewport] =>
      inside(maybeViewport) { case Some(vp) =>
        viewport = vp
        TestRenderer.act { () =>
          props = props.copy(viewport = vp)
          renderer.update(withThemeContext(<(ViewerContent())(^.wrapped := props)()))
        }
      }
    }
    assertViewerContent(renderer.root, props, content = Nil)
    eventually {
      assertViewerContent(renderer.root, props, List(
        "test ",
        "file content"
      ))

      viewport.progress shouldBe percent
    }
  }
  
  it should "call setViewport when empty file" in {
    //given
    val inputRef = ReactRef.create[BlessedElement]
    val fileReader = new ViewerFileReader
    val setViewport = mockFunction[Option[ViewerFileViewport], Unit]
    var props = {
      val p = getViewerContentProps(inputRef, fileReader, setViewport)
      p.copy(viewport = p.viewport.copy(size = 0))
    }
    var viewport = props.viewport
    val readF = Future.successful("test content".split('\n').map(c => (c, c.length)).toList)
    fileReader.readNextLines.expects(viewport.height, 0.0, viewport.encoding).returning(readF)
    val percent = 0

    //when
    val renderer = createTestRenderer(withThemeContext(<(ViewerContent())(^.wrapped := props)()))

    //then
    setViewport.expects(*).onCall { maybeViewport: Option[ViewerFileViewport] =>
      inside(maybeViewport) { case Some(vp) =>
        viewport = vp
        TestRenderer.act { () =>
          props = props.copy(viewport = vp)
          renderer.update(withThemeContext(<(ViewerContent())(^.wrapped := props)()))
        }
      }
    }
    assertViewerContent(renderer.root, props, content = Nil)
    eventually {
      assertViewerContent(renderer.root, props, List(
        "test content"
      ))

      viewport.progress shouldBe percent
    }
  }
  
  private def getViewerContentProps(inputRef: ReactRef[BlessedElement],
                                    fileReader: ViewerFileReader,
                                    setViewport: Option[ViewerFileViewport] => Unit,
                                    onKeypress: String => Boolean = _ => false) = {
    ViewerContentProps(
      inputRef = inputRef,
      viewport = ViewerFileViewport(
        fileReader = fileReader.fileReader,
        encoding = "utf-8",
        size = 25,
        width = 12,
        height = 5
      ),
      setViewport = setViewport,
      onKeypress = onKeypress
    )
  }

  private def assertViewerContent(result: TestInstance,
                                  props: ViewerContentProps,
                                  content: List[String],
                                  hasEncodingsPopup: Boolean = false
                                 )(implicit pos: Position): Assertion = {
    val theme = DefaultTheme

    assertNativeComponent(result.children.head,
      <(viewerInput())(^.assertWrapped(inside(_) {
        case ViewerInputProps(inputRef, _, _) =>
          inputRef shouldBe props.inputRef
      }))(), { children =>
        val (text, maybePopup) = inside(children) {
          case List(text) => (text, None)
          case List(text, popup) => (text, Some(popup))
        }

        assertNativeComponent(text,
          <.text(
            ^.rbWidth := props.viewport.width,
            ^.rbHeight := props.viewport.height,
            ^.rbStyle := ViewerController.contentStyle(theme),
            ^.rbWrap := false,
            ^.content := {
              if (content.isEmpty) ""
              else s"${content.mkString("\n")}\n"
            }
          )()
        )
        maybePopup.isDefined shouldBe hasEncodingsPopup
        maybePopup.foreach { popup =>
          assertNativeComponent(popup,
            <(encodingsPopup())(^.assertWrapped(inside(_) {
              case EncodingsPopupProps(encoding, _, _) =>
                encoding shouldBe props.viewport.encoding
            }))()
          )
        }
        Succeeded
      }
    )
  }
}
