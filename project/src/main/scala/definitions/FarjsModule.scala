package definitions

import sbt.Keys._
import sbt._
import scommons.sbtplugin.project.CommonModule

trait FarjsModule extends CommonModule {

  override val repoName = "far-js"

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
    publishArtifact in Test := false,
    publishTo := {
      if (isSnapshot.value)
        Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
      else
        Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
    },
    pomExtra := {
      <url>https://github.com/scommons/far-js</url>
        <licenses>
          <license>
            <name>The MIT License</name>
            <url>https://opensource.org/licenses/MIT</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:scommons/far-js.git</url>
          <connection>scm:git@github.com:scommons/far-js.git</connection>
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
