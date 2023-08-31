package farjs.ui.task

import scala.scalajs.js

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
}
