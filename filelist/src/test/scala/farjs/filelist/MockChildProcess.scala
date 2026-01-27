package farjs.filelist

import farjs.filelist.util.ChildProcess._
import farjs.filelist.util.{ChildProcess, SubProcess}
import scommons.nodejs.raw

import scala.concurrent.Future
import scala.scalajs.js

//noinspection NotImplementedCode
class MockChildProcess(
  execMock: (String, Option[ChildProcessOptions]) => (raw.ChildProcess, Future[(js.Object, js.Object)]) = (_, _) => ???,
  spawnMock: (String, Seq[String], Option[ChildProcessOptions]) => Future[SubProcess] = (_, _, _) => ???
) extends ChildProcess {

  override def exec(command: String,
                    options: Option[ChildProcessOptions]
                   ): (raw.ChildProcess, Future[(js.Object, js.Object)]) = execMock(command, options)

  override def spawn(command: String,
                     args: Seq[String],
                     options: Option[ChildProcessOptions]
                    ): Future[SubProcess] = spawnMock(command, args, options)
}
