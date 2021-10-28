package farjs.filelist.copy

import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist._
import farjs.filelist.api.FileListItem
import farjs.filelist.copy.MoveItems._
import farjs.ui.popup.StatusPopupProps
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.nodejs.{FS, path}
import scommons.react._
import scommons.react.test._

import scala.concurrent.{Future, Promise}

class MoveItemsSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  MoveItems.statusPopupComp = () => "StatusPopup".asInstanceOf[ReactClass]

  it should "call onTopItem/onDone when success" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val fs = mock[FS]
    MoveItems.fs = fs
    val item1 = FileListItem("dir 1", isDir = true)
    val item2 = FileListItem("file 1", size = 10)
    val props = MoveItemsProps(dispatch, actions, "/from/path", List(item1, item2), "/to/path", onTopItem, onDone)
    val p = Promise[Unit]()
    (fs.rename _).expects(path.join(props.fromPath, "dir 1"), path.join(props.toPath, "dir 1")).returning(p.future)
    val renderer = createTestRenderer(<(MoveItems())(^.wrapped := props)())

    eventually {
      assertTestComponent(renderer.root.children(0), statusPopupComp) {
        case StatusPopupProps(text, title, closable, _) =>
          text shouldBe "Moving item\ndir 1"
          title shouldBe "Move"
          closable shouldBe true
      }
    }.flatMap { _ =>
      //then
      (fs.rename _).expects(path.join(props.fromPath, "file 1"), path.join(props.toPath, "file 1")).returning(p.future)
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
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val fs = mock[FS]
    MoveItems.fs = fs
    val item1 = FileListItem("dir 1", isDir = true)
    val item2 = FileListItem("file 1", size = 10)
    val props = MoveItemsProps(dispatch, actions, "/from/path", List(item1, item2), "/to/path", onTopItem, onDone)
    val p = Promise[Unit]()
    (fs.rename _).expects(path.join(props.fromPath, "dir 1"), path.join(props.toPath, "dir 1")).returning(p.future)
    createTestRenderer(<(MoveItems())(^.wrapped := props)())

    //then
    onDone.expects()
    var resultF: Future[_] = null
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      resultF = action.task.future
      action.task.message shouldBe "Moving items"
    })

    //when
    p.failure(new Exception("test error"))
    
    //then
    eventually {
      resultF should not be null
    }.flatMap(_ => resultF.failed).map { _ =>
      Succeeded
    }
  }

  it should "handle cancel action" in {
    //given
    val onTopItem = mockFunction[FileListItem, Unit]
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val fs = mock[FS]
    MoveItems.fs = fs
    val item1 = FileListItem("dir 1", isDir = true)
    val item2 = FileListItem("file 1", size = 10)
    val props = MoveItemsProps(dispatch, actions, "/from/path", List(item1, item2), "/to/path", onTopItem, onDone)
    val p = Promise[Unit]()
    (fs.rename _).expects(path.join(props.fromPath, "dir 1"), path.join(props.toPath, "dir 1")).returning(p.future)
    val renderer = createTestRenderer(<(MoveItems())(^.wrapped := props)())

    eventually {
      assertTestComponent(renderer.root.children(0), statusPopupComp) {
        case StatusPopupProps(text, title, closable, _) =>
          text shouldBe "Moving item\ndir 1"
          title shouldBe "Move"
          closable shouldBe true
      }
    }.flatMap { _ =>
      //when
      findComponentProps(renderer.root, statusPopupComp).onClose()

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
}
