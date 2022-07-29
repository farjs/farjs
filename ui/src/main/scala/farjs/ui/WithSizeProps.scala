package farjs.ui

import scommons.react.ReactElement

import scala.scalajs.js

sealed trait WithSizeProps extends js.Object {
  val render: js.Function2[Int, Int, ReactElement]
}

object WithSizeProps {

  def apply(render: js.Function2[Int, Int, ReactElement]): WithSizeProps = {
    js.Dynamic.literal(
      render = render
    ).asInstanceOf[WithSizeProps]
  }

  def unapply(arg: WithSizeProps): Option[js.Function2[Int, Int, ReactElement]] = {
    Some(
      arg.render
    )
  }
}
