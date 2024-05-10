package farjs.viewer

import farjs.file.FileReader
import scommons.nodejs.Buffer

import scala.concurrent.Future

//noinspection NotImplementedCode
class MockFileReader(
  openMock: String => Future[Unit] = _ => ???,
  closeMock: () => Future[Unit] = () => ???,
  readBytesMock: (Double, Int) => Future[Buffer] = (_, _) => ???,
) extends FileReader(fs = null) {

  override def open(filePath: String): Future[Unit] = openMock(filePath)

  override def close(): Future[Unit] = closeMock()

  override def readBytes(position: Double, size: Int): Future[Buffer] =
    readBytesMock(position, size)
}
