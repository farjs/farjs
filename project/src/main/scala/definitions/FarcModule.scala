package definitions

import sbt.Keys._
import sbt._
import scommons.sbtplugin.project.CommonModule

trait FarcModule extends CommonModule {

  override val repoName = "scommons-farc"

  override def definition: Project = {
    super.definition
      .settings(FarcModule.settings: _*)
  }
}

object FarcModule {

  val settings: Seq[Setting[_]] = Seq(
    organization := "org.scommons.farc",
    
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
      <url>https://github.com/scommons/scommons-farc</url>
        <licenses>
          <license>
            <name>The MIT License</name>
            <url>https://opensource.org/licenses/MIT</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:scommons/scommons-farc.git</url>
          <connection>scm:git@github.com:scommons/scommons-farc.git</connection>
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
