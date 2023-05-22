package farjs.viewer

import scommons.nodejs.{FS, Stats}

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

//noinspection NotImplementedCode
class MockFS(
  openSyncMock: (String, Int) => Int = (_, _) => ???,
  readMock: (Int, Uint8Array, Int, Int, js.UndefOr[Double]) => Future[Int] = (_, _, _, _, _) => ???,
  closeSyncMock: Int => Unit = _ => ???,
  lstatSyncMock: String => Stats = _ => ???,
) extends FS {

  override def openSync(path: String, flags: Int): Int = openSyncMock(path, flags)

  override def read(fd: Int,
                    buffer: Uint8Array,
                    offset: Int,
                    length: Int,
                    position: js.UndefOr[Double] = js.undefined): Future[Int] = {

    readMock(fd, buffer, offset, length, position)
  }

  override def closeSync(fd: Int): Unit = closeSyncMock(fd)

  override def lstatSync(path: String): Stats =
    lstatSyncMock(path)
}
