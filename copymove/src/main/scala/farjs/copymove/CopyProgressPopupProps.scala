package farjs.copymove

import scala.scalajs.js

sealed trait CopyProgressPopupProps extends js.Object {
  val move: Boolean
  val item: String
  val to: String
  val itemPercent: Int
  val total: Double
  val totalPercent: Int
  val timeSeconds: Int
  val leftSeconds: Int
  val bytesPerSecond: Double
  val onCancel: js.Function0[Unit]
}

object CopyProgressPopupProps {

  def apply(move: Boolean,
            item: String,
            to: String,
            itemPercent: Int,
            total: Double,
            totalPercent: Int,
            timeSeconds: Int,
            leftSeconds: Int,
            bytesPerSecond: Double,
            onCancel: js.Function0[Unit]): CopyProgressPopupProps = {

    js.Dynamic.literal(
      move = move,
      item = item,
      to = to,
      itemPercent = itemPercent,
      total = total,
      totalPercent = totalPercent,
      timeSeconds = timeSeconds,
      leftSeconds = leftSeconds,
      bytesPerSecond = bytesPerSecond,
      onCancel = onCancel
    ).asInstanceOf[CopyProgressPopupProps]
  }

  def unapply(arg: CopyProgressPopupProps): Option[(Boolean, String, String, Int, Double, Int, Int, Int, Double, js.Function0[Unit])] = {
    Some((
      arg.move,
      arg.item,
      arg.to,
      arg.itemPercent,
      arg.total,
      arg.totalPercent,
      arg.timeSeconds,
      arg.leftSeconds,
      arg.bytesPerSecond,
      arg.onCancel
    ))
  }
}
