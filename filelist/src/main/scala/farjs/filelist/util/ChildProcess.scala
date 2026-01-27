package farjs.filelist.util

import farjs.filelist.util.ChildProcess._
import scommons.nodejs.raw
import scommons.nodejs.raw.{ChildProcess => native}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js

trait ChildProcess {

  def exec(command: String,
           options: Option[ChildProcessOptions] = None
          ): (raw.ChildProcess, Future[(js.Object, js.Object)]) = {

    val p = Promise[(js.Object, js.Object)]()
    val callback: js.Function3[js.Error, js.Object, js.Object, Unit] = { (error, stdout, stderr) =>
      if (error != null && !js.isUndefined(error)) {
        val resultError = error.asInstanceOf[js.Dynamic]
        resultError.stdout = stdout
        resultError.stderr = stderr
        p.failure(js.JavaScriptException(resultError))
      }
      else p.success((stdout, stderr))
    }
    
    val child = native.exec(
      command = command,
      options = options match {
        case None => js.undefined
        case Some(v) => v
      },
      callback = callback
    )
    (child, p.future)
  }
  
  def spawn(command: String,
            args: Seq[String] = Nil,
            options: Option[ChildProcessOptions] = None
           ): Future[SubProcess] = {
    
    SubProcess.wrap(native.spawn(
      command = command,
      args =
        if (args.isEmpty) js.undefined
        else js.Array(args: _*),
      options = options match {
        case None => js.undefined
        case Some(v) => v
      }
    )).toFuture
  }
}

object ChildProcess {

  val child_process: ChildProcess = new ChildProcess {}
  
  type ChildProcessOptions = raw.ChildProcessOptions
}
