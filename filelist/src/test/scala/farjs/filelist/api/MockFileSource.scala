package farjs.filelist.api

import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

object MockFileSource {

  //noinspection NotImplementedCode
  def apply(
             fileMock: String = "file.mock",
             readNextBytesMock: Uint8Array => js.Promise[Int] = _ => ???,
             closeMock: () => js.Promise[Unit] = () => ???
           ): FileSource = {
    
    new FileSource {
      
      override val file: String = fileMock

      override def readNextBytes(buff: Uint8Array): js.Promise[Int] = readNextBytesMock(buff)
    
      override def close(): js.Promise[Unit] = closeMock()
    }
  }
}
