package farjs.ui.task

import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.test._

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

class TaskManagerSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  TaskManager.uiComponent = new FunctionComponent[TaskManagerUiProps] {
    override protected def render(props: Props): ReactElement = {
      <.>.empty
    }
  }.apply()

  it should "fail if uiComponent is not set when render" in {
    //given
    val saved = TaskManager.uiComponent
    TaskManager.uiComponent = null
    val props = TaskManagerProps(js.undefined)

    // suppress intended error
    // see: https://github.com/facebook/react/issues/11098#issuecomment-412682721
    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = { _: js.Any =>
    }

    //when
    val JavaScriptException(error) = the[JavaScriptException] thrownBy {
      testRender(<(TaskManager())(^.plain := props)())
    }

    //then
    s"$error" shouldBe "Error: TaskManager.uiComponent is not specified"
    
    //restore default
    js.Dynamic.global.console.error = savedConsoleError
    TaskManager.uiComponent = saved
    Succeeded
  }

  it should "set status to None when onHideStatus" in {
    //given
    val task = Task("Fetching data", Promise[Unit]().future)
    val props = TaskManagerProps(task)
    val renderer = createTestRenderer(<(TaskManager())(^.plain := props)())
    val uiProps = inside(findComponents(renderer.root, TaskManager.uiComponent)) {
      case List(ui) => ui.props.asInstanceOf[TaskManagerUiProps]
    }
    assertUiProps(uiProps, expected(showLoading = true, status = s"${task.message}\\.\\.\\."))

    //when
    uiProps.onHideStatus()

    //then
    val updatedUiProps = inside(findComponents(renderer.root, TaskManager.uiComponent)) {
      case List(ui) => ui.props.asInstanceOf[TaskManagerUiProps]
    }
    assertUiProps(updatedUiProps, expected(showLoading = true))
  }

  it should "set error to None when onCloseErrorPopup" in {
    //given
    val promise = Promise[Unit]()
    val task = Task("Fetching data", promise.future)
    val props = TaskManagerProps(task)
    val renderer = createTestRenderer(<(TaskManager())(^.plain := props)())
    val uiProps = inside(findComponents(renderer.root, TaskManager.uiComponent)) {
      case List(ui) => ui.props.asInstanceOf[TaskManagerUiProps]
    }
    assertUiProps(uiProps, expected(showLoading = true, status = s"${task.message}\\.\\.\\."))

    val e = new Exception("Test error")
    promise.failure(e)

    eventually {
      val uiPropsV2 = inside(findComponents(renderer.root, TaskManager.uiComponent)) {
        case List(ui) => ui.props.asInstanceOf[TaskManagerUiProps]
      }
      assertUiProps(uiPropsV2, expected(
        showLoading = false,
        status = s"${task.message}\\.\\.\\.Done \\d+\\.\\d+ sec\\.",
        error = e.toString,
        errorDetails = js.undefined
      ))

      //when
      uiPropsV2.onCloseErrorPopup()

      //then
      val uiPropsV3 = inside(findComponents(renderer.root, TaskManager.uiComponent)) {
        case List(ui) => ui.props.asInstanceOf[TaskManagerUiProps]
      }
      assertUiProps(uiPropsV3, expected(
        showLoading = false,
        status = s"${task.message}\\.\\.\\.Done \\d+\\.\\d+ sec\\."
      ))
    }
  }

  it should "render loading and status" in {
    //given
    val task = Task("Fetching data", Promise[Unit]().future)
    val props = TaskManagerProps(task)
    val component = <(TaskManager())(^.plain := props)()

    //when
    val result = testRender(component)

    //then
    val uiProps = inside(findComponents(result, TaskManager.uiComponent)) {
      case List(ui) => ui.props.asInstanceOf[TaskManagerUiProps]
    }
    assertUiProps(uiProps, expected(
      showLoading = true,
      status = s"${task.message}\\.\\.\\."
    ))
  }

  it should "render error when exception" in {
    //given
    val promise = Promise[Unit]()
    val task = Task("Fetching data", promise.future)
    val props = TaskManagerProps(task)
    val renderer = createTestRenderer(<(TaskManager())(^.plain := props)())
    val uiProps = inside(findComponents(renderer.root, TaskManager.uiComponent)) {
      case List(ui) => ui.props.asInstanceOf[TaskManagerUiProps]
    }
    assertUiProps(uiProps, expected(showLoading = true, status = s"${task.message}..."))
    val e = new Exception("Test error")

    //when
    promise.failure(e)

    //then
    eventually {
      val uiProps = inside(findComponents(renderer.root, TaskManager.uiComponent)) {
        case List(ui) => ui.props.asInstanceOf[TaskManagerUiProps]
      }
      assertUiProps(uiProps, expected(
        showLoading = false,
        status = s"${task.message}\\.\\.\\.Done \\d+\\.\\d+ sec\\.",
        error = e.toString,
        errorDetails = js.undefined
      ))
    }
  }

  it should "render status when task completed successfully" in {
    //given
    val promise = Promise[String]()
    val task = Task("Fetching data", promise.future)
    val props = TaskManagerProps(task)
    val renderer = createTestRenderer(<(TaskManager())(^.plain := props)())
    val uiProps = inside(findComponents(renderer.root, TaskManager.uiComponent)) {
      case List(ui) => ui.props.asInstanceOf[TaskManagerUiProps]
    }
    assertUiProps(uiProps, expected(showLoading = true, status = s"${task.message}..."))
    val resp = "Ok"

    //when
    promise.success(resp)

    //then
    eventually {
      val uiProps = inside(findComponents(renderer.root, TaskManager.uiComponent)) {
        case List(ui) => ui.props.asInstanceOf[TaskManagerUiProps]
      }
      assertUiProps(uiProps, expected(
        showLoading = false,
        status = s"${task.message}\\.\\.\\.Done \\d+\\.\\d+ sec\\."
      ))
    }
  }

  it should "render status of already completed task" in {
    //given
    val task = Task("Fetching data", Future.successful(()))
    val props = TaskManagerProps(task)
    val renderer = createTestRenderer(<(TaskManager())(^.plain := props)())
    val uiProps = inside(findComponents(renderer.root, TaskManager.uiComponent)) {
      case List(ui) => ui.props.asInstanceOf[TaskManagerUiProps]
    }
    assertUiProps(uiProps, expected(
      showLoading = true,
      status = s"${task.message}..."
    ))

    //when & then
    eventually {
      val uiProps = inside(findComponents(renderer.root, TaskManager.uiComponent)) {
        case List(ui) => ui.props.asInstanceOf[TaskManagerUiProps]
      }
      assertUiProps(uiProps, expected(
        showLoading = false,
        status = s"${task.message}\\.\\.\\.Done \\d+\\.\\d+ sec\\."
      ))
    }
  }

  it should "render status of concurrent tasks" in {
    //given
    val promise1 = Promise[String]()
    val task1 = Task("Fetching data 1", promise1.future)
    val renderer = createTestRenderer(<(TaskManager())(^.plain := TaskManagerProps(task1))())
    val uiProps = inside(findComponents(renderer.root, TaskManager.uiComponent)) {
      case List(ui) => ui.props.asInstanceOf[TaskManagerUiProps]
    }
    assertUiProps(uiProps, expected(
      showLoading = true,
      status = s"${task1.message}..."
    ))

    val promise2 = Promise[String]()
    val task2 = Task("Fetching data 2", promise2.future)
    
    TestRenderer.act { () =>
      renderer.update(<(TaskManager())(^.plain := TaskManagerProps(task2))())
    }
    val updatedUiProps = inside(findComponents(renderer.root, TaskManager.uiComponent)) {
      case List(ui) => ui.props.asInstanceOf[TaskManagerUiProps]
    }
    assertUiProps(updatedUiProps, expected(
      showLoading = true,
      status = s"${task2.message}..."
    ))

    //when
    promise1.success("Ok")

    //then
    eventually {
      val uiProps = inside(findComponents(renderer.root, TaskManager.uiComponent)) {
        case List(ui) => ui.props.asInstanceOf[TaskManagerUiProps]
      }
      assertUiProps(uiProps, expected(
        showLoading = true,
        status = s"${task1.message}\\.\\.\\.Done \\d+\\.\\d+ sec\\."
      ))
    }.flatMap { _ =>
      //when
      promise2.success("Ok")

      //then
      eventually {
        val uiProps = inside(findComponents(renderer.root, TaskManager.uiComponent)) {
          case List(ui) => ui.props.asInstanceOf[TaskManagerUiProps]
        }
        assertUiProps(uiProps, expected(
          showLoading = false,
          status = s"${task2.message}\\.\\.\\.Done \\d+\\.\\d+ sec\\."
        ))
      }
    }
  }

  it should "format duration in seconds properly" in {
    //when & then
    TaskManager.formatDuration(0L) shouldBe "0.000"
    TaskManager.formatDuration(3L) shouldBe "0.003"
    TaskManager.formatDuration(22L) shouldBe "0.022"
    TaskManager.formatDuration(33L) shouldBe "0.033"
    TaskManager.formatDuration(330L) shouldBe "0.330"
    TaskManager.formatDuration(333L) shouldBe "0.333"
    TaskManager.formatDuration(1132L) shouldBe "1.132"
    TaskManager.formatDuration(1333L) shouldBe "1.333"

    Succeeded
  }

  private def assertUiProps(uiProps: TaskManagerUiProps,
                            expectedProps: TaskManagerUiProps
                           )(implicit pos: Position): Assertion = {

    inside(uiProps) { case TaskManagerUiProps(showLoading, _, _, status, error, errorDetails) =>
      showLoading shouldBe expectedProps.showLoading
      expectedProps.status.toOption match {
        case None => status shouldBe js.undefined
        case Some(statusRegex) => status.get should fullyMatch regex statusRegex
      }
      error shouldBe expectedProps.error
      errorDetails shouldBe expectedProps.errorDetails
    }
  }

  private def expected(showLoading: Boolean,
                       status: js.UndefOr[String] = js.undefined,
                       error: js.UndefOr[String] = js.undefined,
                       errorDetails: js.UndefOr[String] = js.undefined,
                       onHideStatus: () => Unit = () => (),
                       onCloseErrorPopup: () => Unit = () => ()): TaskManagerUiProps = {

    TaskManagerUiProps(
      showLoading = showLoading,
      onHideStatus = onHideStatus,
      onCloseErrorPopup = onCloseErrorPopup,
      status = status,
      error = error,
      errorDetails = errorDetails
    )
  }
}
