package farjs.ui.task

import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec

import scala.concurrent.Future
import scala.util.{Success, Try}

class FutureTaskSpec extends AsyncTestSpec {

  it should "call future.onComplete when onComplete" in {
    //given
    val future = Future.successful(())
    val task = FutureTask("test message", future)
    val f = mockFunction[Try[_], Unit]
    
    //then
    f.expects(Success(()))
    
    //when
    task.onComplete(f)

    future.map(_ => Succeeded)
  }
}
