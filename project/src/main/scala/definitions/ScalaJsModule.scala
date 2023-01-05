package definitions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node._
import common.{Libs, TestLibs}
import org.scalajs.linker.interface.Report
import org.scalajs.linker.interface.unstable.ReportImpl
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scommons.sbtplugin.project.CommonNodeJsModule

import scala.collection.JavaConverters._

trait ScalaJsModule extends FarjsModule with CommonNodeJsModule {

  override def definition: Project = {
    super.definition
      .settings(ScalaJsModule.settings: _*)
  }

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = {
    super.superRepoProjectsDependencies ++ Seq(
      ("scommons-react", "scommons-react-core", None),
      ("scommons-react", "scommons-react-redux", None),

      ("scommons-react", "scommons-react-test", Some("test"))
    )
  }

  override def runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting {
    super.runtimeDependencies.value ++ Seq(
      Libs.scommonsReactCore.value,
      Libs.scommonsReactRedux.value
    )
  }

  override def testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting {
    super.testDependencies.value ++ Seq(
      TestLibs.scommonsReactTest.value
    ).map(_ % "test")
  }
}

object ScalaJsModule {

  val scalaJSBundlerPackageJson =
    TaskKey[BundlerFile.PackageJson]("scalaJSBundlerPackageJson",
      "Write a package.json file defining the NPM dependencies of project",
      KeyRanks.Invisible
    )

  val settings: Seq[Setting[_]] = Seq(
    removeWebpackDevDependencies(Compile),
    removeWebpackDevDependencies(Test),

    Test / fastLinkJS := {
      val (_, report) = moveModules(
        linkerDir = (Test / fastLinkJS / scalaJSLinkerOutputDirectory).value,
        targetDir = (Test / npmUpdate / crossTarget).value,
        report = (Test / fastLinkJS).value
      )
      report
    }
  )

  private def removeWebpackDevDependencies(config: ConfigKey): Def.Setting[Task[BundlerFile.PackageJson]] = {
    config / scalaJSBundlerPackageJson := {
      val packageJsonFile = (config / scalaJSBundlerPackageJson).value
      val mapper = new ObjectMapper()
      val json = mapper.readTree(IO.read(packageJsonFile.file)).asInstanceOf[ObjectNode]
      val devDeps = json.get("devDependencies").asInstanceOf[ObjectNode]
      devDeps.remove(List(
        "source-map-loader",
        "webpack",
        "concat-with-sourcemaps",
        "webpack-cli",
        "webpack-dev-server"
      ).asJava)

      mapper.writerWithDefaultPrettyPrinter().writeValue(packageJsonFile.file, json)
      packageJsonFile
    }
  }

  def addFilesToPackageJson(packageJson: File, modules: Set[String]): Unit = {
    val mapper = new ObjectMapper()
    val json = mapper.readTree(IO.read(packageJson)).asInstanceOf[ObjectNode]
    val files = {
      if (json.hasNonNull("files")) {
        json.get("files").elements().asScala.map(_.asText()).toSet
      }
      else Set.empty[String]
    }
    json.set("files", json.arrayNode().addAll((files ++ modules).map(json.textNode).asJava))

    mapper.writerWithDefaultPrettyPrinter().writeValue(packageJson, json)
  }

  def moveModules(linkerDir: File,
                  targetDir: File,
                  report: Attributed[Report]): (Set[String], Attributed[Report]) = {

    val keepFiles = Set("main.js", "main.js.map")
    val modules = IO.listFiles(linkerDir).toSet.filter(f => !keepFiles.contains(f.getName))
    IO.move(modules.map(f => (f, new File(targetDir, f.getName))))

    (modules.map(_.getName), report.map { r =>
      new ReportImpl(r.publicModules.filter(_.moduleID == "main"))
    })
  }
}
