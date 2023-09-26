package farjs.ui.task

import scommons.nodejs.test._

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

class TaskErrorSpec extends TestSpec {

  it should "return error if JavaScriptException in errorHandler" in {
    //given
    val currLogger = TaskError.logger
    val logger = mockFunction[String, Unit]
    TaskError.logger = logger
    val ex = JavaScriptException("test error")
    val value = ex
    val stackTrace = TaskError.printStackTrace(ex, sep = " ")

    //then
    logger.expects(stackTrace)
    
    //when
    val TaskError(error, errorDetails) = TaskError.errorHandler(value)

    //then
    error shouldBe "test error"
    errorDetails shouldBe stackTrace
    
    //cleanup
    TaskError.logger = currLogger
  }

  it should "return error if non-JavaScriptException in errorHandler" in {
    //given
    val currLogger = TaskError.logger
    val logger = mockFunction[String, Unit]
    TaskError.logger = logger
    val ex = new Exception("test error")
    val value = ex
    val stackTrace = TaskError.printStackTrace(ex, sep = " ")

    //then
    logger.expects(stackTrace)

    //when
    val TaskError(error, errorDetails) = TaskError.errorHandler(value)

    //then
    error shouldBe s"$ex"
    errorDetails shouldBe stackTrace

    //cleanup
    TaskError.logger = currLogger
  }

  it should "return error without details if custom error in errorHandler" in {
    //given
    val currLogger = TaskError.logger
    val logger = mockFunction[String, Unit]
    TaskError.logger = logger
    val ex = "test error"
    val value = ex

    //then
    logger.expects(ex)

    //when
    val TaskError(error, errorDetails) = TaskError.errorHandler(value)

    //then
    error shouldBe s"$ex"
    errorDetails shouldBe js.undefined

    //cleanup
    TaskError.logger = currLogger
  }

  it should "return error with stack details of js error in errorHandler" in {
    //given
    val currLogger = TaskError.logger
    val logger = mockFunction[String, Unit]
    TaskError.logger = logger
    val ex = js.Error("js error")
    val value = ex
    val stack = ex.asInstanceOf[js.Dynamic].stack.asInstanceOf[String]

    //then
    logger.expects(stack)

    //when
    val TaskError(error, errorDetails) = TaskError.errorHandler(value)

    //then
    error shouldBe s"$ex"
    errorDetails shouldBe stack
    stack should include ("Error: js error")

    //cleanup
    TaskError.logger = currLogger
  }
}
