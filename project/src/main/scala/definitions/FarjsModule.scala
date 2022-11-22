package definitions

import common.Libs
import org.scoverage.coveralls.Imports.CoverallsKeys._
import org.scoverage.coveralls.CIService
import sbt.Keys._
import sbt._
import scommons.sbtplugin.project.CommonModule
import xerial.sbt.Sonatype.autoImport._

import scala.io.Source
import scala.util.parsing.json.{JSON, JSONObject}

trait FarjsModule extends CommonModule {

  val scommonsNodejsVersion: String = Libs.scommonsNodejsVersion
  
  override val repoName = "farjs"

  override def definition: Project = {
    super.definition
      .settings(FarjsModule.settings: _*)
  }
}

object FarjsModule {

  private case object GitHubActionsCI extends CIService {
    val name = ""
    val jobId: Option[String] = sys.env.get("GITHUB_RUN_ID")

    // https://github.com/coverallsapp/github-action/blob/master/src/run.ts#L31-L40
    val pullRequest: Option[String] = for {
      eventName <- sys.env.get("GITHUB_EVENT_NAME") if eventName.startsWith("pull_request")
      payloadPath <- sys.env.get("GITHUB_EVENT_PATH")
      source = Source.fromFile(payloadPath, "utf-8")
      lines = try source.mkString finally source.close()
      payload <- JSON.parseRaw(lines)
      prNumber <- payload.asInstanceOf[JSONObject].obj.get("number")
    } yield prNumber.toString.stripSuffix(".0")

    // https://docs.github.com/en/actions/learn-github-actions/environment-variables
    val currentBranch: Option[String] = pullRequest match {
      case Some(_) => sys.env.get("GITHUB_HEAD_REF")
      case None => sys.env.get("GITHUB_REF_NAME")
    }
  }
  
  val settings: Seq[Setting[_]] = Seq(
    organization := "org.scommons.farjs",

    coverallsService := GitHubActionsCI.jobId.map(_ => GitHubActionsCI),
    
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
