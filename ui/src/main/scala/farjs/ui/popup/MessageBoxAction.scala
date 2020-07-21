package farjs.ui.popup

case class MessageBoxAction(label: String,
                            onAction: () => Unit,
                            triggeredOnClose: Boolean = false)

object MessageBoxAction {

  def OK(onAction: () => Unit): MessageBoxAction =
    MessageBoxAction("OK", onAction, triggeredOnClose = true)
  
  def YES(onAction: () => Unit): MessageBoxAction =
    MessageBoxAction("YES", onAction)
  
  def NO(onAction: () => Unit): MessageBoxAction =
    MessageBoxAction("NO", onAction, triggeredOnClose = true)
}
