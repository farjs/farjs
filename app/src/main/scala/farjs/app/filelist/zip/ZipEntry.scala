package farjs.app.filelist.zip

import farjs.app.util.DateTimeUtil

import scala.util.matching.Regex

case class ZipEntry(
  parent: String,
  name: String,
  isDir: Boolean = false,
  size: Double = 0.0,
  datetimeMs: Double = 0.0
)

object ZipEntry {

  private lazy val unzipRegex = """(Length|Date|Time|Name)""".r

  def fromUnzipCommand(output: String): List[ZipEntry] = {
    parseOutput(unzipRegex, output).map { data =>
      val pathName = data.getOrElse("Name", "")
      val length = data.getOrElse("Length", "")
      val date = data.getOrElse("Date", "")
      val time = data.getOrElse("Time", "")
      val path = pathName.stripSuffix("/")
      val lastSlash = path.lastIndexOf('/')
      val (parent, name) =
        if (lastSlash != -1) path.splitAt(lastSlash)
        else ("", path)
      
      ZipEntry(
        parent = parent,
        name = name.stripPrefix("/"),
        isDir = pathName.endsWith("/"),
        size = toDouble(length),
        datetimeMs = DateTimeUtil.parseDateTime(s"$date $time")
      )
    }
  }

  private def parseOutput(regex: Regex, output: String): List[Map[String, String]] = {
    val lines = {
      val lines = output.trim.split('\n').toList
      // skip Archive: ...
      lines.tail
    }
    val header = lines.head
    val separators = lines.tail.head
    val columns = (for {
      column <- regex.findAllMatchIn(header)
    } yield {
      val (start, end) = getColumnBounds(separators, column.start, column.end)
      (column.toString().trim, start, end)
    }).toList.zipWithIndex

    val lastColumnIdx = columns.size - 1
    lines.tail.tail.map { line =>
      columns.map { case ((column, start, end), i) =>
        val until =
          if (i == lastColumnIdx) line.length
          else end

        (column, line.slice(start, until).trim)
      }.toMap
    }.takeWhile(!_.getOrElse("Length", "").contains("-"))
  }

  private def toDouble(s: String): Double = if (s.isEmpty) 0.0 else s.toDouble
  
  private def getColumnBounds(separators: String, start: Int, end: Int): (Int, Int) = {
    var from = start
    while (from > 0 && separators.charAt(from - 1) == '-') {
      from -= 1
    }

    var to = end - 1
    while (to < separators.length - 1 && separators.charAt(to + 1) == '-') {
      to += 1
    }
    
    (from, to + 1)
  }
}
