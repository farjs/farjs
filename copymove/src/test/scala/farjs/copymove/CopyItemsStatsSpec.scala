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
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

class CopyItemsStatsSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  CopyItemsStats.statusPopupComp = "StatusPopup".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class Actions {
    val scanDirs = mockFunction[String, js.Array[FileListItem], js.Function2[String, js.Array[FileListItem], Boolean], js.Promise[Boolean]]

    val actions = new MockFileListActions(
      scanDirsMock = scanDirs
    )
  }

  it should "call onCancel when onClose in popup" in {
    //given
    val onDone = mockFunction[Double, Unit]
    val onCancel = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = CopyItemsStatsProps(dispatch, actions.actions, "/folder", js.Array(
      FileListItem("dir 1", isDir = true)
    ), "Copy", onDone, onCancel)
    actions.scanDirs.expects(props.fromPath, *, *).onCall { (_, resItems, _) =>
      resItems.toList shouldBe List(props.items.head)
      js.Promise.resolve[Boolean](false)
    }

    val renderer = createTestRenderer(<(CopyItemsStats())(^.plain := props)())
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
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = CopyItemsStatsProps(dispatch, actions.actions, "/folder", js.Array(
      FileListItem("dir 1", isDir = true),
      FileListItem.copy(FileListItem("file 1"))(size = 10)
    ), "Move", onDone, onCancel)
    val p = Promise[Boolean]()
    actions.scanDirs.expects(props.fromPath, *, *).onCall { (_, resItems, onNextDir) =>
      resItems.toList shouldBe Seq(props.items.head)
      onNextDir("/path", js.Array(
        FileListItem("dir 2", isDir = true),
        FileListItem.copy(FileListItem("file 2"))(size = 123)
      ))
      p.future.toJSPromise
    }
    val renderer = createTestRenderer(<(CopyItemsStats())(^.plain := props)())

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
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = CopyItemsStatsProps(dispatch, actions.actions, "/folder", js.Array(
      FileListItem("dir 1", isDir = true)
    ), "Copy", onDone, onCancel)
    val p = Promise[Boolean]()
    val resultF = p.future
    var onNextDirFn: js.Function2[String, js.Array[FileListItem], Boolean] = null
    actions.scanDirs.expects(props.fromPath, *, *).onCall { (_, resItems, onNextDir) =>
      resItems.toList shouldBe Seq(props.items.head)
      onNextDirFn = onNextDir
      resultF.toJSPromise
    }
    
    val renderer = createTestRenderer(<(CopyItemsStats())(^.plain := props)())
    findComponents(renderer.root, statusPopupComp) should not be empty
    eventually {
      onNextDirFn should not be null
    }.flatMap { _ =>
      //when
      TestRenderer.act { () =>
        renderer.unmount()
      }
    
      //then
      onNextDirFn("/path", js.Array(
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
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val props = CopyItemsStatsProps(dispatch, actions.actions, "/folder", js.Array(
      FileListItem("dir 1", isDir = true)
    ), "Copy", onDone, onCancel)
    val p = Promise[Boolean]()
    actions.scanDirs.expects(props.fromPath, *, *).onCall { (_, resItems, _) =>
      resItems.toList shouldBe Seq(props.items.head)
      p.future.toJSPromise
    }
    
    val renderer = createTestRenderer(<(CopyItemsStats())(^.plain := props)())
    findComponents(renderer.root, statusPopupComp) should not be empty
    var resultF: Future[_] = null

    //then
    onCancel.expects()
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) { case action: TaskAction =>
        resultF = action.task.result.toFuture
        action.task.message shouldBe "Copy dir scan"
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
}
