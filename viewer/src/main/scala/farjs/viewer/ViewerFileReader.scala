package farjs.viewer

import farjs.file.{Encoding, FileReader}
import scommons.nodejs.Buffer

import scala.scalajs.js

class ViewerFileReader(fileReader: FileReader,
                       bufferSize: Int = 64 * 1024,
                       maxLineLength: Int = 1024) extends js.Object {
  
  private val fileBuf = Buffer.allocUnsafe(math.max(bufferSize, maxLineLength))
  
  def open(filePath: String): js.Promise[Unit] = fileReader.open(filePath)

  def close(): js.Promise[Unit] = fileReader.close()

  def readPrevLines(lines: Int, position: Double, maxPos: Double, encoding: String): js.Promise[js.Array[ViewerFileLine]] = {
    val res = new js.Array[ViewerFileLine]()
    var leftBuf: Buffer = null
    val bufSize =
      if (lines > 1) bufferSize
      else maxLineLength

    @annotation.tailrec
    def loopOverBuffer(buf: Buffer, fromEnd: Boolean): Unit = {
      val suffix =
        if (buf.length > maxLineLength) buf.subarray(buf.length - maxLineLength, buf.length)
        else buf

      val rightNewLineIdx =
        if (fromEnd) suffix.length
        else {
          val idx = suffix.lastIndexOf('\n'.toInt, suffix.length, encoding)
          if (idx >= 0 && idx < suffix.length - 1) suffix.length
          else idx
        }
      val leftNewLineIdx =
        if (rightNewLineIdx <= 0) -1
        else suffix.lastIndexOf('\n'.toInt, rightNewLineIdx - 1, encoding)

      if (leftNewLineIdx < 0 && buf.length < maxLineLength) leftBuf = buf
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
          loopOverBuffer(buf.subarray(0, buf.length - bytes), fromEnd = false)
        }
      }
    }

    def loop(position: Double): js.Promise[js.Array[ViewerFileLine]] = {
      val (from, size) =
        if (position > bufSize) (position - bufSize, bufSize)
        else (0.0, position.toInt)
      
      leftBuf = if (leftBuf != null) Buffer.from(leftBuf) else leftBuf
      
      fileReader.readBytes(from, fileBuf.subarray(0, size)).`then`[js.Array[ViewerFileLine]]({ bytesRead =>
        val buf = fileBuf.subarray(0, bytesRead)

        loopOverBuffer(
          if (leftBuf != null) {
            val resBuf = Buffer.concat(js.Array(buf, leftBuf), buf.length + leftBuf.length)
            leftBuf = null
            resBuf
          }
          else buf,
          fromEnd = position == maxPos
        )
        
        if (res.length < lines && from > 0) loop(from)
        else {
          if (res.length < lines && leftBuf != null) {
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
    var leftBuf: Buffer = null
    val bufSize =
      if (lines > 1) bufferSize
      else maxLineLength

    @annotation.tailrec
    def loopOverBuffer(buf: Buffer): Unit = {
      val prefix = buf.subarray(0, maxLineLength)
      
      val newLineIndex = prefix.indexOf('\n'.toInt, 0, encoding)
      if (newLineIndex < 0 && buf.length < maxLineLength) leftBuf = buf
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
          loopOverBuffer(buf.subarray(bytes, buf.length))
        }
      }
    }

    def loop(position: Double): js.Promise[js.Array[ViewerFileLine]] = {
      leftBuf = if (leftBuf != null) Buffer.from(leftBuf) else leftBuf
      
      fileReader.readBytes(position, fileBuf.subarray(0, bufSize)).`then`[js.Array[ViewerFileLine]]({ bytesRead =>
        val buf = fileBuf.subarray(0, bytesRead)
        if (buf.length > 0) {
          loopOverBuffer(
            if (leftBuf != null) {
              val resBuf = Buffer.concat(js.Array(leftBuf, buf), leftBuf.length + buf.length)
              leftBuf = null
              resBuf
            }
            else buf
          )
        }
        
        if (res.length < lines && buf.length > 0) loop(position + buf.length)
        else {
          if (res.length < lines && leftBuf != null) {
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
