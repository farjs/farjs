package farjs.viewer

import farjs.file.{Encoding, FileReader}
import scommons.nodejs.Buffer

import scala.scalajs.js

class ViewerFileReader(fileReader: FileReader,
                       bufferSize: Int = 64 * 1024,
                       maxLineLength: Int = 1024) {
  
  private val fileBuf = Buffer.allocUnsafe(math.max(bufferSize, maxLineLength))
  
  def open(filePath: String): js.Promise[Unit] = fileReader.open(filePath)

  def close(): js.Promise[Unit] = fileReader.close()

  def readPrevLines(lines: Int, position: Double, maxPos: Double, encoding: String): js.Promise[js.Array[ViewerFileLine]] = {
    val res = new js.Array[ViewerFileLine]()
    var leftBuf = Buffer.from(js.Array[Short]())
    val bufSize =
      if (lines > 1) bufferSize
      else maxLineLength

    def loopOverBuffer(bufParam: Buffer, fromEndParam: Boolean): Unit = {
      var buf = bufParam
      var fromEnd = fromEndParam
      while (true) {
        val suffix =
          if (buf.length > maxLineLength) buf.subarray(buf.length - maxLineLength, buf.length)
          else buf

        val rightNewLineIdx =
          if (fromEnd) suffix.length
          else {
            val idx = suffix.lastIndexOf('\n'.toInt, suffix.length)
            if (idx >= 0 && idx < suffix.length - 1) suffix.length
            else idx
          }
        val leftNewLineIdx =
          if (rightNewLineIdx <= 0) -1
          else suffix.lastIndexOf('\n'.toInt, rightNewLineIdx - 1)

        if (leftNewLineIdx < 0 && buf.length < maxLineLength) {
          leftBuf = Buffer.from(buf)
          return
        }
        else {
          val (line, bytes) =
            if (rightNewLineIdx < 0) {
              val line = Encoding.decode(suffix, encoding, start = 0, end = suffix.length)
              val bytes = suffix.length
              (line, bytes)
            }
            else {
              if (leftNewLineIdx < 0) {
                val line = Encoding.decode(suffix, encoding, start = 0, end = rightNewLineIdx)
                val bytes = suffix.length
                (line, bytes)
              }
              else {
                val line = Encoding.decode(suffix, encoding, start = leftNewLineIdx + 1, end = rightNewLineIdx)
                val bytes = suffix.length - leftNewLineIdx - 1
                (line, bytes)
              }
            }
          res.insert(0, ViewerFileLine(line, bytes))

          if (res.length < lines && bytes < buf.length) {
            buf = buf.subarray(0, buf.length - bytes)
            fromEnd = false
          }
          else return
        }
      }
    }

    def loop(position: Double): js.Promise[js.Array[ViewerFileLine]] = {
      val (from, size) =
        if (position > bufSize) (position - bufSize, bufSize)
        else (0.0, position.toInt)
      
      fileReader.readBytes(from, fileBuf.subarray(0, size)).`then`[js.Array[ViewerFileLine]]({ bytesRead =>
        val buf = fileBuf.subarray(0, bytesRead)
        val resBuf = Buffer.concat(js.Array(buf, leftBuf), buf.length + leftBuf.length)
        leftBuf = Buffer.from(js.Array[Short]())

        loopOverBuffer(resBuf, position == maxPos)
        
        if (res.length < lines && from > 0) loop(from)
        else {
          if (res.length < lines && leftBuf.length > 0) {
            val line = Encoding.decode(leftBuf, encoding, start = 0, end = leftBuf.length)
            val bytes = leftBuf.length
            res.insert(0, ViewerFileLine(line.trim, bytes))
          }
          js.Promise.resolve[js.Array[ViewerFileLine]](res)
        }
      }: js.Function1[Int, js.Thenable[js.Array[ViewerFileLine]]])
    }

    if (position == 0.0) js.Promise.resolve[js.Array[ViewerFileLine]](new js.Array[ViewerFileLine]())
    else loop(position)
  }
  
  def readNextLines(lines: Int, position: Double, encoding: String): js.Promise[js.Array[ViewerFileLine]] = {
    val res = new js.Array[ViewerFileLine]()
    var leftBuf = Buffer.from(js.Array[Short]())
    val bufSize =
      if (lines > 1) bufferSize
      else maxLineLength

    def loopOverBuffer(bufParam: Buffer): Unit = {
      var buf = bufParam
      while (true) {
        val prefix = buf.subarray(0, maxLineLength)

        val newLineIndex = prefix.indexOf('\n'.toInt, 0)
        if (newLineIndex < 0 && buf.length < maxLineLength) {
          leftBuf = Buffer.from(buf)
          return
        }
        else {
          val (line, bytes) =
            if (newLineIndex < 0) {
              val line = Encoding.decode(prefix, encoding, start = 0, end = prefix.length)
              val bytes = prefix.length
              (line, bytes)
            }
            else {
              val line = Encoding.decode(prefix, encoding, start = 0, end = newLineIndex)
              val bytes = newLineIndex + 1
              (line, bytes)
            }
          res.push(ViewerFileLine(line, bytes))

          if (res.length < lines && bytes < buf.length) {
            buf = buf.subarray(bytes, buf.length)
          }
          else return
        }
      }
    }

    def loop(position: Double): js.Promise[js.Array[ViewerFileLine]] = {
      fileReader.readBytes(position, fileBuf.subarray(0, bufSize)).`then`[js.Array[ViewerFileLine]]({ bytesRead =>
        val buf = fileBuf.subarray(0, bytesRead)
        val resBuf = Buffer.concat(js.Array(leftBuf, buf), leftBuf.length + buf.length)
        leftBuf = Buffer.from(js.Array[Short]())

        if (resBuf.length > 0) {
          loopOverBuffer(resBuf)
        }
        
        if (res.length < lines && buf.length > 0) loop(position + buf.length)
        else {
          if (res.length < lines && leftBuf.length > 0) {
            val line = Encoding.decode(leftBuf, encoding, start = 0, end = leftBuf.length)
            val bytes = leftBuf.length
            res.push(ViewerFileLine(line, bytes))
          }
          js.Promise.resolve[js.Array[ViewerFileLine]](res)
        }
      }: js.Function1[Int, js.Promise[js.Array[ViewerFileLine]]])
    }

    loop(position)
  }
}
