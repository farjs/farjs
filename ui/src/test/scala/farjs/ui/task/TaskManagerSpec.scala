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
  }

  it should "fail if uiComponent is not set when render" in {
    //given
    val saved = TaskManager.uiComponent
    TaskManager.uiComponent = null
    val props = TaskManagerProps(None)

    // suppress intended error
    // see: https://github.com/facebook/react/issues/11098#issuecomment-412682721
    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = { _: js.Any =>
    }

    //when
    val JavaScriptException(error) = the[JavaScriptException] thrownBy {
      testRender(<(TaskManager())(^.wrapped := props)())
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
    val task = FutureTask("Fetching data", Promise[Unit]().future)
    val props = TaskManagerProps(Some(task))
    val renderer = createTestRenderer(<(TaskManager())(^.wrapped := props)())
    val uiProps = findComponentProps(renderer.root, TaskManager.uiComponent)
    assertUiProps(uiProps, expected(showLoading = true, status = Some(s"${task.message}\\.\\.\\.")))

    //when
    uiProps.onHideStatus()

    //then
    val updatedUiProps = findComponentProps(renderer.root, TaskManager.uiComponent)
    assertUiProps(updatedUiProps, expected(showLoading = true, status = None))
  }

  it should "set error to None when onCloseErrorPopup" in {
    //given
    val promise = Promise[Unit]()
    val task = FutureTask("Fetching data", promise.future)
    val props = TaskManagerProps(Some(task))
    val renderer = createTestRenderer(<(TaskManager())(^.wrapped := props)())
    val uiProps = findComponentProps(renderer.root, TaskManager.uiComponent)
    assertUiProps(uiProps, expected(showLoading = true, status = Some(s"${task.message}\\.\\.\\.")))

    val e = new Exception("Test error")
    promise.failure(e)

    eventually {
      val uiPropsV2 = findComponentProps(renderer.root, TaskManager.uiComponent)
      assertUiProps(uiPropsV2, expected(
        showLoading = false,
        status = Some(s"${task.message}\\.\\.\\.Done \\d+\\.\\d+ sec\\."),
        error = Some(e.toString),
        errorDetails = Some(TaskManager.printStackTrace(e))
      ))

      //when
      uiPropsV2.onCloseErrorPopup()

      //then
      val uiPropsV3 = findComponentProps(renderer.root, TaskManager.uiComponent)
      assertUiProps(uiPropsV3, expected(
        showLoading = false,
        status = Some(s"${task.message}\\.\\.\\.Done \\d+\\.\\d+ sec\\."),
        error = None,
        errorDetails = None
      ))
    }
  }

  it should "render loading and status" in {
    //given
    val task = FutureTask("Fetching data", Promise[Unit]().future)
    val props = TaskManagerProps(Some(task))
    val component = <(TaskManager())(^.wrapped := props)()

    //when
    val result = testRender(component)

    //then
    assertUiProps(findComponentProps(result, TaskManager.uiComponent), expected(
      showLoading = true,
      status = Some(s"${task.message}\\.\\.\\.")
    ))
  }

  it should "render error when exception" in {
    //given
    val promise = Promise[Unit]()
    val task = FutureTask("Fetching data", promise.future)
    val props = TaskManagerProps(Some(task))
    val renderer = createTestRenderer(<(TaskManager())(^.wrapped := props)())
    val uiProps = findComponentProps(renderer.root, TaskManager.uiComponent)
    assertUiProps(uiProps, expected(showLoading = true, status = Some(s"${task.message}...")))
    val e = new Exception("Test error")

    //when
    promise.failure(e)

    //then
    eventually {
      assertUiProps(findComponentProps(renderer.root, TaskManager.uiComponent), expected(
        showLoading = false,
        status = Some(s"${task.message}\\.\\.\\.Done \\d+\\.\\d+ sec\\."),
        error = Some(e.toString),
        errorDetails = Some(TaskManager.printStackTrace(e))
      ))
    }
  }

  it should "render status when task completed successfully" in {
    //given
    val promise = Promise[String]()
    val task = FutureTask("Fetching data", promise.future)
    val props = TaskManagerProps(Some(task))
    val renderer = createTestRenderer(<(TaskManager())(^.wrapped := props)())
    val uiProps = findComponentProps(renderer.root, TaskManager.uiComponent)
    assertUiProps(uiProps, expected(showLoading = true, status = Some(s"${task.message}...")))
    val resp = "Ok"

    //when
    promise.success(resp)

    //then
    eventually {
      assertUiProps(findComponentProps(renderer.root, TaskManager.uiComponent), expected(
        showLoading = false,
        status = Some(s"${task.message}\\.\\.\\.Done \\d+\\.\\d+ sec\\."),
        error = None,
        errorDetails = None
      ))
    }
  }

  it should "render status of already completed task" in {
    //given
    val task = FutureTask("Fetching data", Future.successful(()))
    val props = TaskManagerProps(Some(task))
    val renderer = createTestRenderer(<(TaskManager())(^.wrapped := props)())
    assertUiProps(findComponentProps(renderer.root, TaskManager.uiComponent), expected(
      showLoading = true,
      status = Some(s"${task.message}...")
    ))

    //when & then
    eventually {
      assertUiProps(findComponentProps(renderer.root, TaskManager.uiComponent), expected(
        showLoading = false,
        status = Some(s"${task.message}\\.\\.\\.Done \\d+\\.\\d+ sec\\."),
        error = None,
        errorDetails = None
      ))
    }
  }

  it should "render status of concurrent tasks" in {
    //given
    val promise1 = Promise[String]()
    val task1 = FutureTask("Fetching data 1", promise1.future)
    val renderer = createTestRenderer(<(TaskManager())(^.wrapped := TaskManagerProps(Some(task1)))())
    assertUiProps(findComponentProps(renderer.root, TaskManager.uiComponent), expected(
      showLoading = true,
      status = Some(s"${task1.message}...")
    ))

    val promise2 = Promise[String]()
    val task2 = FutureTask("Fetching data 2", promise2.future)
    
    TestRenderer.act { () =>
      renderer.update(<(TaskManager())(^.wrapped := TaskManagerProps(Some(task2)))())
    }
    assertUiProps(findComponentProps(renderer.root, TaskManager.uiComponent), expected(
      showLoading = true,
      status = Some(s"${task2.message}...")
    ))

    //when
    promise1.success("Ok")

    //then
    eventually {
      assertUiProps(findComponentProps(renderer.root, TaskManager.uiComponent), expected(
        showLoading = true,
        status = Some(s"${task1.message}\\.\\.\\.Done \\d+\\.\\d+ sec\\.")
      ))
    }.flatMap { _ =>
      //when
      promise2.success("Ok")

      //then
      eventually {
        assertUiProps(findComponentProps(renderer.root, TaskManager.uiComponent), expected(
          showLoading = false,
          status = Some(s"${task2.message}\\.\\.\\.Done \\d+\\.\\d+ sec\\.")
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

    inside(uiProps) { case TaskManagerUiProps(showLoading, status, _, error, errorDetails, _) =>
      showLoading shouldBe expectedProps.showLoading
      expectedProps.status match {
        case None => status shouldBe None
        case Some(statusRegex) => status.get should fullyMatch regex statusRegex
      }
      error shouldBe expectedProps.error
      errorDetails shouldBe expectedProps.errorDetails
    }
  }

  def expected(showLoading: Boolean,
               status: Option[String] = None,
               onHideStatus: () => Unit = () => (),
               error: Option[String] = None,
               errorDetails: Option[String] = None,
               onCloseErrorPopup: () => Unit = () => ()): TaskManagerUiProps = {

    TaskManagerUiProps(
      showLoading,
      status,
      onHideStatus,
      error,
      errorDetails,
      onCloseErrorPopup
    )
  }
}
