package farjs.copymove

import farjs.copymove.CopyItemsStats._
import farjs.filelist._
import farjs.filelist.api.FileListItem
import farjs.ui.popup.StatusPopupProps
import farjs.ui.task.TaskAction
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

import scala.concurrent.{Future, Promise}

class CopyItemsStatsSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  CopyItemsStats.statusPopupComp = "StatusPopup".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class Actions {
    val scanDirs = mockFunction[String, Seq[FileListItem], (String, Seq[FileListItem]) => Boolean, Future[Boolean]]

    val actions = new MockFileListActions(
      scanDirsMock = scanDirs
    )
  }

  it should "call onCancel when onClose in popup" in {
    //given
    val onDone = mockFunction[Double, Unit]
    val onCancel = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = CopyItemsStatsProps(dispatch, actions.actions, "/folder", List(
      FileListItem("dir 1", isDir = true)
    ), "Copy", onDone, onCancel)
    actions.scanDirs.expects(props.fromPath, Seq(props.items.head), *).returning(Future.successful(false))

    val renderer = createTestRenderer(<(CopyItemsStats())(^.wrapped := props)())
    val statusPopup = inside(findComponents(renderer.root, statusPopupComp)) {
      case List(p) => p.props.asInstanceOf[StatusPopupProps]
    }

    //then
    onCancel.expects()

    //when
    statusPopup.onClose.foreach(_.apply())

    Succeeded
  }

  it should "call onDone with calculated total size" in {
    //given
    val onDone = mockFunction[Double, Unit]
    val onCancel = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = CopyItemsStatsProps(dispatch, actions.actions, "/folder", List(
      FileListItem("dir 1", isDir = true),
      FileListItem.copy(FileListItem("file 1"))(size = 10)
    ), "Move", onDone, onCancel)
    val p = Promise[Boolean]()
    actions.scanDirs.expects(props.fromPath, Seq(props.items.head), *).onCall { (_, _, onNextDir) =>
      onNextDir("/path", List(
        FileListItem("dir 2", isDir = true),
        FileListItem.copy(FileListItem("file 2"))(size = 123)
      ))
      p.future
    }
    val renderer = createTestRenderer(<(CopyItemsStats())(^.wrapped := props)())

    eventually {
      assertNativeComponent(renderer.root.children(0), <(statusPopupComp)(^.assertPlain[StatusPopupProps](inside(_) {
        case StatusPopupProps(text, title, onClose) =>
          text shouldBe "Calculating total size\ndir 1"
          title shouldBe "Move"
          onClose.isDefined shouldBe true
      }))())
    }.flatMap { _ =>
      var done = false
      
      //then
      onDone.expects(*).onCall { total: Double =>
        total shouldBe 133
        done = true
      }

      //when
      p.success(true)

      //then
      eventually {
        done shouldBe true
      }
    }
  }

  it should "handle cancel action when unmount" in {
    //given
    val onDone = mockFunction[Double, Unit]
    val onCancel = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = CopyItemsStatsProps(dispatch, actions.actions, "/folder", List(
      FileListItem("dir 1", isDir = true)
    ), "Copy", onDone, onCancel)
    val p = Promise[Boolean]()
    val resultF = p.future
    var onNextDirFn: (String, Seq[FileListItem]) => Boolean = null
    actions.scanDirs.expects(props.fromPath, Seq(props.items.head), *).onCall { (_, _, onNextDir) =>
      onNextDirFn = onNextDir
      resultF
    }
    
    val renderer = createTestRenderer(<(CopyItemsStats())(^.wrapped := props)())
    findComponents(renderer.root, statusPopupComp) should not be empty
    eventually {
      onNextDirFn should not be null
    }.flatMap { _ =>
      //when
      TestRenderer.act { () =>
        renderer.unmount()
      }
    
      //then
      onNextDirFn("/path", List(
        FileListItem("dir 2", isDir = true),
        FileListItem.copy(FileListItem("file 2"))(size = 123)
      )) shouldBe false

      p.success(false)
      resultF.map { res =>
        res shouldBe false
      }
    }
  }

  it should "dispatch actions when failure" in {
    //given
    val onDone = mockFunction[Double, Unit]
    val onCancel = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = CopyItemsStatsProps(dispatch, actions.actions, "/folder", List(
      FileListItem("dir 1", isDir = true)
    ), "Copy", onDone, onCancel)
    val p = Promise[Boolean]()
    actions.scanDirs.expects(props.fromPath, Seq(props.items.head), *).returning(p.future)
    
    val renderer = createTestRenderer(<(CopyItemsStats())(^.wrapped := props)())
    findComponents(renderer.root, statusPopupComp) should not be empty
    var resultF: Future[_] = null

    //then
    onCancel.expects()
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) { case action: TaskAction =>
        resultF = action.task.result.toFuture
        action.task.message shouldBe "Copy dir scan"
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
}
