package farjs.viewer

import scala.concurrent.Future

//noinspection NotImplementedCode
class MockViewerFileReader(
  openMock: String => Future[Unit] = _ => ???,
  closeMock: () => Future[Unit] = () => ???,
  readPrevLinesMock: (Int, Double, Double, String) => Future[List[(String, Int)]] = (_, _, _, _) => ???,
  readNextLinesMock: (Int, Double, String) => Future[List[(String, Int)]] = (_, _, _) => ???
) extends ViewerFileReader(null, bufferSize = 15, maxLineLength = 10) {

  override def open(filePath: String): Future[Unit] = openMock(filePath)

  override def close(): Future[Unit] = closeMock()

  override def readPrevLines(lines: Int, position: Double, maxPos: Double, encoding: String): Future[List[(String, Int)]] =
    readPrevLinesMock(lines, position, maxPos, encoding)

  override def readNextLines(lines: Int, position: Double, encoding: String): Future[List[(String, Int)]] =
    readNextLinesMock(lines, position, encoding)
}
