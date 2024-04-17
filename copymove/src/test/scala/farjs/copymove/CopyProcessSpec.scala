package farjs.copymove

import farjs.copymove.CopyProcess._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem, FileTarget}
import farjs.ui.Dispatch
import farjs.ui.popup.MessageBoxProps
import farjs.ui.task.TaskAction
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs
import scommons.nodejs._
import scommons.nodejs.raw.Timers
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._
import scommons.react.test.raw.TestRenderer

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class CopyProcessSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  CopyProcess.copyProgressPopup = mockUiComponent("CopyProgressPopup")
  CopyProcess.fileExistsPopup = mockUiComponent("FileExistsPopup")
  CopyProcess.messageBoxComp = "MessageBox".asInstanceOf[ReactClass]
  
  CopyProcess.timers = literal(
    "setInterval" -> ({ (_: js.Function0[Any], _: Double) =>
      js.Dynamic.literal().asInstanceOf[Timeout]
    }: js.Function2[js.Function0[Any], Double, Timeout]),

    "clearInterval" -> ({ _: Timeout =>
    }: js.Function1[Timeout, Unit])
  ).asInstanceOf[Timers]

  //noinspection TypeAnnotation
  class Actions {
    val updateDir = mockFunction[Dispatch, Boolean, String, TaskAction]
    val mkDirs = mockFunction[List[String], Future[String]]
    val readDir = mockFunction[String, js.UndefOr[String], Future[FileListDir]]
    val delete = mockFunction[String, Seq[FileListItem], Future[Unit]]
    val writeFile = mockFunction[String, String,
      FileListItem => js.Promise[js.UndefOr[Boolean]], Future[js.UndefOr[FileTarget]]]
    val copyFile = mockFunction[String, FileListItem, Future[js.UndefOr[FileTarget]],
      Double => Future[Boolean], Future[Boolean]]

    val actions = new MockFileListActions(
      mkDirsMock = mkDirs,
      readDirMock = readDir,
      deleteMock = delete,
      writeFileMock = writeFile,
      copyFileMock = copyFile
    )
  }
  
  it should "increment time100ms every 100 ms." in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val from = FileListData(dispatch, actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val to = FileListData(toDispatch, toActions, FileListState())
    val setIntervalMock = mockFunction[js.Function0[Any], Double, Timeout]
    val clearIntervalMock = mockFunction[Timeout, Unit]
    val timers = literal("setInterval" -> setIntervalMock, "clearInterval" -> clearIntervalMock)
    val savedTimers = CopyProcess.timers
    CopyProcess.timers = timers.asInstanceOf[Timers]
    val props = CopyProcessProps(from, to, move = false, "/from/path", Nil, "/to/path", 12345, _ => (), () => ())
    val timerId = literal().asInstanceOf[Timeout]

    //then
    var onTimer: js.Function0[Any] = null
    setIntervalMock.expects(*, 100).onCall { (callback: js.Function0[Any], _) =>
      onTimer = callback
      timerId
    }
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))
    
    //when & then
    for (_ <- 1 to 10) {
      onTimer()
    }
    findComponentProps(renderer.root, copyProgressPopup).timeSeconds shouldBe 1
    
    //when & then
    for (_ <- 1 to 10) {
      onTimer()
    }
    findComponentProps(renderer.root, copyProgressPopup).timeSeconds shouldBe 2
    
    //then
    clearIntervalMock.expects(timerId)
    
    //when
    TestRenderer.act { () =>
      renderer.unmount()
    }
    
    //cleanup
    CopyProcess.timers = savedTimers
    Succeeded
  }

  it should "not increment time100ms when cancel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val from = FileListData(dispatch, actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val to = FileListData(toDispatch, toActions, FileListState())
    val setIntervalMock = mockFunction[js.Function0[Any], Double, Timeout]
    val clearIntervalMock = mockFunction[Timeout, Unit]
    val timers = literal("setInterval" -> setIntervalMock, "clearInterval" -> clearIntervalMock)
    val savedTimers = CopyProcess.timers
    CopyProcess.timers = timers.asInstanceOf[Timers]
    val props = CopyProcessProps(from, to, move = false, "/from/path", Nil, "/to/path", 12345, _ => (), () => ())
    val timerId = literal().asInstanceOf[Timeout]

    //then
    var onTimer: js.Function0[Any] = null
    setIntervalMock.expects(*, 100).onCall { (callback: js.Function0[Any], _) =>
      onTimer = callback
      timerId
    }
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))
    val progressProps = findComponentProps(renderer.root, copyProgressPopup)
    progressProps.onCancel()

    //when & then
    onTimer()
    findComponentProps(renderer.root, copyProgressPopup).timeSeconds shouldBe 0
    
    //cleanup
    clearIntervalMock.expects(timerId)
    TestRenderer.act { () =>
      renderer.unmount()
    }
    CopyProcess.timers = savedTimers
    Succeeded
  }

  it should "call onDone when YES action in cancel popup" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val to = FileListData(toDispatch, toActions.actions, FileListState())
    val item = FileListItem("file 1")
    val props = CopyProcessProps(from, to, move = false, "/from/path", List(
      (item, "newName"),
      (FileListItem("file 2"), "file 2")
    ), "/to/path", 12345, onTopItem, onDone)
    val p = Promise[Boolean]()

    var onProgressFn: Double => Future[Boolean] = null
    toActions.writeFile.expects("/to/path", "newName", *).returning(Future.successful(js.undefined: js.UndefOr[FileTarget]))
    actions.copyFile.expects("/from/path", item, *, *).onCall {
      (_, _, _, onProgress) =>
        onProgressFn = onProgress
        p.future
    }
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))
    eventually {
      onProgressFn should not be null
    }.flatMap { _ =>
      val progressProps = findComponentProps(renderer.root, copyProgressPopup)
      progressProps.onCancel()
      val cancelProps = inside(findComponents(renderer.root, messageBoxComp)) {
        case List(msgBox) => msgBox.props.asInstanceOf[MessageBoxProps]
      }
      val resultF = onProgressFn(123)
      
      //then
      onTopItem.expects(*).never()
      var onDoneCalled = false
      onDone.expects().onCall { () =>
        onDoneCalled = true
      }

      //when
      cancelProps.actions.head.onAction()

      //then
      findComponents(renderer.root, messageBoxComp) shouldBe Nil
      resultF.flatMap { res =>
        res shouldBe false

        //complete
        p.success(false)
        p.future.flatMap(_ => eventually(onDoneCalled shouldBe true)).map(_ => Succeeded)
      }
    }
  }

  it should "hide cancel popup when NO action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val to = FileListData(toDispatch, toActions, FileListState())
    val props = CopyProcessProps(from, to, move = false,
      "/from/path", Nil, "/to/path", 12345, _ => (), () => ())
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))
    val progressProps = findComponentProps(renderer.root, copyProgressPopup)
    progressProps.onCancel()
    val cancelProps = inside(findComponents(renderer.root, messageBoxComp)) {
      case List(msgBox) => msgBox.props.asInstanceOf[MessageBoxProps]
    }
    
    //when
    cancelProps.actions.last.onAction()
    
    //then
    findComponents(renderer.root, messageBoxComp) should be (empty)
  }

  it should "render cancel popup when onCancel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val to = FileListData(toDispatch, toActions, FileListState())
    val props = CopyProcessProps(from, to, move = false,
      "/from/path", Nil, "/to/path", 12345, _ => (), () => ())
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))
    val progressProps = findComponentProps(renderer.root, copyProgressPopup)

    //when
    progressProps.onCancel()

    //then
    val currTheme = DefaultTheme
    inside(renderer.root.children.toList) { case List(_, cancel) =>
      assertNativeComponent(cancel, <(messageBoxComp)(^.assertPlain[MessageBoxProps](inside(_) {
        case MessageBoxProps(title, message, resActions, style) =>
          title shouldBe "Operation has been interrupted"
          message shouldBe "Do you really want to cancel it?"
          inside(resActions.toList) { case List(yes, no) =>
            yes.label shouldBe "YES"
            no.label shouldBe "NO"
          }
          style shouldBe currTheme.popup.error
      }))())
    }
  }

  it should "call onDone when onCancel in FileExistsPopup" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val to = FileListData(toDispatch, toActions.actions, FileListState())
    val item = FileListItem("file 1")
    val props = CopyProcessProps(from, to, move = false, "/from/path", List(
      (item, "newName")
    ), "/to/path", 12345, onTopItem, onDone)
    val p = Promise[Boolean]()

    var onExistsFn: FileListItem => js.Promise[js.UndefOr[Boolean]] = null
    toActions.writeFile.expects("/to/path", "newName", *).onCall {
      (_, _, onExists) =>
        onExistsFn = onExists
        Future.successful(js.undefined: js.UndefOr[FileTarget])
    }
    actions.copyFile.expects("/from/path", item, *, *).returning(p.future)
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))
    
    eventually(onExistsFn should not be null).flatMap { _ =>
      val existsF = onExistsFn(FileListItem("existing.file"))
      val existsProps = findComponentProps(renderer.root, fileExistsPopup)
      
      //then
      onTopItem.expects(*).never()
      var onDoneCalled = false
      onDone.expects().onCall { () =>
        onDoneCalled = true
      }
      
      //when
      existsProps.onCancel()
      
      //then
      findProps(renderer.root, fileExistsPopup) should be (empty)
      existsF.toFuture.flatMap { res =>
        res shouldBe js.undefined

        //complete
        p.success(false)
        p.future.flatMap(_ => eventually(onDoneCalled shouldBe true)).map(_ => Succeeded)
      }
    }
  }

  it should "skip existing file when skip action" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val to = FileListData(toDispatch, toActions.actions, FileListState())
    val item = FileListItem("file 1")
    val props = CopyProcessProps(from, to, move = false, "/from/path", List(
      (item, "newName")
    ), "/to/path", 12345, onTopItem, onDone)
    val p = Promise[Boolean]()

    var onExistsFn: FileListItem => js.Promise[js.UndefOr[Boolean]] = null
    toActions.writeFile.expects("/to/path", "newName", *).onCall {
      (_, _, onExists) =>
        onExistsFn = onExists
        Future.successful(js.undefined: js.UndefOr[FileTarget])
    }
    actions.copyFile.expects("/from/path", item, *, *).returning(p.future)
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))
    
    eventually(onExistsFn should not be null).flatMap { _ =>
      val existsF = onExistsFn(FileListItem("existing.file"))
      val existsProps = findComponentProps(renderer.root, fileExistsPopup)
      
      //when
      TestRenderer.act { () =>
        existsProps.onAction(FileExistsAction.Skip)
      }

      //then
      findProps(renderer.root, fileExistsPopup) should be (empty)
      existsF.toFuture.flatMap { res =>
        res shouldBe js.undefined

        //then
        onTopItem.expects(*).never()
        var onDoneCalled = false
        onDone.expects().onCall { () =>
          onDoneCalled = true
        }
        
        //complete
        p.success(true)
        p.future.flatMap(_ => eventually(onDoneCalled shouldBe true)).map(_ => Succeeded)
      }
    }
  }

  it should "append to existing file when append action" in {
    //given
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val to = FileListData(toDispatch, toActions.actions, FileListState())
    val item = FileListItem("file 1")
    val props = CopyProcessProps(from, to, move = false, "/from/path", List(
      (item, "newName")
    ), "/to/path", 12345, _ => (), onDone)
    val p = Promise[Boolean]()

    var onExistsFn: FileListItem => js.Promise[js.UndefOr[Boolean]] = null
    toActions.writeFile.expects("/to/path", "newName", *).onCall {
      (_, _, onExists) =>
        onExistsFn = onExists
        Future.successful(js.undefined: js.UndefOr[FileTarget])
    }
    actions.copyFile.expects("/from/path", item, *, *).returning(p.future)
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))
    
    eventually(onExistsFn should not be null).flatMap { _ =>
      val existsF = onExistsFn(FileListItem("existing.file"))
      val existsProps = findComponentProps(renderer.root, fileExistsPopup)
      
      //when
      TestRenderer.act { () =>
        existsProps.onAction(FileExistsAction.Append)
      }

      //then
      findProps(renderer.root, fileExistsPopup) should be (empty)
      existsF.toFuture.flatMap { res =>
        res shouldBe false

        //then
        var onDoneCalled = false
        onDone.expects().onCall { () =>
          onDoneCalled = true
        }

        //complete
        p.success(false)
        p.future.flatMap(_ => eventually(onDoneCalled shouldBe true)).map(_ => Succeeded)
      }
    }
  }

  it should "pause copy process when asking for existing file action" in {
    //given
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val to = FileListData(toDispatch, toActions.actions, FileListState())
    val item = FileListItem("file 1")
    val props = CopyProcessProps(from, to, move = false, "/from/path", List(
      (item, "newName")
    ), "/to/path", 12345, _ => (), onDone)
    val p = Promise[Boolean]()

    var onExistsFn: FileListItem => js.Promise[js.UndefOr[Boolean]] = null
    toActions.writeFile.expects("/to/path", "newName", *).onCall {
      (_, _, onExists) =>
        onExistsFn = onExists
        Future.successful(js.undefined: js.UndefOr[FileTarget])
    }
    actions.copyFile.expects("/from/path", item, *, *).returning(p.future)
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))
    
    eventually {
      onExistsFn should not be null
    }.flatMap { _ =>
      //when
      val existsF = onExistsFn(FileListItem("existing.file")).toFuture
      
      //then
      implicit val patienceConfig: PatienceConfig = PatienceConfig(
        timeout = scaled(Span(1, Seconds)),
        interval = scaled(Span(100, Millis))
      )
      val resultF = eventually(existsF.isCompleted shouldBe true)
      resultF.failed.flatMap { _ =>
        //when
        val existsProps = findComponentProps(renderer.root, fileExistsPopup)
        TestRenderer.act { () =>
          existsProps.onAction(FileExistsAction.Overwrite)
        }
        findProps(renderer.root, fileExistsPopup) should be (empty)

        //then
        existsF.flatMap { res =>
          res shouldBe true

          //then
          var onDoneCalled = false
          onDone.expects().onCall { () =>
            onDoneCalled = true
          }

          //complete
          p.success(false)
          p.future.flatMap(_ => eventually(onDoneCalled shouldBe true)).map(_ => Succeeded)
        }
      }
    }
  }

  it should "do not ask for existing file action when canceled(unmount)" in {
    //given
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val to = FileListData(toDispatch, toActions.actions, FileListState())
    val item = FileListItem("file 1")
    val props = CopyProcessProps(from, to, move = false, "/from/path", List(
      (item, "newName")
    ), "/to/path", 12345, _ => (), onDone)
    val p = Promise[Boolean]()

    var onExistsFn: FileListItem => js.Promise[js.UndefOr[Boolean]] = null
    toActions.writeFile.expects("/to/path", "newName", *).onCall {
      (_, _, onExists) =>
        onExistsFn = onExists
        Future.successful(js.undefined: js.UndefOr[FileTarget])
    }
    actions.copyFile.expects("/from/path", item, *, *).returning(p.future)
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))
    
    eventually {
      onExistsFn should not be null
    }.flatMap { _ =>
      //when
      TestRenderer.act { () =>
        renderer.unmount()
      }
      
      //then
      val existsF = onExistsFn(FileListItem("existing.file")).toFuture
      existsF.flatMap { res =>
        res shouldBe js.undefined
        
        //then
        var onDoneCalled = false
        onDone.expects().onCall { () =>
          onDoneCalled = true
        }

        //complete
        p.success(false)
        p.future.flatMap(_ => eventually(onDoneCalled shouldBe true)).map(_ => Succeeded)
      }
    }
  }

  it should "do not ask for existing file action again if applied all action" in {
    //given
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val to = FileListData(toDispatch, toActions.actions, FileListState())
    val item1 = FileListItem("file 1")
    val item2 = FileListItem("file 2")
    val props = CopyProcessProps(from, to, move = false, "/from/path", List(
      (item1, "newName1"),
      (item2, "newName2")
    ), "/to/path", 12345, _ => (), onDone)

    val p1 = Promise[Boolean]()
    var onExistsFn1: FileListItem => js.Promise[js.UndefOr[Boolean]] = null
    toActions.writeFile.expects("/to/path", "newName1", *).onCall {
      (_, _, onExists) =>
        onExistsFn1 = onExists
        Future.successful(js.undefined: js.UndefOr[FileTarget])
    }
    actions.copyFile.expects("/from/path", item1, *, *).returning(p1.future)
    val p2 = Promise[Boolean]()
    var onExistsFn2: FileListItem => js.Promise[js.UndefOr[Boolean]] = null
    toActions.writeFile.expects("/to/path", "newName2", *).onCall {
      (_, _, onExists) =>
        onExistsFn2 = onExists
        Future.successful(js.undefined: js.UndefOr[FileTarget])
    }
    actions.copyFile.expects("/from/path", item2, *, *).returning(p2.future)
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))

    eventually(onExistsFn1 should not be null).flatMap { _ =>
      //given
      val existsF1 = onExistsFn1(FileListItem("existing.file1")).toFuture
      val existsProps = findComponentProps(renderer.root, fileExistsPopup)
      TestRenderer.act { () =>
        existsProps.onAction(FileExistsAction.All)
      }
      findProps(renderer.root, fileExistsPopup) should be (empty)
      existsF1.flatMap { res =>
        res shouldBe true
        p1.success(true)

        eventually(onExistsFn2 should not be null).flatMap { _ =>
          //when
          val existsF2 = onExistsFn2(FileListItem("existing.file2")).toFuture

          //then
          findProps(renderer.root, fileExistsPopup) should be (empty)
          existsF2.flatMap { res =>
            res shouldBe true
            
            //then
            var onDoneCalled = false
            onDone.expects().onCall { () =>
              onDoneCalled = true
            }

            //complete
            p2.success(false)
            p2.future.flatMap(_ => eventually(onDoneCalled shouldBe true)).map(_ => Succeeded)
          }
        }
      }
    }
  }

  it should "do not ask for existing file action again if applied skip all action" in {
    //given
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val to = FileListData(toDispatch, toActions.actions, FileListState())
    val item1 = FileListItem("file 1")
    val item2 = FileListItem("file 2")
    val props = CopyProcessProps(from, to, move = false, "/from/path", List(
      (item1, "newName1"),
      (item2, "newName2")
    ), "/to/path", 12345, _ => (), onDone)

    val p1 = Promise[Boolean]()
    var onExistsFn1: FileListItem => js.Promise[js.UndefOr[Boolean]] = null
    toActions.writeFile.expects("/to/path", "newName1", *).onCall {
      (_, _, onExists) =>
        onExistsFn1 = onExists
        Future.successful(js.undefined: js.UndefOr[FileTarget])
    }
    actions.copyFile.expects("/from/path", item1, *, *).returning(p1.future)
    val p2 = Promise[Boolean]()
    var onExistsFn2: FileListItem => js.Promise[js.UndefOr[Boolean]] = null
    toActions.writeFile.expects("/to/path", "newName2", *).onCall {
      (_, _, onExists) =>
        onExistsFn2 = onExists
        Future.successful(js.undefined: js.UndefOr[FileTarget])
    }
    actions.copyFile.expects("/from/path", item2, *, *).returning(p2.future)
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))

    eventually(onExistsFn1 should not be null).flatMap { _ =>
      //given
      val existsF1 = onExistsFn1(FileListItem("existing.file1")).toFuture
      val existsProps = findComponentProps(renderer.root, fileExistsPopup)
      TestRenderer.act { () =>
        existsProps.onAction(FileExistsAction.SkipAll)
      }
      findProps(renderer.root, fileExistsPopup) should be (empty)
      existsF1.flatMap { res =>
        res shouldBe js.undefined
        p1.success(true)

        eventually(onExistsFn2 should not be null).flatMap { _ =>
          //when
          val existsF2 = onExistsFn2(FileListItem("existing.file2")).toFuture

          //then
          findProps(renderer.root, fileExistsPopup) should be (empty)
          existsF2.flatMap { res =>
            res shouldBe js.undefined
            
            //then
            var onDoneCalled = false
            onDone.expects().onCall { () =>
              onDoneCalled = true
            }

            //complete
            p2.success(false)
            p2.future.flatMap(_ => eventually(onDoneCalled shouldBe true)).map(_ => Succeeded)
          }
        }
      }
    }
  }

  it should "pause copy process when cancelling" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val to = FileListData(toDispatch, toActions.actions, FileListState())
    val item = FileListItem("file 1")
    val props = CopyProcessProps(from, to, move = false, "/from/path", List(
      (item, "newName")
    ), "/to/path", 12345, onTopItem, onDone)
    val p = Promise[Boolean]()

    var onProgressFn: Double => Future[Boolean] = null
    toActions.writeFile.expects("/to/path", "newName", *).returning(Future.successful(js.undefined: js.UndefOr[FileTarget]))
    actions.copyFile.expects("/from/path", item, *, *).onCall {
      (_, _, _, onProgress) =>
        onProgressFn = onProgress
        p.future
    }
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))
    
    eventually {
      onProgressFn should not be null
    }.flatMap { _ =>
      //when
      val progressProps = findComponentProps(renderer.root, copyProgressPopup)
      progressProps.onCancel()
      
      //then
      val progressF = onProgressFn(123)
      implicit val patienceConfig: PatienceConfig = PatienceConfig(
        timeout = scaled(Span(1, Seconds)),
        interval = scaled(Span(100, Millis))
      )
      val resultF = eventually(progressF.isCompleted shouldBe true)
      resultF.failed.flatMap { _ =>
        //when
        val cancelProps = inside(findComponents(renderer.root, messageBoxComp)) {
          case List(msgBox) => msgBox.props.asInstanceOf[MessageBoxProps]
        }
        cancelProps.actions.last.onAction()

        //then
        progressF.flatMap { res =>
          res shouldBe true

          //then
          onTopItem.expects(item)
          var onDoneCalled = false
          onDone.expects().onCall { () =>
            onDoneCalled = true
          }

          //when & then
          p.success(res)
          p.future.flatMap(_ => eventually(onDoneCalled shouldBe true)).map(_ => Succeeded)
        }
      }
    }
  }

  it should "not call onTopItem if cancelled when unmount" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val to = FileListData(toDispatch, toActions.actions, FileListState())
    val item = FileListItem("file 1")
    val props = CopyProcessProps(from, to, move = false, "/from/path", List(
      (item, "newName1"),
      (FileListItem("file 2"), "file 2")
    ), "/to/path", 12345, onTopItem, onDone)
    val p = Promise[Boolean]()

    var onProgressFn: Double => Future[Boolean] = null
    toActions.writeFile.expects("/to/path", "newName1", *).returning(Future.successful(js.undefined: js.UndefOr[FileTarget]))
    actions.copyFile.expects("/from/path", item, *, *).onCall {
      (_, _, _, onProgress) =>
        onProgressFn = onProgress
        p.future
    }
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))
    eventually {
      onProgressFn should not be null
    }.flatMap { _ =>
      //when
      TestRenderer.act { () =>
        renderer.unmount()
      }
      onProgressFn(123).flatMap { res =>
        //then
        res shouldBe false

        //then
        onTopItem.expects(*).never()
        var onDoneCalled = false
        onDone.expects().onCall { () =>
          onDoneCalled = true
        }

        //when & then
        p.success(res)
        p.future.flatMap(_ => eventually(onDoneCalled shouldBe true)).map(_ => Succeeded)
      }
    }
  }

  it should "dispatch actions when failure" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val to = FileListData(toDispatch, toActions.actions, FileListState())
    val item = FileListItem("file 1")
    val props = CopyProcessProps(from, to, move = false, "/from/path", List(
      (item, "newName")
    ), "/to/path", 12345, onTopItem, onDone)

    val p = Promise[Boolean]()
    toActions.writeFile.expects("/to/path", "newName", *).returning(Future.successful(js.undefined: js.UndefOr[FileTarget]))
    actions.copyFile.expects("/from/path", item, *, *).returning(p.future)
    testRender(withThemeContext(<(CopyProcess())(^.wrapped := props)()))

    //then
    onTopItem.expects(*).never()
    onDone.expects()
    var resultF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) { case action: TaskAction =>
        action.task.message shouldBe "Copy/Move Items"
        resultF = action.task.result.toFuture
      }
    }

    //when
    p.failure(new Exception("test error"))

    //then
    eventually {
      resultF should not be null
    }.flatMap(_ => resultF.failed).map { _ =>
      Succeeded
    }
  }

  it should "make target dir, copy file, update progress and call onDone" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val to = FileListData(toDispatch, toActions.actions, FileListState())
    val item = FileListItem.copy(FileListItem("file 1"))(size = 246)
    val dir = FileListItem("dir 1", isDir = true)
    val props = CopyProcessProps(from, to, move = false, "/from/path", List(
      (dir, "newName")
    ), "/to/path", 492, onTopItem, onDone)
    val dirList = FileListDir("/from/path/dir 1", isRoot = false, js.Array(item))
    val p = Promise[Boolean]()

    actions.readDir.expects("/from/path", "dir 1": js.UndefOr[String]).returning(Future.successful(dirList))
    toActions.mkDirs.expects(List("/to/path", "newName")).returning(Future.successful("/to/path/newName"))
    
    var onProgressFn: Double => Future[Boolean] = null
    toActions.writeFile.expects("/to/path/newName", item.name, *).returning(Future.successful(js.undefined: js.UndefOr[FileTarget]))
    actions.copyFile.expects("/from/path/dir 1", item, *, *).onCall {
      (_, _, _, onProgress) =>
        onProgressFn = onProgress
        p.future
    }
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))
    eventually {
      onProgressFn should not be null
    }.flatMap { _ =>
      //when
      onProgressFn(123).flatMap { res =>
        //then
        res shouldBe true
        assertCopyProgressPopup(renderer, props, "/newName", "file 1", "file 1", itemPercent = 50, totalPercent = 25)
  
        //then
        onTopItem.expects(dir)
        var onDoneCalled = false
        onDone.expects().onCall { () =>
          onDoneCalled = true
        }

        //when & then
        p.success(res)
        p.future.flatMap(_ => eventually(onDoneCalled shouldBe true)).map { _ =>
          assertCopyProgressPopup(renderer, props, "/newName", "file 1", "file 1", itemPercent = 50, totalPercent = 25)
        }
      }
    }
  }

  it should "make target dir, move file, update progress and call onDone" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val to = FileListData(toDispatch, toActions.actions, FileListState())
    val item = FileListItem.copy(FileListItem("file 1"))(size = 246)
    val dir = FileListItem("dir 1", isDir = true)
    val props = CopyProcessProps(from, to, move = true, "/from/path", List(
      (dir, "newName")
    ), "/to/path", 492, onTopItem, onDone)
    val dirList = FileListDir("/from/path/dir 1", isRoot = false, js.Array(item))
    val p = Promise[Boolean]()

    actions.readDir.expects("/from/path", "dir 1": js.UndefOr[String]).returning(Future.successful(dirList))
    toActions.mkDirs.expects(List("/to/path", "newName")).returning(Future.successful("/to/path/newName"))
    
    var onProgressFn: Double => Future[Boolean] = null
    toActions.writeFile.expects("/to/path/newName", item.name, *).returning(Future.successful(js.undefined: js.UndefOr[FileTarget]))
    actions.copyFile.expects("/from/path/dir 1", item, *, *).onCall {
      (_, _, _, onProgress) =>
        onProgressFn = onProgress
        p.future
    }
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))
    eventually {
      onProgressFn should not be null
    }.flatMap { _ =>
      //when
      onProgressFn(123).flatMap { res =>
        //then
        res shouldBe true
        assertCopyProgressPopup(renderer, props, "/newName", "file 1", "file 1", itemPercent = 50, totalPercent = 25)
        actions.delete.expects("/from/path/dir 1", Seq(item)).returning(Future.unit)
        actions.delete.expects("/from/path", Seq(dir)).returning(Future.unit)
  
        //then
        onTopItem.expects(dir)
        var onDoneCalled = false
        onDone.expects().onCall { () =>
          onDoneCalled = true
        }

        //when & then
        p.success(res)
        p.future.flatMap(_ => eventually(onDoneCalled shouldBe true)).map { _ =>
          assertCopyProgressPopup(renderer, props, "/newName", "file 1", "file 1", itemPercent = 50, totalPercent = 25)
        }
      }
    }
  }

  it should "copy two files and update progress" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val to = FileListData(toDispatch, toActions.actions, FileListState())
    val item1 = FileListItem.copy(FileListItem("file 1"))(size = 246)
    val item2 = FileListItem.copy(FileListItem("file 2"))(size = 123)
    val props = CopyProcessProps(from, to, move = false, "/from/path", List(
      (item1, "newName1"),
      (item2, "newName2")
    ), "/to/path", 492, onTopItem, onDone)
    
    val p1 = Promise[Boolean]()
    var onProgressFn1: Double => Future[Boolean] = null
    toActions.writeFile.expects("/to/path", "newName1", *).returning(Future.successful(js.undefined: js.UndefOr[FileTarget]))
    actions.copyFile.expects("/from/path", item1, *, *).onCall {
      (_, _, _, onProgress) =>
        onProgressFn1 = onProgress
        p1.future
    }
    val p2 = Promise[Boolean]()
    var onProgressFn2: Double => Future[Boolean] = null
    toActions.writeFile.expects("/to/path", "newName2", *).returning(Future.successful(js.undefined: js.UndefOr[FileTarget]))
    actions.copyFile.expects("/from/path", item2, *, *).onCall {
      (_, _, _, onProgress) =>
        onProgressFn2 = onProgress
        p2.future
    }
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))
    eventually {
      onProgressFn1 should not be null
    }.flatMap { _ =>
      //then
      onTopItem.expects(item1)
      
      //when
      onProgressFn1(123).flatMap { res =>
        //then
        res shouldBe true
        assertCopyProgressPopup(renderer, props, "", "file 1", "newName1", itemPercent = 50, totalPercent = 25)

        p1.success(res)
        p1.future.flatMap(_ => eventually {
          onProgressFn2 should not be null
        }).flatMap { _ =>
          //then
          onTopItem.expects(item2)

          //when
          onProgressFn2(123).flatMap { res =>
            //then
            res shouldBe true
            assertCopyProgressPopup(renderer, props, "", "file 2", "newName2", itemPercent = 100, totalPercent = 50)
            
            //then
            var onDoneCalled = false
            onDone.expects().onCall { () =>
              onDoneCalled = true
            }

            //when & then
            p2.success(res)
            p2.future.flatMap(_ => eventually(onDoneCalled shouldBe true)).map { _ =>
              assertCopyProgressPopup(renderer, props, "", "file 2", "newName2", itemPercent = 100, totalPercent = 50)
            }
          }
        }
      }
    }
  }

  it should "move two files and update progress" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val from = FileListData(dispatch, actions.actions, FileListState())
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val to = FileListData(toDispatch, toActions.actions, FileListState())
    val item1 = FileListItem.copy(FileListItem("file 1"))(size = 246)
    val item2 = FileListItem.copy(FileListItem("file 2"))(size = 123)
    val props = CopyProcessProps(from, to, move = true, "/from/path", List(
      (item1, "newName1"),
      (item2, "newName2")
    ), "/to/path", 492, onTopItem, onDone)
    
    val p1 = Promise[Boolean]()
    var onProgressFn1: Double => Future[Boolean] = null
    toActions.writeFile.expects("/to/path", "newName1", *).returning(Future.successful(js.undefined: js.UndefOr[FileTarget]))
    actions.copyFile.expects("/from/path", item1, *, *).onCall {
      (_, _, _, onProgress) =>
        onProgressFn1 = onProgress
        p1.future
    }
    val p2 = Promise[Boolean]()
    var onProgressFn2: Double => Future[Boolean] = null
    toActions.writeFile.expects("/to/path", "newName2", *).returning(Future.successful(js.undefined: js.UndefOr[FileTarget]))
    actions.copyFile.expects("/from/path", item2, *, *).onCall {
      (_, _, _, onProgress) =>
        onProgressFn2 = onProgress
        p2.future
    }
    val renderer = createTestRenderer(withThemeContext(<(CopyProcess())(^.wrapped := props)()))
    eventually {
      onProgressFn1 should not be null
    }.flatMap { _ =>
      //then
      onTopItem.expects(item1)
      
      //when
      onProgressFn1(123).flatMap { res =>
        //then
        res shouldBe true
        assertCopyProgressPopup(renderer, props, "", "file 1", "newName1", itemPercent = 50, totalPercent = 25)
        actions.delete.expects("/from/path", Seq(item1)).returning(Future.unit)

        p1.success(res)
        p1.future.flatMap(_ => eventually {
          onProgressFn2 should not be null
        }).flatMap { _ =>
          //then
          onTopItem.expects(item2)

          //when
          onProgressFn2(123).flatMap { res =>
            //then
            res shouldBe true
            assertCopyProgressPopup(renderer, props, "", "file 2", "newName2", itemPercent = 100, totalPercent = 50)
            actions.delete.expects("/from/path", Seq(item2)).returning(Future.unit)
            
            //then
            var onDoneCalled = false
            onDone.expects().onCall { () =>
              onDoneCalled = true
            }

            //when & then
            p2.success(res)
            p2.future.flatMap(_ => eventually(onDoneCalled shouldBe true)).map { _ =>
              assertCopyProgressPopup(renderer, props, "", "file 2", "newName2", itemPercent = 100, totalPercent = 50)
            }
          }
        }
      }
    }
  }

  private def assertCopyProgressPopup(renderer: TestRenderer,
                                      props: CopyProcessProps,
                                      path: String,
                                      item: String,
                                      toName: String,
                                      itemPercent: Int,
                                      totalPercent: Int): Assertion = {

    renderer.update(withThemeContext(<(CopyProcess())(^.wrapped := props)()))

    assertTestComponent(renderer.root.children.head, copyProgressPopup) {
      case CopyProgressPopupProps(
        move,
        resItem,
        resTo,
        resItemPercent,
        resTotal,
        resTotalPercent,
        timeSeconds,
        leftSeconds,
        bytesPerSecond,
        _
      ) =>
        move shouldBe props.move
        resItem shouldBe item
        resTo shouldBe nodejs.path.join(props.toPath, path, toName)
        resItemPercent shouldBe itemPercent
        resTotal shouldBe props.total
        resTotalPercent shouldBe totalPercent
        timeSeconds shouldBe 0
        leftSeconds shouldBe 0
        bytesPerSecond shouldBe 0
    }
  }
}
