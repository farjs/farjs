package farjs.filelist.api

import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

object MockFileTarget {

  //noinspection NotImplementedCode
  def apply(
             fileMock: String = "file.mock",
             writeNextBytesMock: (Uint8Array, Int) => js.Promise[Double] = (_, _) => ???,
             setAttributesMock: FileListItem => js.Promise[Unit] = _ => ???,
             closeMock: () => js.Promise[Unit] = () => ???,
             deleteMock: () => js.Promise[Unit] = () => ???
           ): FileTarget = {

    new FileTarget {

      override val file: String = fileMock
      
      override def writeNextBytes(buff: Uint8Array, length: Int): js.Promise[Double] =
        writeNextBytesMock(buff, length)
    
      override def setAttributes(src: FileListItem): js.Promise[Unit] =
        setAttributesMock(src)
    
      override def close(): js.Promise[Unit] = closeMock()
    
      override def delete(): js.Promise[Unit] = deleteMock()
    }
  }
}
