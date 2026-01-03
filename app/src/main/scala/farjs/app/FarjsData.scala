package farjs.app

import scommons.nodejs.Process.Platform

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

trait FarjsData extends js.Object {

  def getDBFilePath(): String

  def getDataDir(): js.Array[String]
}

@js.native
@JSImport("../app/FarjsData.mjs", JSImport.Default)
object FarjsData extends js.Function1[Platform, FarjsData] {

  val instance: FarjsData = js.native

  def apply(platform: Platform): FarjsData = js.native
}
