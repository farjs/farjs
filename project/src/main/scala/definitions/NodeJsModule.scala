package definitions

import sbt.Keys._
import sbt._
import scommons.sbtplugin.project.CommonNodeJsModule

trait NodeJsModule extends FarjsModule with CommonNodeJsModule {

  override def definition: Project = {
    super.definition
      .settings(
        scalaVersion := "2.13.5"
      )
  }
}
