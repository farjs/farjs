package farjs.copymove

import farjs.copymove.MoveProcess._
import farjs.filelist._
import farjs.filelist.api.FileListItem
import farjs.ui.popup.{MessageBoxProps, StatusPopupProps}
import farjs.ui.task.TaskAction
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import org.scalatest.Succeeded
import scommons.nodejs.path
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

import scala.concurrent.{Future, Promise}
import scala.scalajs.js

class MoveProcessSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  MoveProcess.statusPopupComp = "StatusPopup".asInstanceOf[ReactClass]
  MoveProcess.messageBoxComp = "MessageBox".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class FS {
    val rename = mockFunction[String, String, Future[Unit]]
    val existsSync = mockFunction[String, Boolean]

    val fs = new MockFS(
      renameMock = rename,
      existsSyncMock = existsSync
    )
  }

  it should "call onTopItem/onDone when success" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val fs = new FS
    MoveProcess.fs = fs.fs
    val item1 = FileListItem("dir 1", isDir = true)
    val item2 = FileListItem.copy(FileListItem("file 1"))(size = 10)
    val props = MoveProcessProps(dispatch, actions, "/from/path", List(
      (item1, "newName1"),
      (item2, item2.name)
    ), "/to/path", onTopItem, onDone)
    val p = Promise[Unit]()
    fs.rename.expects(path.join(props.fromPath, "dir 1"), path.join(props.toPath, "newName1")).returning(p.future)
    val renderer = createTestRenderer(withThemeContext(<(MoveProcess())(^.wrapped := props)()))

    eventually {
      assertNativeComponent(renderer.root.children(0), <(statusPopupComp)(^.assertPlain[StatusPopupProps](inside(_) {
        case StatusPopupProps(text, title, onClose) =>
          text shouldBe "Moving item\ndir 1"
          title shouldBe "Move"
          onClose.isDefined shouldBe true
      }))())
    }.flatMap { _ =>
      //then
      fs.existsSync.expects(path.join(props.toPath, "file 1")).returning(false)
      fs.rename.expects(path.join(props.fromPath, "file 1"), path.join(props.toPath, "file 1")).returning(p.future)
      onTopItem.expects(item1)
      onTopItem.expects(item2)
      var done = false
      onDone.expects().onCall { () =>
        done = true
      }

      //when
      p.success(())

      //then
      eventually {
        done shouldBe true
      }
    }
  }

  it should "dispatch actions when failure" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val fs = new FS
    MoveProcess.fs = fs.fs
    val item1 = FileListItem("dir 1", isDir = true)
    val item2 = FileListItem.copy(FileListItem("file 1"))(size = 10)
    val props = MoveProcessProps(dispatch, actions, "/from/path", List(
      (item1, item1.name),
      (item2, item2.name)
    ), "/to/path", onTopItem, onDone)
    val p = Promise[Unit]()
    fs.rename.expects(path.join(props.fromPath, "dir 1"), path.join(props.toPath, "dir 1")).returning(p.future)
    createTestRenderer(withThemeContext(<(MoveProcess())(^.wrapped := props)()))

    //then
    onDone.expects()
    var resultF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) { case action: TaskAction =>
        resultF = action.task.result.toFuture
        action.task.message shouldBe "Moving items"
      }
      ()
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

  it should "handle onClose action" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val fs = new FS
    MoveProcess.fs = fs.fs
    val item1 = FileListItem("dir 1", isDir = true)
    val item2 = FileListItem.copy(FileListItem("file 1"))(size = 10)
    val props = MoveProcessProps(dispatch, actions, "/from/path", List(
      (item1, item1.name),
      (item2, item2.name)
    ), "/to/path", onTopItem, onDone)
    val p = Promise[Unit]()
    fs.rename.expects(path.join(props.fromPath, "dir 1"), path.join(props.toPath, "dir 1")).returning(p.future)
    val renderer = createTestRenderer(withThemeContext(<(MoveProcess())(^.wrapped := props)()))

    eventually {
      assertNativeComponent(renderer.root.children(0), <(statusPopupComp)(^.assertPlain[StatusPopupProps](inside(_) {
        case StatusPopupProps(text, title, onClose) =>
          text shouldBe "Moving item\ndir 1"
          title shouldBe "Move"
          onClose.isDefined shouldBe true
      }))())
    }.flatMap { _ =>
      //when
      val popup = inside(findComponents(renderer.root, statusPopupComp)) {
        case List(p) => p.props.asInstanceOf[StatusPopupProps]
      }
      popup.onClose.foreach(_.apply())

      //then
      onTopItem.expects(item1)
      var done = false
      onDone.expects().onCall { () =>
        done = true
      }

      //when
      p.success(())
      eventually {
        done shouldBe true
      }
    }
  }

  it should "render item exists message and handle Cancel action" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val fs = new FS
    MoveProcess.fs = fs.fs
    val item1 = FileListItem.copy(FileListItem("file 1"))(size = 1)
    val item2 = FileListItem.copy(FileListItem("file 2"))(size = 10)
    val props = MoveProcessProps(dispatch, actions, "/from/path", List(
      (item1, item1.name),
      (item2, item2.name)
    ), "/to/path", onTopItem, onDone)
    val newPath = path.join(props.toPath, "file 1")
    fs.existsSync.expects(newPath).returning(true)
    val renderer = createTestRenderer(withThemeContext(<(MoveProcess())(^.wrapped := props)()))

    eventually {
      inside(renderer.root.children.toList) { case List(_, existsMessage) =>
        assertNativeComponent(existsMessage, <(messageBoxComp)(^.assertPlain[MessageBoxProps](inside(_) {
          case MessageBoxProps(title, message, resActions, style) =>
            title shouldBe "Warning"
            message shouldBe s"File already exists.\nDo you want to overwrite it's content?\n\n$newPath"
            inside(resActions.toList) { case List(overwrite, all, skip, skipAll, cancel) =>
              overwrite.label shouldBe "Overwrite"
              all.label shouldBe "All"
              skip.label shouldBe "Skip"
              skipAll.label shouldBe "Skip all"
              cancel.label shouldBe "Cancel"
              cancel.triggeredOnClose shouldBe true
            }
            style shouldBe DefaultTheme.popup.error
        }))())
      }
    }.flatMap { _ =>
      //then
      var done = false
      onDone.expects().onCall { () =>
        done = true
      }

      //when
      findComponents(renderer.root, messageBoxComp).head.props.asInstanceOf[MessageBoxProps].actions.last.onAction()

      //then
      findComponents(renderer.root, messageBoxComp) should be (empty)
      eventually {
        done shouldBe true
      }
    }
  }

  it should "render item exists message and handle Overwrite action" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val fs = new FS
    MoveProcess.fs = fs.fs
    val item1 = FileListItem.copy(FileListItem("file 1"))(size = 1)
    val item2 = FileListItem.copy(FileListItem("file 2"))(size = 10)
    val props = MoveProcessProps(dispatch, actions, "/from/path", List(
      (item1, item1.name),
      (item2, item2.name)
    ), "/to/path", onTopItem, onDone)
    val newPath1 = path.join(props.toPath, "file 1")
    fs.existsSync.expects(newPath1).returning(true)
    val renderer = createTestRenderer(withThemeContext(<(MoveProcess())(^.wrapped := props)()))

    eventually {
      inside(renderer.root.children.toList) { case List(_, existsMessage) =>
        assertNativeComponent(existsMessage, <(messageBoxComp)(^.assertPlain[MessageBoxProps](inside(_) {
          case MessageBoxProps(_, message, _, _) =>
            message shouldBe s"File already exists.\nDo you want to overwrite it's content?\n\n$newPath1"
        }))())
      }
    }.flatMap { _ =>
      //then
      fs.rename.expects(path.join(props.fromPath, "file 1"), newPath1).returning(Future.unit)
      onTopItem.expects(item1)
      
      //when
      findComponents(renderer.root, messageBoxComp).head.props.asInstanceOf[MessageBoxProps].actions.head.onAction()
      
      //then
      findComponents(renderer.root, messageBoxComp) should be (empty)
      val newPath2 = path.join(props.toPath, "file 2")
      fs.existsSync.expects(newPath2).returning(true)

      eventually {
        inside(renderer.root.children.toList) { case List(_, existsMessage) =>
          assertNativeComponent(existsMessage, <(messageBoxComp)(^.assertPlain[MessageBoxProps](inside(_) {
            case MessageBoxProps(_, message, _, _) =>
              message shouldBe s"File already exists.\nDo you want to overwrite it's content?\n\n$newPath2"
          }))())
        }
      }.flatMap { _ =>
        //then
        fs.rename.expects(path.join(props.fromPath, "file 2"), newPath2).returning(Future.unit)
        onTopItem.expects(item2)
        
        var done = false
        onDone.expects().onCall { () =>
          done = true
        }

        //when
        findComponents(renderer.root, messageBoxComp).head.props.asInstanceOf[MessageBoxProps].actions.head.onAction()

        //then
        findComponents(renderer.root, messageBoxComp) should be (empty)
        eventually {
          done shouldBe true
        }
      }
    }
  }

  it should "render item exists message and handle All action" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val fs = new FS
    MoveProcess.fs = fs.fs
    val item1 = FileListItem.copy(FileListItem("file 1"))(size = 1)
    val item2 = FileListItem.copy(FileListItem("file 2"))(size = 10)
    val props = MoveProcessProps(dispatch, actions, "/from/path", List(
      (item1, item1.name),
      (item2, item2.name)
    ), "/to/path", onTopItem, onDone)
    val newPath1 = path.join(props.toPath, "file 1")
    fs.existsSync.expects(newPath1).returning(true)
    val renderer = createTestRenderer(withThemeContext(<(MoveProcess())(^.wrapped := props)()))

    eventually {
      inside(renderer.root.children.toList) { case List(_, existsMessage) =>
        assertNativeComponent(existsMessage, <(messageBoxComp)(^.assertPlain[MessageBoxProps](inside(_) {
          case MessageBoxProps(_, message, _, _) =>
            message shouldBe s"File already exists.\nDo you want to overwrite it's content?\n\n$newPath1"
        }))())
      }
    }.flatMap { _ =>
      //then
      fs.rename.expects(path.join(props.fromPath, "file 1"), newPath1).returning(Future.unit)
      onTopItem.expects(item1)
      
      val newPath2 = path.join(props.toPath, "file 2")
      fs.existsSync.expects(newPath2).returning(true)
      fs.rename.expects(path.join(props.fromPath, "file 2"), newPath2).returning(Future.unit)
      onTopItem.expects(item2)
      
      var done = false
      onDone.expects().onCall { () =>
        done = true
      }
      
      //when
      findComponents(renderer.root, messageBoxComp).head.props.asInstanceOf[MessageBoxProps].actions(1).onAction()
      
      //then
      findComponents(renderer.root, messageBoxComp) should be (empty)
      eventually {
        done shouldBe true
      }
    }
  }

  it should "render item exists message and handle Skip action" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val fs = new FS
    MoveProcess.fs = fs.fs
    val item1 = FileListItem.copy(FileListItem("file 1"))(size = 1)
    val item2 = FileListItem.copy(FileListItem("file 2"))(size = 10)
    val props = MoveProcessProps(dispatch, actions, "/from/path", List(
      (item1, item1.name),
      (item2, item2.name)
    ), "/to/path", onTopItem, onDone)
    val newPath1 = path.join(props.toPath, "file 1")
    fs.existsSync.expects(newPath1).returning(true)
    val renderer = createTestRenderer(withThemeContext(<(MoveProcess())(^.wrapped := props)()))

    eventually {
      inside(renderer.root.children.toList) { case List(_, existsMessage) =>
        assertNativeComponent(existsMessage, <(messageBoxComp)(^.assertPlain[MessageBoxProps](inside(_) {
          case MessageBoxProps(_, message, _, _) =>
            message shouldBe s"File already exists.\nDo you want to overwrite it's content?\n\n$newPath1"
        }))())
      }
    }.flatMap { _ =>
      //when
      findComponents(renderer.root, messageBoxComp).head.props.asInstanceOf[MessageBoxProps].actions(2).onAction()

      //then
      findComponents(renderer.root, messageBoxComp) should be (empty)
      val newPath2 = path.join(props.toPath, "file 2")
      fs.existsSync.expects(newPath2).returning(true)

      eventually {
        inside(renderer.root.children.toList) { case List(_, existsMessage) =>
          assertNativeComponent(existsMessage, <(messageBoxComp)(^.assertPlain[MessageBoxProps](inside(_) {
            case MessageBoxProps(_, message, _, _) =>
              message shouldBe s"File already exists.\nDo you want to overwrite it's content?\n\n$newPath2"
          }))())
        }
      }.flatMap { _ =>
        var done = false
        onDone.expects().onCall { () =>
          done = true
        }

        //when
        findComponents(renderer.root, messageBoxComp).head.props.asInstanceOf[MessageBoxProps].actions(2).onAction()

        //then
        findComponents(renderer.root, messageBoxComp) should be (empty)
        eventually {
          done shouldBe true
        }
      }
    }
  }

  it should "render item exists message and handle Skip all action" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val fs = new FS
    MoveProcess.fs = fs.fs
    val item1 = FileListItem.copy(FileListItem("file 1"))(size = 1)
    val item2 = FileListItem.copy(FileListItem("file 2"))(size = 10)
    val item3 = FileListItem.copy(FileListItem("file 3"))(size = 11)
    val props = MoveProcessProps(dispatch, actions, "/from/path", List(
      (item1, item1.name),
      (item2, item2.name),
      (item3, item3.name)
    ), "/to/path", onTopItem, onDone)
    val newPath1 = path.join(props.toPath, "file 1")
    fs.existsSync.expects(newPath1).returning(true)
    val renderer = createTestRenderer(withThemeContext(<(MoveProcess())(^.wrapped := props)()))

    eventually {
      inside(renderer.root.children.toList) { case List(_, existsMessage) =>
        assertNativeComponent(existsMessage, <(messageBoxComp)(^.assertPlain[MessageBoxProps](inside(_) {
          case MessageBoxProps(_, message, _, _) =>
            message shouldBe s"File already exists.\nDo you want to overwrite it's content?\n\n$newPath1"
        }))())
      }
    }.flatMap { _ =>
      fs.existsSync.expects(path.join(props.toPath, "file 2")).returning(true)
      val newPath3 = path.join(props.toPath, "file 3")
      fs.existsSync.expects(newPath3).returning(false)
      fs.rename.expects(path.join(props.fromPath, "file 3"), newPath3).returning(Future.unit)
      onTopItem.expects(item3)

      var done = false
      onDone.expects().onCall { () =>
        done = true
      }

      //when
      findComponents(renderer.root, messageBoxComp).head.props.asInstanceOf[MessageBoxProps].actions(3).onAction()

      //then
      findComponents(renderer.root, messageBoxComp) should be (empty)
      eventually {
        done shouldBe true
      }
    }
  }
}
