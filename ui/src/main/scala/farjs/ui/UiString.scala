package farjs.ui

import scommons.react.blessed.raw.Blessed.unicode

import scala.collection.mutable

class UiString private(val str: String) {

  def ensureWidth(width: Int, padCh: Char): String = {
    
    def pad(s: String, padLen: Int): String = {
      val sb = new mutable.StringBuilder(padLen, s)
      var count = padLen
      while (count > 0) {
        sb.append(padCh)
        count -= 1
      }
      sb.toString()
    }
    
    if (width == strWidth) str
    else if (width > strWidth) pad(str, width - strWidth)
    else {
      var sw = 0
      var cw = 0
      var i = 0
      while ((sw + cw) < width) {
        sw += cw
        cw = unicode.charWidth(str, i)
        if ((sw + cw) <= width) {
          if (unicode.isSurrogate(str, i) ||
            (i + 1) < str.length && unicode.isCombining(str, i + 1)) {
            i += 1
          }
          i += 1
        }
      }
      
      val s = str.slice(0, i)
      if ((sw + cw) > width) pad(s, width - sw)
      else s
    }
  }

  lazy val strWidth: Int = unicode.strWidth(str)

  override def toString: String = str
}

object UiString {

  def apply(s: String): UiString = new UiString(s)
}
