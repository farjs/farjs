package farjs.filelist.util

import scommons.nodejs.raw

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

sealed trait SubProcess extends js.Object {
  val child: raw.ChildProcess
  val stdout: StreamReader
  val exitP: js.Promise[js.UndefOr[js.Error]]
}

object SubProcess {

  def apply(child: raw.ChildProcess,
            stdout: StreamReader,
            exitP: js.Promise[js.UndefOr[js.Error]]): SubProcess = {

    js.Dynamic.literal(
      child = child,
      stdout = stdout,
      exitP = exitP
    ).asInstanceOf[SubProcess]
  }

  def unapply(arg: SubProcess): Option[(raw.ChildProcess, StreamReader, js.Promise[js.UndefOr[js.Error]])] = {
    Some((
      arg.child,
      arg.stdout,
      arg.exitP
    ))
  }

  def wrap(childProcess: raw.ChildProcess): js.Promise[SubProcess] =
    SubProcessNative.wrap(childProcess)
}

@js.native
@JSImport("@farjs/filelist/util/SubProcess.mjs", JSImport.Default)
object SubProcessNative extends js.Object {

  def wrap(childProcess: raw.ChildProcess): js.Promise[SubProcess] = js.native
}
