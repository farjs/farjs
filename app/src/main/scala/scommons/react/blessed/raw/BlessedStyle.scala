package scommons.react.blessed.raw

import scala.scalajs.js

trait BlessedStyle extends js.Object {

  val border: js.UndefOr[BlessedBorderStyle] = js.undefined
}

trait BlessedBorder extends js.Object {

  val `type`: js.UndefOr[String] = js.undefined
}

trait BlessedBorderStyle extends js.Object {

  val fg: js.UndefOr[String] = js.undefined
}
