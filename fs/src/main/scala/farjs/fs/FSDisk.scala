package farjs.fs

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.RegExp

sealed trait FSDisk extends js.Object {
  val root: String
  val size: Double
  val free: Double
  val name: String
}

object FSDisk {

  def apply(root: String, size: Double, free: Double, name: String): FSDisk = {
    js.Dynamic.literal(
      root = root,
      size = size,
      free = free,
      name = name
    ).asInstanceOf[FSDisk]
  }

  def unapply(arg: FSDisk): Option[(String, Double, Double, String)] = {
    Some((
      arg.root,
      arg.size,
      arg.free,
      arg.name
    ))
  }

  def copy(p: FSDisk)(root: String = p.root,
                      size: Double = p.size,
                      free: Double = p.free,
                      name: String = p.name): FSDisk = {
    FSDisk(
      root = root,
      size = size,
      free = free,
      name = name
    )
  }

  private lazy val dfRegex: js.RegExp =
    new js.RegExp("""(Filesystem\s+|1024-blocks|\s+Used|\s+Available|\s+Capacity|\s+Mounted on\s*)""", "dg")
  
  private lazy val wmicLogicalDiskRegex: js.RegExp =
    new js.RegExp("""(Caption\s*|FreeSpace\s*|Size\s*|VolumeName\s*)""", "dg")
  
  def fromDfCommand(output: String): js.Array[FSDisk] = {
    parseOutput(dfRegex, output).map { data =>
      FSDisk(
        root = data.getOrElse("Mounted on", ""),
        size = toDouble(data.getOrElse("1024-blocks", "")) * 1024,
        free = toDouble(data.getOrElse("Available", "")) * 1024,
        name = data.getOrElse("Mounted on", "")
      )
    }
  }

  def fromWmicLogicalDisk(output: String): js.Array[FSDisk] = {
    parseOutput(wmicLogicalDiskRegex, output).map { data =>
      FSDisk(
        root = data.getOrElse("Caption", ""),
        size = toDouble(data.getOrElse("Size", "")),
        free = toDouble(data.getOrElse("FreeSpace", "")),
        name = data.getOrElse("VolumeName", "")
      )
    }
  }
  
  private def parseOutput(regexIn: js.RegExp, output: String): js.Array[Map[String, String]] = {
    val lines = output.trim.split('\n')
    val headLine = lines.head
    val regex = new RegExp(regexIn)
    val columnsBuf = new mutable.ArrayBuffer[(String, Int, Int)]
    var regexRes: js.RegExp.ExecResult = null
    while ({regexRes = regex.exec(headLine); regexRes} != null) {
      val indices = regexRes.asInstanceOf[js.Dynamic].indices.asInstanceOf[js.Array[js.Array[Int]]]
      val start = indices(1)(0)
      val end = indices(1)(1)
      val column = headLine.substring(start, end).trim
      columnsBuf.addOne((column, start, end))
    }
    
    val columns = columnsBuf.toList.zipWithIndex
    val lastColumnIdx = columns.size - 1
    val res = lines.tail.map { line =>
      columns.map { case ((column, start, end), i) =>
        val until =
          if (i == lastColumnIdx) line.length
          else end
        
        (column, line.slice(start, until).trim)
      }.toMap
    }
    js.Array(res.toList: _*)
  }
  
  private def toDouble(s: String): Double = if (s.isEmpty) 0.0 else s.toDouble
}
