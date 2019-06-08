package scommons.nodejs.test

import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.AsyncFlatSpec

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext

trait AsyncTestSpec extends AsyncFlatSpec
  with BaseTestSpec
  with AsyncMockFactory {

  implicit override val executionContext: ExecutionContext = JSExecutionContext.queue
}
