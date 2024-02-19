package farjs.ui.popup

import scala.scalajs.js

sealed trait ListPopupProps extends js.Object {
  val title: String
  val items: js.Array[String]
  val onAction: js.Function1[Int, Unit]
  val onClose: js.Function0[Unit]
  val selected: js.UndefOr[Int]
  val onSelect: js.UndefOr[js.Function1[Int, Unit]]
  val onKeypress: js.UndefOr[js.Function1[String, Boolean]]
  val footer: js.UndefOr[String]
  val textPaddingLeft: js.UndefOr[Int]
  val textPaddingRight: js.UndefOr[Int]
  val itemWrapPrefixLen: js.UndefOr[Int]
}

object ListPopupProps {

  def apply(title: String,
            items: js.Array[String],
            onAction: js.Function1[Int, Unit],
            onClose: js.Function0[Unit],
            selected: js.UndefOr[Int] = js.undefined,
            onSelect: js.UndefOr[js.Function1[Int, Unit]] = js.undefined,
            onKeypress: js.UndefOr[js.Function1[String, Boolean]] = js.undefined,
            footer: js.UndefOr[String] = js.undefined,
            textPaddingLeft: js.UndefOr[Int] = js.undefined,
            textPaddingRight: js.UndefOr[Int] = js.undefined,
            itemWrapPrefixLen: js.UndefOr[Int] = js.undefined): ListPopupProps = {

    js.Dynamic.literal(
      title = title,
      items = items,
      onAction = onAction,
      onClose = onClose,
      selected = selected,
      onSelect = onSelect,
      onKeypress = onKeypress,
      footer = footer,
      textPaddingLeft = textPaddingLeft,
      textPaddingRight = textPaddingRight,
      itemWrapPrefixLen = itemWrapPrefixLen
    ).asInstanceOf[ListPopupProps]
  }

  def unapply(arg: ListPopupProps): Option[(String, js.Array[String], js.Function1[Int, Unit], js.Function0[Unit], js.UndefOr[Int], js.UndefOr[js.Function1[Int, Unit]], js.UndefOr[js.Function1[String, Boolean]], js.UndefOr[String], js.UndefOr[Int], js.UndefOr[Int], js.UndefOr[Int])] = {
    Some((
      arg.title,
      arg.items,
      arg.onAction,
      arg.onClose,
      arg.selected,
      arg.onSelect,
      arg.onKeypress,
      arg.footer,
      arg.textPaddingLeft,
      arg.textPaddingRight,
      arg.itemWrapPrefixLen,
    ))
  }
}
