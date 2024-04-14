package farjs.filelist.api

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.typedarray.Uint8Array

object MockFileTarget {

  //noinspection NotImplementedCode
  def apply(
             fileMock: String = "file.mock",
             writeNextBytesMock: (Uint8Array, Int) => Future[Double] = (_, _) => ???,
             setAttributesMock: FileListItem => Future[Unit] = _ => ???,
             closeMock: () => Future[Unit] = () => ???,
             deleteMock: () => Future[Unit] = () => ???
           ): FileTarget = {

    new FileTarget {

      override val file: String = fileMock
      
      override def writeNextBytes(buff: Uint8Array, length: Int): js.Promise[Double] =
        writeNextBytesMock(buff, length).toJSPromise
    
      override def setAttributes(src: FileListItem): js.Promise[Unit] =
        setAttributesMock(src).toJSPromise
    
      override def close(): js.Promise[Unit] = closeMock().toJSPromise
    
      override def delete(): js.Promise[Unit] = deleteMock().toJSPromise
    }
  }
}
