package farjs.file

import scommons.nodejs.raw.FSConstants
import scommons.nodejs.{Buffer, FS}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSConverters.JSRichFutureNonThenable
import scala.util.Failure
import scala.util.control.NonFatal

class FileReader(val fs: FS) extends js.Object {
  
  private var fd: Int = 0

  def open(filePath: String): js.Promise[Unit] = Future {
    fd = fs.openSync(filePath, FSConstants.O_RDONLY)
  }.toJSPromise

  def close(): js.Promise[Unit] = {
    Future(fs.closeSync(fd)).recover {
      case NonFatal(ex) =>
        Console.err.println(s"Failed to close file, error: $ex")
    }
  }.toJSPromise

  def readBytes(position: Double, buf: Buffer): js.Promise[Int] = {
    fs.read(fd, buf, offset = 0, length = buf.length, position).andThen {
      case Failure(NonFatal(ex)) =>
        Console.err.println(s"Failed to read from file, error: $ex")
    }
  }.toJSPromise
}
