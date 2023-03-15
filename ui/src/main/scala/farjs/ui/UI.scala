package farjs.ui

object UI {

  def splitText(text: String, maxLen: Int): List[String] = {
    val sentences = text.split('\n')
    
    sentences.toList.flatMap { sentence =>
      val words = sentence.trim.split(' ')

      words.foldLeft(List.empty[String]) {
        case (Nil, item) => List(item)
        case (head :: tail, item) =>
          if ((head.length + item.length + 1) > maxLen) {
            item :: head :: tail
          } else {
            s"$head $item" :: tail
          }
      }.reverse
    }
  }
}
