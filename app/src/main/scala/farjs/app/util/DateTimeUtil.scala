package farjs.app.util

import scala.scalajs.js

object DateTimeUtil {

  private val currYear = new js.Date().getFullYear().toInt
  
  private lazy val mmddyyyyTimeRegex = """(\d{2})-(\d{2})-(\d{4}) (\d{2}):(\d{2})""".r
  private lazy val ddmmyyTimeRegex = """(\d{2}).(\d{2}).(\d{2}) (\d{2}):(\d{2})""".r

  def parseDateTime(input: String): Double = {
    
    def mmddyyyyTime: Option[(Int, Int, Int, Int, Int)] =
      for {
        mmddyyyyTimeRegex(month, date, year, hours, minutes) <- mmddyyyyTimeRegex.findFirstMatchIn(input)
      } yield {
        (year.toInt, month.toInt, date.toInt, hours.toInt, minutes.toInt)
      }

    def ddmmyyTime: Option[(Int, Int, Int, Int, Int)] =
      for {
        ddmmyyTimeRegex(date, month, year, hours, minutes) <- ddmmyyTimeRegex.findFirstMatchIn(input)
      } yield {
        (year.toInt, month.toInt, date.toInt, hours.toInt, minutes.toInt)
      }

    mmddyyyyTime
      .orElse(ddmmyyTime)
      .map { case (year, month, date, hours, minutes) =>
        val fullYear =
          if (year < 100) {
            val after2000 = year + 2000
            if (after2000 > currYear) year + 1900
            else after2000
          }
          else year
          
        new js.Date(fullYear, month - 1, date, hours, minutes).getTime()
      }
      .getOrElse(0.0)
  }
}
