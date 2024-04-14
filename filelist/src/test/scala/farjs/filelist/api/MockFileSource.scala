package farjs.filelist.api

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.typedarray.Uint8Array

object MockFileSource {

  //noinspection NotImplementedCode
  def apply(
             fileMock: String = "file.mock",
             readNextBytesMock: Uint8Array => Future[Int] = _ => ???,
             closeMock: () => Future[Unit] = () => ???
           ): FileSource = {
    
    new FileSource {
      
      override val file: String = fileMock

      override def readNextBytes(buff: Uint8Array): js.Promise[Int] = readNextBytesMock(buff).toJSPromise
    
      override def close(): js.Promise[Unit] = closeMock().toJSPromise
    }
  }
}
