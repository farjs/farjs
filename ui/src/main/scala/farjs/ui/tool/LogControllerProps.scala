package farjs.ui.tool

import scommons.react.ReactElement

import scala.scalajs.js

sealed trait LogControllerProps extends js.Object {
  val onReady: js.Function0[Unit]
  val render: js.Function1[String, ReactElement]
}

object LogControllerProps {

  def apply(onReady: js.Function0[Unit],
            render: js.Function1[String, ReactElement]): LogControllerProps = {

    js.Dynamic.literal(
      onReady = onReady,
      render = render
    ).asInstanceOf[LogControllerProps]
  }

  def unapply(arg: LogControllerProps): Option[(js.Function0[Unit], js.Function1[String, ReactElement])] = {
    Some((
      arg.onReady,
      arg.render
    ))
  }
}
