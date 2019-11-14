package farclone.ui.popup

case class MessageBoxAction(label: String,
                            onAction: () => Unit,
                            triggeredOnClose: Boolean = false)

object MessageBoxAction {

  def OK(onAction: () => Unit): MessageBoxAction =
    MessageBoxAction("OK", onAction, triggeredOnClose = true)
}
