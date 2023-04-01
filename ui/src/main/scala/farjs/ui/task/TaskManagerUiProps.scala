package farjs.ui.task

case class TaskManagerUiProps(showLoading: Boolean,
                              status: Option[String],
                              onHideStatus: () => Unit,
                              error: Option[String],
                              errorDetails: Option[String],
                              onCloseErrorPopup: () => Unit)
