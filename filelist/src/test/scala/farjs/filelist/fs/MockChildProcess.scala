package farjs.filelist.fs

import scommons.nodejs.ChildProcess._
import scommons.nodejs._

import scala.concurrent.Future
import scala.scalajs.js

//noinspection NotImplementedCode
class MockChildProcess(
  execMock: (String, Option[ChildProcessOptions]) => (raw.ChildProcess, Future[(js.Object, js.Object)]) = (_, _) => ???,
  spawnMock: (String, Seq[String], Option[ChildProcessOptions]) => raw.ChildProcess = (_, _, _) => ???
) extends ChildProcess {

  override def exec(command: String,
                    options: Option[ChildProcessOptions]
                   ): (raw.ChildProcess, Future[(js.Object, js.Object)]) = execMock(command, options)

  override def spawn(command: String,
                     args: Seq[String],
                     options: Option[ChildProcessOptions]
                    ): raw.ChildProcess = spawnMock(command, args, options)
}
