package farjs.viewer

import scala.scalajs.js

//noinspection NotImplementedCode
class MockViewerFileReader(
  openMock: String => js.Promise[Unit] = _ => ???,
  closeMock: () => js.Promise[Unit] = () => ???,
  readPrevLinesMock: (Int, Double, Double, String) => js.Promise[js.Array[ViewerFileLine]] = (_, _, _, _) => ???,
  readNextLinesMock: (Int, Double, String) => js.Promise[js.Array[ViewerFileLine]] = (_, _, _) => ???
) extends ViewerFileReader(null) {

  override def open(filePath: String): js.Promise[Unit] = openMock(filePath)

  override def close(): js.Promise[Unit] = closeMock()

  override def readPrevLines(lines: Int, position: Double, maxPos: Double, encoding: String): js.Promise[js.Array[ViewerFileLine]] =
    readPrevLinesMock(lines, position, maxPos, encoding)

  override def readNextLines(lines: Int, position: Double, encoding: String): js.Promise[js.Array[ViewerFileLine]] =
    readNextLinesMock(lines, position, encoding)
}
