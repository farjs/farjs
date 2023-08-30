package farjs.ui.task

import scala.scalajs.js

sealed trait TaskManagerUiProps extends js.Object {
  val showLoading: Boolean
  val onHideStatus: js.Function0[Unit]
  val onCloseErrorPopup: js.Function0[Unit]
  val status: js.UndefOr[String]
  val error: js.UndefOr[String]
  val errorDetails: js.UndefOr[String]
}

object TaskManagerUiProps {

  def apply(showLoading: Boolean,
            onHideStatus: js.Function0[Unit],
            onCloseErrorPopup: js.Function0[Unit],
            status: js.UndefOr[String] = js.undefined,
            error: js.UndefOr[String] = js.undefined,
            errorDetails: js.UndefOr[String] = js.undefined
           ): TaskManagerUiProps = {

    js.Dynamic.literal(
      showLoading = showLoading,
      onHideStatus = onHideStatus,
      onCloseErrorPopup = onCloseErrorPopup,
      status = status,
      error = error,
      errorDetails = errorDetails
    ).asInstanceOf[TaskManagerUiProps]
  }

  def unapply(arg: TaskManagerUiProps): Option[
    (Boolean, js.Function0[Unit], js.Function0[Unit], js.UndefOr[String], js.UndefOr[String], js.UndefOr[String])
  ] = {
    Some((
      arg.showLoading,
      arg.onHideStatus,
      arg.onCloseErrorPopup,
      arg.status,
      arg.error,
      arg.errorDetails
    ))
  }

  def copy(p: TaskManagerUiProps)(showLoading: Boolean = p.showLoading,
                                  onHideStatus: js.Function0[Unit] = p.onHideStatus,
                                  onCloseErrorPopup: js.Function0[Unit] = p.onCloseErrorPopup,
                                  status: js.UndefOr[String] = p.status,
                                  error: js.UndefOr[String] = p.error,
                                  errorDetails: js.UndefOr[String] = p.errorDetails): TaskManagerUiProps = {
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
