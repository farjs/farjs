package definitions

import common.Libs
import sbt.Keys._
import sbt._
import scommons.sbtplugin.project.CommonModule
import xerial.sbt.Sonatype.autoImport._

trait FarjsModule extends CommonModule {

  val scommonsNodejsVersion: String = Libs.scommonsNodejsVersion
  
  override val repoName = "farjs"

  override def definition: Project = {
    super.definition
      .settings(FarjsModule.settings: _*)
  }
}

object FarjsModule {

  val settings: Seq[Setting[_]] = Seq(
    organization := "org.scommons.farjs",
    
    //
    // publish/release related settings:
    //
    publishMavenStyle := true,
    Test / publishArtifact := false,
    publishTo := sonatypePublishToBundle.value,
    pomExtra := {
      <url>https://github.com/farjs/farjs</url>
        <licenses>
          <license>
            <name>The MIT License</name>
            <url>https://opensource.org/licenses/MIT</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:farjs/farjs.git</url>
          <connection>scm:git@github.com:farjs/farjs.git</connection>
        </scm>
        <developers>
          <developer>
            <id>viktorp</id>
            <name>Viktor Podzigun</name>
            <url>https://github.com/viktor-podzigun</url>
          </developer>
        </developers>
    },
    pomIncludeRepository := {
      _ => false
    }
  )
}
