package farjs.viewer

import scala.scalajs.js

sealed trait ViewerSearchProps extends js.Object {
  val searchTerm: String
  val onComplete: js.Function0[Unit]
}

object ViewerSearchProps {

  def apply(searchTerm: String,
            onComplete: js.Function0[Unit]): ViewerSearchProps = {

    js.Dynamic.literal(
      searchTerm = searchTerm,
      onComplete = onComplete
    ).asInstanceOf[ViewerSearchProps]
  }

  def unapply(arg: ViewerSearchProps): Option[(String, js.Function0[Unit])] = {
    Some((
      arg.searchTerm,
      arg.onComplete
    ))
  }
}
