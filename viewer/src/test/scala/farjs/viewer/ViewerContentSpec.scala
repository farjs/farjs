package farjs.viewer

import farjs.file.popups.{EncodingsPopupProps, TextSearchPopupProps}
import farjs.filelist.theme.FileListTheme
import farjs.filelist.theme.FileListThemeSpec.withThemeContext
import farjs.viewer.ViewerContent._
import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.blessed._
import scommons.react.raw.NativeRef
import scommons.react.test._
import scommons.react.{ReactClass, raw}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSConverters.JSRichFutureNonThenable

class ViewerContentSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  ViewerContent.viewerInput = mockUiComponent("ViewerInput")
  ViewerContent.encodingsPopup = "EncodingsPopup".asInstanceOf[ReactClass]
  ViewerContent.textSearchPopup = "TextSearchPopup".asInstanceOf[ReactClass]
  ViewerContent.viewerSearch = mockUiComponent("ViewerSearch")

  //noinspection TypeAnnotation
  class ViewerFileReader {
    val readPrevLines = mockFunction[Int, Double, Double, String, js.Promise[js.Array[ViewerFileLine]]]
    val readNextLines = mockFunction[Int, Double, String, js.Promise[js.Array[ViewerFileLine]]]

    val fileReader = new MockViewerFileReader(
      readPrevLinesMock = readPrevLines,
      readNextLinesMock = readNextLines
    )
  }

  //noinspection TypeAnnotation
  class TestContext(implicit pos: Position) {

    val inputRef = raw.React.createRef()
    val fileReader = new ViewerFileReader
    val setViewport = mockFunction[Option[ViewerFileViewport], Unit]
    var props = getViewerContentProps(inputRef, fileReader, setViewport)
    var viewport = props.viewport
    val readF = js.Promise.resolve[js.Array[ViewerFileLine]](js.Array[ViewerFileLine]("test \nfile content".split('\n').map(c => ViewerFileLine(c, c.length)).toList: _*))
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
    val inputRef = raw.React.createRef()
    val fileReader = new ViewerFileReader
    val setViewport = mockFunction[Option[ViewerFileViewport], Unit]
    var props = getViewerContentProps(inputRef, fileReader, setViewport)
    var viewport = props.viewport
    val readP = Promise[js.Array[ViewerFileLine]]()
    fileReader.readNextLines.expects(viewport.height, 0.0, viewport.encoding).returning(readP.future.toJSPromise)
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
    findComponentProps(renderer.root, viewerInput, plain = true).onWheel(true)
    findComponentProps(renderer.root, viewerInput, plain = true).onWheel(false)
    findComponentProps(renderer.root, viewerInput, plain = true).onWheel(true)
    findComponentProps(renderer.root, viewerInput, plain = true).onWheel(false)

    //then
    assertViewerContent(renderer.root, props, content = Nil)
    readP.success(js.Array("completed".split('\n').map(c => ViewerFileLine(c, c.length)).toList: _*))
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

      val readF = js.Promise.resolve[js.Array[ViewerFileLine]](js.Array(content.split('\n').map(c => ViewerFileLine(c, c.length)).toList: _*))

      //then
      if (up) fileReader.readPrevLines.expects(lines, position, viewport.size, viewport.encoding).returning(readF)
      else fileReader.readNextLines.expects(lines, position, viewport.encoding).returning(readF)

      //when
      findComponentProps(renderer.root, viewerInput, plain = true).onWheel(up)

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

  it should "show and close search popup when onKeypress(F7)" in {
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
      findComponentProps(renderer.root, viewerInput, plain = true).onKeypress("f7")
      inside(findComponents(renderer.root, textSearchPopup)) {
        case List(c) => c.props.asInstanceOf[TextSearchPopupProps].onCancel()
      }

      //then
      findComponents(renderer.root, textSearchPopup) should be(empty)
    }
  }

  it should "trigger search when onKeypress(F7)" in {
    //given
    val ctx = new TestContext
    import ctx._

    eventually {
      assertViewerContent(renderer.root, props, List(
        "test ",
        "file content"
      ))
    }.flatMap { _ =>
      findComponentProps(renderer.root, viewerInput, plain = true).onKeypress("f7")
      val searchTerm = "test"

      //when & then
      inside(findComponents(renderer.root, textSearchPopup)) {
        case List(c) => c.props.asInstanceOf[TextSearchPopupProps].onSearch(searchTerm)
      }
      findComponents(renderer.root, textSearchPopup) should be(empty)

      //when & then
      findComponentProps(renderer.root, viewerSearch, plain = true).onComplete()
      findProps(renderer.root, viewerSearch, plain = true) should be(empty)
    }
  }

  it should "show encodings popup and switch encoding when onKeypress(F8)" in {
    //given
    val ctx = new TestContext
    import ctx._

    def check(encoding: String, content: String, expected: List[String])
             (implicit pos: Position): () => Future[Unit] = { () =>

      val readF = js.Promise.resolve[js.Array[ViewerFileLine]](js.Array(content.split('\n').map(c => ViewerFileLine(c, c.length)).toList: _*))

      //then
      fileReader.readNextLines.expects(viewport.height, viewport.position, encoding).returning(readF)

      //when
      inside(findComponents(renderer.root, encodingsPopup)) {
        case List(c) => c.props.asInstanceOf[EncodingsPopupProps].onApply(encoding)
      }

      //then
      eventually(assertViewerContent(renderer.root, props, expected, hasEncodingsPopup = true))
    }

    eventually {
      assertViewerContent(renderer.root, props, List(
        "test ",
        "file content"
      ))
    }.flatMap { _ =>
      findComponentProps(renderer.root, viewerInput, plain = true).onKeypress("f8")
      eventually(findComponents(renderer.root, encodingsPopup).head)
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
            inside(findComponents(renderer.root, encodingsPopup)) {
              case List(c) => c.props.asInstanceOf[EncodingsPopupProps].onClose()
            }
            findComponents(renderer.root, encodingsPopup) should be (empty)
            ()
          }
        }
      ).foldLeft(Future.unit)((res, f) => res.flatMap(_ => f())).map(_ => Succeeded)
    }
  }

  it should "re-load prev page if at the end when onKeypress(down)" in {
    //given
    val inputRef = raw.React.createRef()
    val fileReader = new ViewerFileReader
    val setViewport = mockFunction[Option[ViewerFileViewport], Unit]
    var props = {
      val p = getViewerContentProps(inputRef, fileReader, setViewport)
      p.copy(viewport = p.viewport.copy(size = 10))
    }
    var viewport = props.viewport
    val readF = js.Promise.resolve[js.Array[ViewerFileLine]](js.Array("1\n2\n3\n4\n5\n".split('\n').map(c => ViewerFileLine(c, c.length + 1)).toList: _*))
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
      val resF = js.Promise.resolve[js.Array[ViewerFileLine]](js.Array("2\n3\n4\n5\n\n".split('\n').map(c => ViewerFileLine(c, c.length + 1)).toList: _*))
      fileReader.readPrevLines.expects(viewport.height, viewport.size, viewport.size, viewport.encoding).returning(resF)
  
      //when
      findComponentProps(renderer.root, viewerInput, plain = true).onKeypress("down")
  
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
        if (content.isEmpty) js.Promise.resolve[js.Array[ViewerFileLine]](js.Array[ViewerFileLine]())
        else js.Promise.resolve[js.Array[ViewerFileLine]](js.Array(content.split('\n').map(c => ViewerFileLine(c, c.length)).toList: _*))

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
      findComponentProps(renderer.root, viewerInput, plain = true).onKeypress(key)

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
          "iiiiiiiine 1",
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
    val inputRef = raw.React.createRef()
    val fileReader = new ViewerFileReader
    val setViewport = mockFunction[Option[ViewerFileViewport], Unit]
    var props = getViewerContentProps(inputRef, fileReader, setViewport, onKeypress = _ => true)
    var viewport = props.viewport
    val readF = js.Promise.resolve[js.Array[ViewerFileLine]](js.Array[ViewerFileLine]("test \nfile content".split('\n').map(c => ViewerFileLine(c, c.length)).toList: _*))
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
      findComponentProps(renderer.root, viewerInput, plain = true).onKeypress("f2")
  
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
      findComponentProps(renderer.root, viewerInput, plain = true).onKeypress("unknown")
  
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
      val read2F = js.Promise.resolve[js.Array[ViewerFileLine]](js.Array(ViewerFileLine(content2, content2.length)))

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
    val inputRef = raw.React.createRef()
    val fileReader = new ViewerFileReader
    val setViewport = mockFunction[Option[ViewerFileViewport], Unit]
    var props = getViewerContentProps(inputRef, fileReader, setViewport)
    var viewport = props.viewport
    val readF = js.Promise.resolve[js.Array[ViewerFileLine]](js.Array("test \nfile content\n".split('\n').map(c => ViewerFileLine(c, c.length + 1)).toList: _*))
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
    val inputRef = raw.React.createRef()
    val fileReader = new ViewerFileReader
    val setViewport = mockFunction[Option[ViewerFileViewport], Unit]
    var props = {
      val p = getViewerContentProps(inputRef, fileReader, setViewport)
      p.copy(viewport = p.viewport.copy(size = 0))
    }
    var viewport = props.viewport
    val readF = js.Promise.resolve[js.Array[ViewerFileLine]](js.Array[ViewerFileLine]("test content".split('\n').map(c => ViewerFileLine(c, c.length)).toList: _*))
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
  
  private def getViewerContentProps(inputRef: NativeRef,
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

    val theme = FileListTheme.defaultTheme

    assertComponents(result.children, List(
      <(viewerInput())(^.assertPlain[ViewerInputProps](inside(_) {
        case ViewerInputProps(inputRef, _, _) =>
          inputRef shouldBe props.inputRef
      }))(
        <.text(
          ^.rbWidth := props.viewport.width,
          ^.rbHeight := props.viewport.height,
          ^.rbStyle := ViewerController.contentStyle(theme),
          ^.rbWrap := false,
          ^.content := {
            if (content.isEmpty) ""
            else s"${content.mkString("\n")}\n"
          }
        )(),

        if (hasEncodingsPopup) Some(
          <(encodingsPopup)(^.assertPlain[EncodingsPopupProps](inside(_) {
            case EncodingsPopupProps(encoding, _, _) =>
              encoding shouldBe props.viewport.encoding
          }))()
        )
        else None
      )
    ))
  }
}
