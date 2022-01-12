package definitions

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scommons.sbtplugin.project.CommonNodeJsModule
import scoverage.ScoverageKeys.{coverageEnabled, coverageScalacPluginVersion}
import scoverage.ScoverageSbtPlugin._

trait NodeJsModule extends FarjsModule with CommonNodeJsModule {

  override def definition: Project = {
    super.definition
      .settings(
        //TODO: remove these temporal fixes for Scala.js 1.1+ and scoverage
        coverageScalacPluginVersion := {
          val current = coverageScalacPluginVersion.value
          if (scalaJSVersion.startsWith("0.6")) current
          else "1.4.2" //the only version that supports Scala.js 1.1+
        },
        libraryDependencies ~= { modules =>
          if (scalaJSVersion.startsWith("0.6")) modules
          else modules.filter(_.organization != OrgScoverage)
        },
        libraryDependencies ++= {
          if (coverageEnabled.value) {
            if (scalaJSVersion.startsWith("0.6")) Nil
            else Seq(
              OrgScoverage %% s"${ScalacRuntimeArtifact}_sjs1" % coverageScalacPluginVersion.value,
              OrgScoverage %% ScalacPluginArtifact % coverageScalacPluginVersion.value % ScoveragePluginConfig.name
            )
          }
          else Nil
        }
      )
  }
}
