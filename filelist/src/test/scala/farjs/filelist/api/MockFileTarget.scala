package farjs.filelist.api

import scala.concurrent.Future
import scala.scalajs.js.typedarray.Uint8Array

//noinspection NotImplementedCode
class MockFileTarget(
  val file: String = "file.mock",
  writeNextBytesMock: (Uint8Array, Int) => Future[Double] = (_, _) => ???,
  setAttributesMock: FileListItem => Future[Unit] = _ => ???,
  closeMock: () => Future[Unit] = () => ???,
  deleteMock: () => Future[Unit] = () => ???
) extends FileTarget {

  override def writeNextBytes(buff: Uint8Array, length: Int): Future[Double] =
    writeNextBytesMock(buff, length)

  override def setAttributes(src: FileListItem): Future[Unit] =
    setAttributesMock(src)

  override def close(): Future[Unit] = closeMock()

  override def delete(): Future[Unit] = deleteMock()
}
