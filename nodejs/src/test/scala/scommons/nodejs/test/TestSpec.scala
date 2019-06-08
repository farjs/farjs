package scommons.nodejs.test

import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

trait TestSpec extends FlatSpec
  with BaseTestSpec
  with MockFactory
