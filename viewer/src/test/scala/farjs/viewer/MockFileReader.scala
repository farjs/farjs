package farjs.viewer

import farjs.file.FileReader
import scommons.nodejs.Buffer

import scala.scalajs.js

//noinspection NotImplementedCode
class MockFileReader(
  openMock: String => js.Promise[Unit] = _ => ???,
  closeMock: () => js.Promise[Unit] = () => ???,
  readBytesMock: (Double, Buffer) => js.Promise[Int] = (_, _) => ???,
) extends FileReader() {

  override def open(filePath: String): js.Promise[Unit] = openMock(filePath)

  override def close(): js.Promise[Unit] = closeMock()

  override def readBytes(position: Double, buf: Buffer): js.Promise[Int] =
    readBytesMock(position, buf)
}
