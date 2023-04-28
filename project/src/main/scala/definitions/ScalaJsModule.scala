package definitions

import common.{Libs, TestLibs}
import org.scalajs.jsenv.Input.{CommonJSModule, ESModule}
import org.scalajs.linker.interface.ESVersion
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbt.nio.file.FileTreeView
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scalajsbundler.util.JSON
import scommons.sbtplugin.project.CommonNodeJsModule

import java.io.IOException
import java.nio.file.Files

trait ScalaJsModule extends FarjsModule with CommonNodeJsModule {

  override def definition: Project = {
    super.definition
      .settings(ScalaJsModule.settings: _*)
  }

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = {
    super.superRepoProjectsDependencies ++ Seq(
      ("scommons-react", "scommons-react-core", None),

      ("scommons-react", "scommons-react-test", Some("test"))
    )
  }

  override def runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting {
    super.runtimeDependencies.value ++ Seq(
      Libs.scommonsReactCore.value
    )
  }

  override def testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting {
    super.testDependencies.value ++ Seq(
      TestLibs.scommonsReactTest.value
    ).map(_ % "test")
  }
}

object ScalaJsModule {

  private val scalaJSBundlerPackageJson =
    TaskKey[BundlerFile.PackageJson]("scalaJSBundlerPackageJson",
      "Write a package.json file defining the NPM dependencies of project",
      KeyRanks.Invisible
    )
  
  private val ensureModuleKindIsCommonJSModule =
    SettingKey[Boolean](
      "ensureModuleKindIsCommonJSModule",
      "Checks that scalaJSModuleKind is set to CommonJSModule",
      KeyRanks.Invisible
    )

  // Settings that must be applied for each configuration
  private val configSettings: Seq[Setting[_]] = Def.settings(
    jsEnvInput := {
      val prev = jsEnvInput.value
      val linkingResult = scalaJSLinkerResult.value
      val legacyKeyOutput = scalaJSLinkedFile.value

      // Compute the path to the `main` module, which is what sbt-scalajs puts in jsEnvInput
      val report = linkingResult.data
      val optMainModule = report.publicModules.find(_.moduleID == "main")
      val optMainModulePath = optMainModule.map { mainModule =>
        val linkerOutputDirectory = linkingResult.get(scalaJSLinkerOutputDirectory.key).getOrElse {
          throw new MessageOnlyException(
            "Linking report was not attributed with output directory. " +
              "Please report this as a Scala.js bug.")
        }
        (linkerOutputDirectory / mainModule.jsFileName).toPath
      }

      // Replace the path to the `main` module by the path to the legacy key output
      optMainModulePath match {
        case Some(mainModulePath) =>
          prev.map {
            case CommonJSModule(module) if module == mainModulePath =>
              CommonJSModule(legacyKeyOutput.data.toPath)
            case ESModule(module) if module == mainModulePath =>
              ESModule(legacyKeyOutput.data.toPath)
            case inputItem =>
              inputItem
          }
        case None =>
          prev
      }
    }
  )

  val settings: Seq[Setting[_]] = Seq(
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withSourceMap(false)
        .withESFeatures(_.withESVersion(ESVersion.ES2015))
    },
    
    Test / additionalNpmConfig := Map(
      "type" -> JSON.str("module")
    ),

    // to avoid this limitation:
    //  scalaJSModuleKind must be set to ModuleKind.CommonJSModule in projects where ScalaJSBundler plugin is enabled
    ensureModuleKindIsCommonJSModule := true,

    removeUnusedDevDependencies(Compile, scalaJSBundlerPackageJson),
    removeUnusedDevDependencies(Test, scalaJSBundlerPackageJson),

    clean := {
      val logger = streams.value.log
      doClean(logger, Seq(managedDirectory.value, target.value), cleanKeepFiles.value)
    },
  ) ++
    inConfig(Compile)(configSettings) ++
    inConfig(Test)(configSettings)

  private def removeUnusedDevDependencies(config: ConfigKey,
                                          packageJsonTask: TaskKey[BundlerFile.PackageJson]
                                         ): Def.Setting[Task[BundlerFile.PackageJson]] = {
    import com.fasterxml.jackson.databind.ObjectMapper
    import com.fasterxml.jackson.databind.node._

    import scala.collection.JavaConverters._

    config / packageJsonTask := {
      val packageJsonFile = (config / packageJsonTask).value
      val mapper = new ObjectMapper()
      val json = mapper.readTree(IO.read(packageJsonFile.file)).asInstanceOf[ObjectNode]
      val devDeps = json.get("devDependencies").asInstanceOf[ObjectNode]
      devDeps.remove(List(
        "source-map-loader",
        "webpack",
        "concat-with-sourcemaps",
        "webpack-cli",
        "webpack-dev-server",
        "websql"
      ).asJava)

      mapper.writerWithDefaultPrettyPrinter().writeValue(packageJsonFile.file, json)

      packageJsonFile
    }
  }

  private def doClean(logger: Logger, clean: Seq[File], preserve: Seq[File]): Unit = {
    val filesToPreserve = preserve.toSet
    clean.foreach(delete(logger, _, filesToPreserve))
  }

  private def delete(logger: Logger, file: File, preserve: Set[File]): Unit = {
    try {
      FileTreeView.default.list(file.toPath).foreach {
        case (dir, attrs) if attrs.isDirectory =>
          val dirFile = dir.toFile
          if (!preserve.contains(dirFile)) {
            delete(logger, dirFile, preserve)
          }
          else logger.info(s"keep: $dirFile")
        case (f, _) if !preserve.contains(f.toFile) =>
          try Files.deleteIfExists(f)
          catch {
            case _: IOException =>
          }
        case (f, _) => logger.info(s"keep: $f")
      }
    } catch {
      case _: IOException => // Silently fail to preserve legacy behavior.
    }

    if (!preserve.contains(file)) {
      try Files.deleteIfExists(file.toPath)
      catch {
        case _: IOException =>
      }
    }
    else logger.info(s"keep: $file")
  }
}
