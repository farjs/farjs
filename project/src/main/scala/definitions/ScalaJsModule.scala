package definitions

import common.{Libs, TestLibs}
import org.scalajs.jsenv.Input.{CommonJSModule, ESModule}
import org.scalajs.linker.interface.ESVersion
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.xml.sax.helpers.{AttributesImpl, XMLFilterImpl, XMLReaderFactory}
import org.xml.sax.{Attributes, InputSource}
import sbt._
import sbt.Keys._
import sbt.nio.file.FileTreeView
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scalajsbundler.util.JSON
import scommons.sbtplugin.project.CommonNodeJsModule
import scoverage.ScoverageKeys.coverageReport

import java.io.{IOException, StringReader}
import java.nio.file.Files
import javax.xml.XMLConstants
import javax.xml.transform.{OutputKeys, TransformerFactory}
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult

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

    npmExtraArgs := Seq("--install-links=true"),

    //disable local npm install command to use root node_modules
    Compile / npmUpdate := {
      val _ = (Compile / scalaJSBundlerPackageJson).value
      (Compile / npmUpdate / crossTarget).value
    },
    Test / npmUpdate := {
      (Test / npmUpdate / crossTarget).value
    },
    
    Test / additionalNpmConfig := Map(
      "type" -> JSON.str("module")
    ),

    // to avoid this limitation:
    //  scalaJSModuleKind must be set to ModuleKind.CommonJSModule in projects where ScalaJSBundler plugin is enabled
    ensureModuleKindIsCommonJSModule := true,

    clean := {
      val logger = streams.value.log
      doClean(logger, Seq(managedDirectory.value, target.value), cleanKeepFiles.value)
    },

    coverageReport := {
      coverageReport.value
      addSrcPathToCoberturaXml(name.value.stripPrefix("farjs-"))
    },
  ) ++
    inConfig(Compile)(configSettings) ++
    inConfig(Test)(configSettings)

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

  private def addSrcPathToCoberturaXml(module: String): Unit = {
    val xmlFile = new File(s"./$module/target/scala-2.13/coverage-report/cobertura.xml")
    if (xmlFile.exists()) {
      val xr = new XMLFilterImpl(XMLReaderFactory.createXMLReader) {
        override def startElement(uri: String, localName: String, qName: String, atts: Attributes): Unit = {
          if (!qName.startsWith("source")) {
            val newAtts =
              if (qName == "class") {
                val atts2 = new AttributesImpl(atts)
                val idx = atts2.getIndex("filename")
                val path = atts2.getValue(idx)
                atts2.setValue(idx, s"$module/src/main/scala/$path")
                atts2
              }
              else atts

            super.startElement(uri, localName, qName, newAtts)
          }
        }

        override def endElement(uri: String, localName: String, qName: String): Unit = {
          if (!qName.startsWith("source")) {
            super.endElement(uri, localName, qName)
          }
        }

        override def characters(ch: Array[Char], start: Int, length: Int): Unit = {
          //super.characters(ch, start, length);
        }
      }

      val xmlStr = IO.read(xmlFile).stripPrefix(
        """<?xml version="1.0"?>
          |<!DOCTYPE coverage SYSTEM "http://cobertura.sourceforge.net/xml/coverage-04.dtd">
          |""".stripMargin)

      val src = new SAXSource(xr, new InputSource(new StringReader(xmlStr)))
      val res = new StreamResult(xmlFile)
      val transformer = TransformerFactory.newInstance.newTransformer
      transformer.setOutputProperty(OutputKeys.INDENT, "yes")
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
      transformer.transform(src, res)
    }
  }
}
