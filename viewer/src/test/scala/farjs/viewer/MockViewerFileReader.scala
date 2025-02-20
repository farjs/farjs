package farjs.viewer

import scala.concurrent.Future
import scala.scalajs.js

//noinspection NotImplementedCode
class MockViewerFileReader(
  openMock: String => js.Promise[Unit] = _ => ???,
  closeMock: () => js.Promise[Unit] = () => ???,
  readPrevLinesMock: (Int, Double, Double, String) => Future[List[(String, Int)]] = (_, _, _, _) => ???,
  readNextLinesMock: (Int, Double, String) => Future[List[(String, Int)]] = (_, _, _) => ???
) extends ViewerFileReader(null, bufferSize = 15, maxLineLength = 10) {

  override def open(filePath: String): js.Promise[Unit] = openMock(filePath)

  override def close(): js.Promise[Unit] = closeMock()

  override def readPrevLines(lines: Int, position: Double, maxPos: Double, encoding: String): Future[List[(String, Int)]] =
    readPrevLinesMock(lines, position, maxPos, encoding)

  override def readNextLines(lines: Int, position: Double, encoding: String): Future[List[(String, Int)]] =
    readNextLinesMock(lines, position, encoding)
}
