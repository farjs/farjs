package farjs.ui

import farjs.ui.raw.NativeUI
import scommons.react.blessed.BlessedStyle

object UI {

  def renderText2(style: BlessedStyle, text: String): String =
    NativeUI.renderText2(style, text)

  def renderText(isBold: Boolean, fgColor: String, bgColor: String, text: String): String =
    NativeUI.renderText(isBold, fgColor, bgColor, text)

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
