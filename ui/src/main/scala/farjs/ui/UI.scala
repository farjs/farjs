package farjs.ui

object UI {

  val newLine: String = """
                          |""".stripMargin

  def splitText(text: String, maxLen: Int): List[String] = {
    val parts = text.split(" ")

    parts.foldLeft(List.empty[String]) {
      case (Nil, item) => List(item)
      case (head::tail, item) =>
        if ((head.length + item.length + 1) > maxLen) {
          item :: head :: tail
        } else {
          s"$head $item" :: tail
        }
    }.reverse
  }
}
