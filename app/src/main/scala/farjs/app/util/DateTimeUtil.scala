package farjs.app.util

import scala.scalajs.js

object DateTimeUtil {

  private lazy val dateTimeRegex = """(\d{2})-(\d{2})-(\d{4}) (\d{2}):(\d{2})""".r

  def parseDateTime(input: String): Double = {
    (for {
      dateTimeRegex(month, date, year, hours, minutes) <- dateTimeRegex.findFirstMatchIn(input)
    } yield {
      new js.Date(year.toInt, month.toInt - 1, date.toInt, hours.toInt, minutes.toInt).getTime()
    }).getOrElse(0.0)
  }
}
