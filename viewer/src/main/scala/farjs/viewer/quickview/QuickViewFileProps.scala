package farjs.viewer.quickview

import farjs.filelist.stack.WithStackProps
import farjs.ui.Dispatch

import scala.scalajs.js

sealed trait QuickViewFileProps extends js.Object {
  val dispatch: Dispatch
  val panelStack: WithStackProps
  val filePath: String
  val size: Double
}

object QuickViewFileProps {

  def apply(dispatch: Dispatch,
            panelStack: WithStackProps,
            filePath: String,
            size: Double): QuickViewFileProps = {

    js.Dynamic.literal(
      dispatch = dispatch,
      panelStack = panelStack,
      filePath = filePath,
      size = size
    ).asInstanceOf[QuickViewFileProps]
  }

  def unapply(arg: QuickViewFileProps): Option[(Dispatch, WithStackProps, String, Double)] = {
    Some((
      arg.dispatch,
      arg.panelStack,
      arg.filePath,
      arg.size
    ))
  }
}
