package farjs.filelist.api

import scala.concurrent.Future
import scala.scalajs.js.typedarray.Uint8Array

//noinspection NotImplementedCode
class MockFileSource(
  val file: String = "file.mock",
  readNextBytesMock: Uint8Array => Future[Int] = _ => ???,
  closeMock: () => Future[Unit] = () => ???
) extends FileSource {

  override def readNextBytes(buff: Uint8Array): Future[Int] = readNextBytesMock(buff)

  override def close(): Future[Unit] = closeMock()
}
