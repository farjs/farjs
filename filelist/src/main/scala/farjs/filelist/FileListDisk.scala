package farjs.filelist

import scala.util.matching.Regex

case class FileListDisk(root: String, size: Double, free: Double, name: String)

object FileListDisk {
  
  private lazy val dfRegex =
    """(Filesystem\s+|1024-blocks|\s+Used|\s+Available|\s+Capacity|\s+Mounted on\s*)""".r
  
  private lazy val wmicLogicalDiskRegex =
    """(Caption\s*|FreeSpace\s*|Size\s*|VolumeName\s*)""".r
  
  def fromDfCommand(output: String): List[FileListDisk] = {
    parseOutput(dfRegex, output).map { data =>
      FileListDisk(
        root = data.getOrElse("Mounted on", ""),
        size = toDouble(data.getOrElse("1024-blocks", "")) * 1024,
        free = toDouble(data.getOrElse("Available", "")) * 1024,
        name = data.getOrElse("Mounted on", "")
      )
    }
  }

  def fromWmicLogicalDisk(output: String): List[FileListDisk] = {
    parseOutput(wmicLogicalDiskRegex, output).map { data =>
      FileListDisk(
        root = data.getOrElse("Caption", ""),
        size = toDouble(data.getOrElse("Size", "")),
        free = toDouble(data.getOrElse("FreeSpace", "")),
        name = data.getOrElse("VolumeName", "")
      )
    }
  }
  
  private def parseOutput(regex: Regex, output: String): List[Map[String, String]] = {
    val lines = output.trim.split('\n').toList
    val columns = (for {
      column <- regex.findAllMatchIn(lines.head)
    } yield {
      (column.toString().trim, column.start, column.end)
    }).toList.zipWithIndex
    
    val lastColumnIdx = columns.size - 1
    lines.tail.map { line =>
      columns.map { case ((column, start, end), i) =>
        val until =
          if (i == lastColumnIdx) line.length
          else end
        
        (column, line.slice(start, until).trim)
      }.toMap
    }
  }
  
  private def toDouble(s: String): Double = if (s.isEmpty) 0.0 else s.toDouble
}
