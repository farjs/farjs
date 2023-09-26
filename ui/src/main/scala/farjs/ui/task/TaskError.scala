package farjs.ui.task

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

sealed trait TaskError extends js.Object {
  val error: String
  val errorDetails: js.UndefOr[String]
}

object TaskError {

  def apply(error: String,
            errorDetails: js.UndefOr[String] = js.undefined
           ): TaskError = {

    js.Dynamic.literal(
      error = error,
      errorDetails = errorDetails
    ).asInstanceOf[TaskError]
  }

  def unapply(arg: TaskError): Option[(String, js.UndefOr[String])] = {
    Some((
      arg.error,
      arg.errorDetails
    ))
  }

  private[task] var logger: String => Unit = println

  val errorHandler: js.Function1[Any, TaskError] = {
    case ex@JavaScriptException(error) =>
      val stackTrace = printStackTrace(ex, sep = " ")
      logger(stackTrace)
      TaskError(s"$error", stackTrace)
    case ex: Throwable =>
      val stackTrace = printStackTrace(ex, sep = " ")
      logger(stackTrace)
      TaskError(s"$ex", stackTrace)
    case ex =>
      val stack = ex.asInstanceOf[js.Dynamic].stack.asInstanceOf[js.UndefOr[String]]
      val error = s"$ex"
      logger(stack.getOrElse(error))
      TaskError(error, stack)
  }

  private[task] def printStackTrace(x: Throwable, sep: String = "&nbsp"): String = {
    val sb = new StringBuilder(x.toString)
    val trace = x.getStackTrace
    for (t <- trace) {
      sb.append(s"\n\tat$sep").append(t)
    }

    val cause = x.getCause
    if (cause != null) {
      printStackTraceAsCause(sb, cause, trace, sep)
    }

    sb.toString
  }

  /**
   * Print stack trace as a cause for the specified stack trace.
   */
  @annotation.tailrec
  private def printStackTraceAsCause(sb: StringBuilder,
                                     cause: Throwable,
                                     causedTrace: Array[StackTraceElement],
                                     sep: String): Unit = {

    // Compute number of frames in common between this and caused
    val trace = cause.getStackTrace
    var m = trace.length - 1
    var n = causedTrace.length - 1
    while (m >= 0 && n >= 0 && trace(m) == causedTrace(n)) {
      m -= 1
      n -= 1
    }

    val framesInCommon = trace.length - 1 - m
    sb.append("\nCaused by: " + cause)

    for (i <- 0 to m) {
      sb.append(s"\n\tat$sep").append(trace(i))
    }

    if (framesInCommon != 0) {
      sb.append(s"\n\t...$sep").append(framesInCommon).append(s"${sep}more")
    }

    // Recurse if we have a cause
    val ourCause = cause.getCause
    if (ourCause != null) {
      printStackTraceAsCause(sb, ourCause, trace, sep)
    }
  }
}
