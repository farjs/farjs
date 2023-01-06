package farjs.archiver

import scala.scalajs.js

object DateTimeUtil {

  private val currYear = new js.Date().getFullYear().toInt
  
  private lazy val MMddyyyyTimeRegex = """(\d{2})-(\d{2})-(\d{4}) (\d{2}):(\d{2})""".r
  private lazy val ddMMyyTimeRegex = """(\d{2})\.(\d{2})\.(\d{2}) (\d{2}):(\d{2})""".r
  private lazy val yyyyMMddHHmmssRegex = """(\d{4})(\d{2})(\d{2})\.(\d{2})(\d{2})(\d{2})""".r

  def parseDateTime(input: String): Double = {
    
    def MMddyyyyTime: Option[(Int, Int, Int, Int, Int, Int)] =
      for {
        MMddyyyyTimeRegex(month, date, year, hours, minutes) <- MMddyyyyTimeRegex.findFirstMatchIn(input)
      } yield {
        (year.toInt, month.toInt, date.toInt, hours.toInt, minutes.toInt, 0)
      }

    def ddMMyyTime: Option[(Int, Int, Int, Int, Int, Int)] =
      for {
        ddMMyyTimeRegex(date, month, year, hours, minutes) <- ddMMyyTimeRegex.findFirstMatchIn(input)
      } yield {
        (year.toInt, month.toInt, date.toInt, hours.toInt, minutes.toInt, 0)
      }

    def yyyyMMddHHmmss: Option[(Int, Int, Int, Int, Int, Int)] =
      for {
        yyyyMMddHHmmssRegex(year, month, date, hours, minutes, seconds) <- yyyyMMddHHmmssRegex.findFirstMatchIn(input)
      } yield {
        (year.toInt, month.toInt, date.toInt, hours.toInt, minutes.toInt, seconds.toInt)
      }

    yyyyMMddHHmmss
      .orElse(ddMMyyTime)
      .orElse(MMddyyyyTime)
      .map { case (year, month, date, hours, minutes, seconds) =>
        val fullYear =
          if (year < 100) {
            val after2000 = year + 2000
            if (after2000 > currYear) year + 1900
            else after2000
          }
          else year
          
        new js.Date(fullYear, month - 1, date, hours, minutes, seconds).getTime()
      }
      .getOrElse(0.0)
  }
}
