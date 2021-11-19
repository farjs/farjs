package farjs.filelist.copy

import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.copy.CopyProcess._
import farjs.filelist.copy.CopyProcessSpec._
import farjs.ui.popup.MessageBoxProps
import farjs.ui.theme.Theme
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs._
import scommons.nodejs.raw.Timers
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._
import scommons.react.test.raw.TestRenderer

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

class CopyProcessSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  CopyProcess.copyProgressPopup = mockUiComponent("CopyProgressPopup")
  CopyProcess.fileExistsPopup = mockUiComponent("FileExistsPopup")
  CopyProcess.messageBoxComp = mockUiComponent("MessageBox")
  
  CopyProcess.timers = new TimersMock {

    def setInterval(callback: js.Function0[Any], delay: Double): Timeout = {
      js.Dynamic.literal().asInstanceOf[Timeout]
    }

    def clearInterval(timeout: Timeout): Unit = {
    }
  }.asInstanceOf[Timers]
  
  it should "increment time100ms every 100 ms." in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val timers = mock[TimersMock]
    val savedTimers = CopyProcess.timers
    CopyProcess.timers = timers.asInstanceOf[Timers]
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", Nil, "/to/path", 12345, _ => (), () => ())
    val timerId = js.Dynamic.literal().asInstanceOf[Timeout]

    //then
    var onTimer: js.Function0[Any] = null
    (timers.setInterval _).expects(*, 100).onCall { (callback: js.Function0[Any], _) =>
      onTimer = callback
      timerId
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    
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
    (timers.clearInterval _).expects(timerId)
    
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
    val timers = mock[TimersMock]
    val savedTimers = CopyProcess.timers
    CopyProcess.timers = timers.asInstanceOf[Timers]
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", Nil, "/to/path", 12345, _ => (), () => ())
    val timerId = js.Dynamic.literal().asInstanceOf[Timeout]

    //then
    var onTimer: js.Function0[Any] = null
    (timers.setInterval _).expects(*, 100).onCall { (callback: js.Function0[Any], _) =>
      onTimer = callback
      timerId
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    val progressProps = findComponentProps(renderer.root, copyProgressPopup)
    progressProps.onCancel()

    //when & then
    onTimer()
    findComponentProps(renderer.root, copyProgressPopup).timeSeconds shouldBe 0
    
    //cleanup
    (timers.clearInterval _).expects(timerId)
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
    val actions = mock[FileListActions]
    val item = FileListItem("file 1")
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", List(
      (item, "newName"),
      (FileListItem("file 2"), "file 2")
    ), "/to/path", 12345, onTopItem, onDone)
    val p = Promise[Boolean]()

    var onProgressFn: Double => Future[Boolean] = null
    (actions.copyFile _).expects(List("/from/path"), item, List("/to/path"), "newName", *, *).onCall {
      (_, _, _, _, _, onProgress) =>
        onProgressFn = onProgress
        p.future
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    eventually {
      onProgressFn should not be null
    }.flatMap { _ =>
      val progressProps = findComponentProps(renderer.root, copyProgressPopup)
      progressProps.onCancel()
      val cancelProps = findComponentProps(renderer.root, messageBoxComp)
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
      findProps(renderer.root, messageBoxComp) shouldBe Nil
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
    val actions = mock[FileListActions]
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", Nil, "/to/path", 12345, _ => (), () => ())
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    val progressProps = findComponentProps(renderer.root, copyProgressPopup)
    progressProps.onCancel()
    val cancelProps = findComponentProps(renderer.root, messageBoxComp)
    
    //when
    cancelProps.actions.last.onAction()
    
    //then
    findProps(renderer.root, messageBoxComp) should be (empty)
  }

  it should "render cancel popup when onCancel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", Nil, "/to/path", 12345, _ => (), () => ())
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    val progressProps = findComponentProps(renderer.root, copyProgressPopup)

    //when
    progressProps.onCancel()

    //then
    inside(renderer.root.children.toList) { case List(_, cancel) =>
      assertTestComponent(cancel, messageBoxComp) {
        case MessageBoxProps(title, message, resActions, style) =>
          title shouldBe "Operation has been interrupted"
          message shouldBe "Do you really want to cancel it?"
          inside(resActions) { case List(yes, no) =>
            yes.label shouldBe "YES"
            no.label shouldBe "NO"
          }
          style shouldBe Theme.current.popup.error
      }
    }
  }

  it should "call onDone when onCancel in FileExistsPopup" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val item = FileListItem("file 1")
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", List(
      (item, "newName")
    ), "/to/path", 12345, onTopItem, onDone)
    val p = Promise[Boolean]()

    var onExistsFn: FileListItem => Future[Option[Boolean]] = null
    (actions.copyFile _).expects(List("/from/path"), item, List("/to/path"), "newName", *, *).onCall {
      (_, _, _, _, onExists, _) =>
        onExistsFn = onExists
        p.future
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    
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
      existsF.flatMap { res =>
        res shouldBe None

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
    val actions = mock[FileListActions]
    val item = FileListItem("file 1")
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", List(
      (item, "newName")
    ), "/to/path", 12345, onTopItem, onDone)
    val p = Promise[Boolean]()

    var onExistsFn: FileListItem => Future[Option[Boolean]] = null
    (actions.copyFile _).expects(List("/from/path"), item, List("/to/path"), "newName", *, *).onCall {
      (_, _, _, _, onExists, _) =>
        onExistsFn = onExists
        p.future
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    
    eventually(onExistsFn should not be null).flatMap { _ =>
      val existsF = onExistsFn(FileListItem("existing.file"))
      val existsProps = findComponentProps(renderer.root, fileExistsPopup)
      
      //when
      TestRenderer.act { () =>
        existsProps.onAction(FileExistsAction.Skip)
      }

      //then
      findProps(renderer.root, fileExistsPopup) should be (empty)
      existsF.flatMap { res =>
        res shouldBe None

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
    val actions = mock[FileListActions]
    val item = FileListItem("file 1")
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", List(
      (item, "newName")
    ), "/to/path", 12345, _ => (), onDone)
    val p = Promise[Boolean]()

    var onExistsFn: FileListItem => Future[Option[Boolean]] = null
    (actions.copyFile _).expects(List("/from/path"), item, List("/to/path"), "newName", *, *).onCall {
      (_, _, _, _, onExists, _) =>
        onExistsFn = onExists
        p.future
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    
    eventually(onExistsFn should not be null).flatMap { _ =>
      val existsF = onExistsFn(FileListItem("existing.file"))
      val existsProps = findComponentProps(renderer.root, fileExistsPopup)
      
      //when
      TestRenderer.act { () =>
        existsProps.onAction(FileExistsAction.Append)
      }

      //then
      findProps(renderer.root, fileExistsPopup) should be (empty)
      existsF.flatMap { res =>
        res shouldBe Some(false)

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
    val actions = mock[FileListActions]
    val item = FileListItem("file 1")
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", List(
      (item, "newName")
    ), "/to/path", 12345, _ => (), onDone)
    val p = Promise[Boolean]()

    var onExistsFn: FileListItem => Future[Option[Boolean]] = null
    (actions.copyFile _).expects(List("/from/path"), item, List("/to/path"), "newName", *, *).onCall {
      (_, _, _, _, onExists, _) =>
        onExistsFn = onExists
        p.future
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    
    eventually {
      onExistsFn should not be null
    }.flatMap { _ =>
      //when
      val existsF = onExistsFn(FileListItem("existing.file"))
      
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
          res shouldBe Some(true)

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
    val actions = mock[FileListActions]
    val item = FileListItem("file 1")
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", List(
      (item, "newName")
    ), "/to/path", 12345, _ => (), onDone)
    val p = Promise[Boolean]()

    var onExistsFn: FileListItem => Future[Option[Boolean]] = null
    (actions.copyFile _).expects(List("/from/path"), item, List("/to/path"), "newName", *, *).onCall {
      (_, _, _, _, onExists, _) =>
        onExistsFn = onExists
        p.future
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    
    eventually {
      onExistsFn should not be null
    }.flatMap { _ =>
      //when
      TestRenderer.act { () =>
        renderer.unmount()
      }
      
      //then
      val existsF = onExistsFn(FileListItem("existing.file"))
      existsF.flatMap { res =>
        res shouldBe None
        
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
    val actions = mock[FileListActions]
    val item1 = FileListItem("file 1")
    val item2 = FileListItem("file 2")
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", List(
      (item1, "newName1"),
      (item2, "newName2")
    ), "/to/path", 12345, _ => (), onDone)

    val p1 = Promise[Boolean]()
    var onExistsFn1: FileListItem => Future[Option[Boolean]] = null
    (actions.copyFile _).expects(List("/from/path"), item1, List("/to/path"), "newName1", *, *).onCall {
      (_, _, _, _, onExists, _) =>
        onExistsFn1 = onExists
        p1.future
    }
    val p2 = Promise[Boolean]()
    var onExistsFn2: FileListItem => Future[Option[Boolean]] = null
    (actions.copyFile _).expects(List("/from/path"), item2, List("/to/path"), "newName2", *, *).onCall {
      (_, _, _, _, onExists, _) =>
        onExistsFn2 = onExists
        p2.future
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())

    eventually(onExistsFn1 should not be null).flatMap { _ =>
      //given
      val existsF1 = onExistsFn1(FileListItem("existing.file1"))
      val existsProps = findComponentProps(renderer.root, fileExistsPopup)
      TestRenderer.act { () =>
        existsProps.onAction(FileExistsAction.All)
      }
      findProps(renderer.root, fileExistsPopup) should be (empty)
      existsF1.flatMap { res =>
        res shouldBe Some(true)
        p1.success(true)

        eventually(onExistsFn2 should not be null).flatMap { _ =>
          //when
          val existsF2 = onExistsFn2(FileListItem("existing.file2"))

          //then
          findProps(renderer.root, fileExistsPopup) should be (empty)
          existsF2.flatMap { res =>
            res shouldBe Some(true)
            
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
    val actions = mock[FileListActions]
    val item1 = FileListItem("file 1")
    val item2 = FileListItem("file 2")
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", List(
      (item1, "newName1"),
      (item2, "newName2")
    ), "/to/path", 12345, _ => (), onDone)

    val p1 = Promise[Boolean]()
    var onExistsFn1: FileListItem => Future[Option[Boolean]] = null
    (actions.copyFile _).expects(List("/from/path"), item1, List("/to/path"), "newName1", *, *).onCall {
      (_, _, _, _, onExists, _) =>
        onExistsFn1 = onExists
        p1.future
    }
    val p2 = Promise[Boolean]()
    var onExistsFn2: FileListItem => Future[Option[Boolean]] = null
    (actions.copyFile _).expects(List("/from/path"), item2, List("/to/path"), "newName2", *, *).onCall {
      (_, _, _, _, onExists, _) =>
        onExistsFn2 = onExists
        p2.future
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())

    eventually(onExistsFn1 should not be null).flatMap { _ =>
      //given
      val existsF1 = onExistsFn1(FileListItem("existing.file1"))
      val existsProps = findComponentProps(renderer.root, fileExistsPopup)
      TestRenderer.act { () =>
        existsProps.onAction(FileExistsAction.SkipAll)
      }
      findProps(renderer.root, fileExistsPopup) should be (empty)
      existsF1.flatMap { res =>
        res shouldBe None
        p1.success(true)

        eventually(onExistsFn2 should not be null).flatMap { _ =>
          //when
          val existsF2 = onExistsFn2(FileListItem("existing.file2"))

          //then
          findProps(renderer.root, fileExistsPopup) should be (empty)
          existsF2.flatMap { res =>
            res shouldBe None
            
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
    val actions = mock[FileListActions]
    val item = FileListItem("file 1")
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", List(
      (item, "newName")
    ), "/to/path", 12345, onTopItem, onDone)
    val p = Promise[Boolean]()

    var onProgressFn: Double => Future[Boolean] = null
    (actions.copyFile _).expects(List("/from/path"), item, List("/to/path"), "newName", *, *).onCall {
      (_, _, _, _, _, onProgress) =>
        onProgressFn = onProgress
        p.future
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    
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
        val cancelProps = findComponentProps(renderer.root, messageBoxComp)
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
    val actions = mock[FileListActions]
    val item = FileListItem("file 1")
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", List(
      (item, "newName1"),
      (FileListItem("file 2"), "file 2")
    ), "/to/path", 12345, onTopItem, onDone)
    val p = Promise[Boolean]()

    var onProgressFn: Double => Future[Boolean] = null
    (actions.copyFile _).expects(List("/from/path"), item, List("/to/path"), "newName1", *, *).onCall {
      (_, _, _, _, _, onProgress) =>
        onProgressFn = onProgress
        p.future
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
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
    val actions = mock[FileListActions]
    val item = FileListItem("file 1")
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", List(
      (item, "newName")
    ), "/to/path", 12345, onTopItem, onDone)

    val p = Promise[Boolean]()
    (actions.copyFile _).expects(List("/from/path"), item, List("/to/path"), "newName", *, *).returning(p.future)
    testRender(<(CopyProcess())(^.wrapped := props)())

    //then
    onTopItem.expects(*).never()
    onDone.expects()
    var resultF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      inside(action) { case action: FileListTaskAction =>
        action.task.message shouldBe "Copy/Move Items"
        resultF = action.task.future
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
    val actions = mock[FileListActions]
    val item = FileListItem("file 1", size = 246)
    val dir = FileListItem("dir 1", isDir = true)
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", List(
      (dir, "newName")
    ), "/to/path", 492, onTopItem, onDone)
    val dirList = FileListDir("/from/path/dir 1", isRoot = false, List(item))
    val p = Promise[Boolean]()

    (actions.readDir _).expects(Some("/from/path"), "dir 1").returning(Future.successful(dirList))
    (actions.mkDirs _).expects(List("/to/path", "newName")).returning(Future.unit)
    
    var onProgressFn: Double => Future[Boolean] = null
    (actions.copyFile _).expects(List("/from/path/dir 1"), item, List("/to/path", "newName"), item.name, *, *).onCall {
      (_, _, _, _, _, onProgress) =>
        onProgressFn = onProgress
        p.future
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
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
    val actions = mock[FileListActions]
    val item = FileListItem("file 1", size = 246)
    val dir = FileListItem("dir 1", isDir = true)
    val props = CopyProcessProps(dispatch, actions, move = true, "/from/path", List(
      (dir, "newName")
    ), "/to/path", 492, onTopItem, onDone)
    val dirList = FileListDir("/from/path/dir 1", isRoot = false, List(item))
    val p = Promise[Boolean]()

    (actions.readDir _).expects(Some("/from/path"), "dir 1").returning(Future.successful(dirList))
    (actions.mkDirs _).expects(List("/to/path", "newName")).returning(Future.unit)
    
    var onProgressFn: Double => Future[Boolean] = null
    (actions.copyFile _).expects(List("/from/path/dir 1"), item, List("/to/path", "newName"), item.name, *, *).onCall {
      (_, _, _, _, _, onProgress) =>
        onProgressFn = onProgress
        p.future
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    eventually {
      onProgressFn should not be null
    }.flatMap { _ =>
      //when
      onProgressFn(123).flatMap { res =>
        //then
        res shouldBe true
        assertCopyProgressPopup(renderer, props, "/newName", "file 1", "file 1", itemPercent = 50, totalPercent = 25)
        (actions.delete _).expects("/from/path/dir 1", Seq(item)).returning(Future.unit)
        (actions.delete _).expects("/from/path", Seq(dir)).returning(Future.unit)
  
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
    val actions = mock[FileListActions]
    val item1 = FileListItem("file 1", size = 246)
    val item2 = FileListItem("file 2", size = 123)
    val props = CopyProcessProps(dispatch, actions, move = false, "/from/path", List(
      (item1, "newName1"),
      (item2, "newName2")
    ), "/to/path", 492, onTopItem, onDone)
    
    val p1 = Promise[Boolean]()
    var onProgressFn1: Double => Future[Boolean] = null
    (actions.copyFile _).expects(List("/from/path"), item1, List("/to/path"), "newName1", *, *).onCall {
      (_, _, _, _, _, onProgress) =>
        onProgressFn1 = onProgress
        p1.future
    }
    val p2 = Promise[Boolean]()
    var onProgressFn2: Double => Future[Boolean] = null
    (actions.copyFile _).expects(List("/from/path"), item2, List("/to/path"), "newName2", *, *).onCall {
      (_, _, _, _, _, onProgress) =>
        onProgressFn2 = onProgress
        p2.future
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
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
    val actions = mock[FileListActions]
    val item1 = FileListItem("file 1", size = 246)
    val item2 = FileListItem("file 2", size = 123)
    val props = CopyProcessProps(dispatch, actions, move = true, "/from/path", List(
      (item1, "newName1"),
      (item2, "newName2")
    ), "/to/path", 492, onTopItem, onDone)
    
    val p1 = Promise[Boolean]()
    var onProgressFn1: Double => Future[Boolean] = null
    (actions.copyFile _).expects(List("/from/path"), item1, List("/to/path"), "newName1", *, *).onCall {
      (_, _, _, _, _, onProgress) =>
        onProgressFn1 = onProgress
        p1.future
    }
    val p2 = Promise[Boolean]()
    var onProgressFn2: Double => Future[Boolean] = null
    (actions.copyFile _).expects(List("/from/path"), item2, List("/to/path"), "newName2", *, *).onCall {
      (_, _, _, _, _, onProgress) =>
        onProgressFn2 = onProgress
        p2.future
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
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
        (actions.delete _).expects("/from/path", Seq(item1)).returning(Future.unit)

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
            (actions.delete _).expects("/from/path", Seq(item2)).returning(Future.unit)
            
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

    renderer.update(<(CopyProcess())(^.wrapped := props)())

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
        resTo shouldBe s"${props.toPath}$path/$toName"
        resItemPercent shouldBe itemPercent
        resTotal shouldBe props.total
        resTotalPercent shouldBe totalPercent
        timeSeconds shouldBe 0
        leftSeconds shouldBe 0
        bytesPerSecond shouldBe 0
    }
  }
}

object CopyProcessSpec {

  @JSExportAll
  trait TimersMock {

    def setInterval(callback: js.Function0[Any], delay: Double): Timeout

    def clearInterval(timeout: Timeout): Unit
  }
}
