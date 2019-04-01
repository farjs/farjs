package scommons.react.blessed.raw

import scala.scalajs.js

trait BlessedStyle extends js.Object {

  val fg: js.UndefOr[String] = js.undefined
  val bg: js.UndefOr[String] = js.undefined
  val bold: js.UndefOr[Boolean] = js.undefined
  val underline: js.UndefOr[Boolean] = js.undefined
  val blink: js.UndefOr[Boolean] = js.undefined
  val inverse: js.UndefOr[Boolean] = js.undefined
  val invisible: js.UndefOr[Boolean] = js.undefined
  val transparent: js.UndefOr[Boolean] = js.undefined
  val border: js.UndefOr[BlessedBorderStyle] = js.undefined
  val scrollbar: js.UndefOr[BlessedScrollBarStyle] = js.undefined
  
  val focus: js.UndefOr[BlessedStyle] = js.undefined
}

trait BlessedBorderStyle extends js.Object {

  val fg: js.UndefOr[String] = js.undefined
  val bg: js.UndefOr[String] = js.undefined
}

trait BlessedScrollBarStyle extends js.Object {

  val fg: js.UndefOr[String] = js.undefined
  val bg: js.UndefOr[String] = js.undefined
}

trait BlessedBorder extends js.Object {

  val `type`: js.UndefOr[String] = js.undefined
}
