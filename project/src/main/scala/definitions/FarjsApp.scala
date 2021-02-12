package definitions

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys.coverageExcludedPackages

import scalajsbundler.BundlingMode
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

object FarjsApp extends ScalaJsModule {

  override val id = "farjs-app"

  override val base: File = file("app")

  override def definition: Project = super.definition
    .settings(
      skip in publish := true,
      publish := ((): Unit),
      publishLocal := ((): Unit),
      publishM2 := ((): Unit),

      coverageExcludedPackages := {
        "farjs.app.FarjsApp" +
          ";farjs.app.FarjsActions"
      },

      scalaJSUseMainModuleInitializer := false,
      webpackBundlingMode := BundlingMode.LibraryOnly(),
      version in webpack := "4.29.0",

      webpackResources := {
        baseDirectory.value / ".." / "LICENSE.txt" +++
          baseDirectory.value / ".." / "README.md"
      },
      
      //dev
      webpackConfigFile in fastOptJS := Some(
        baseDirectory.value / "src" / "main" / "resources" / "dev.webpack.config.js"
      ),
      //prod
      webpackConfigFile in fullOptJS := Some(
        baseDirectory.value / "src" / "main" / "resources" / "dev.webpack.config.js"
      ),
      //tests
      //webpackConfigFile in Test := Some(
      //  baseDirectory.value / "src" / "test" / "resources" / "test.webpack.config.js"
      //),

      //useYarn := true,
      //yarnExtraArgs := Seq("--frozen-lockfile"),

      npmDevDependencies in Compile ++= Seq(
        "webpack-merge" -> "4.1.0",
        "webpack-node-externals" -> "1.7.2",

        "@babel/cli" -> "^7.0.0",
        "@babel/core" -> "^7.0.0",
        "@babel/plugin-proposal-class-properties" -> "^7.7.0",
        "@babel/plugin-proposal-object-rest-spread" -> "^7.0.0",
        "@babel/plugin-transform-flow-strip-types" -> "^7.0.0",
        "@babel/preset-env" -> "^7.0.0",
        "@babel/preset-react" -> "^7.0.0",
        "@babel/register" -> "^7.0.0",
        "rollup" -> "^0.65.0",
        "rollup-plugin-babel" -> "^4.0.2",
        "rollup-plugin-commonjs" -> "^9.1.6",
        "rollup-plugin-node-resolve" -> "^3.3.0",
        "rollup-plugin-peer-deps-external" -> "^2.2.0"
      ),

      additionalNpmConfig in Compile := {
        import scalajsbundler.util.JSON._
        Map(
          "name" -> str("farjs-app"),
          "version" -> str(version.value),
          "description" -> str("File and Archive Manager (FAR) app built with Scala.js/React.js and runs on Node.js"),
          "scripts" -> obj(
            "build" -> str("rollup -c")
          ),
          "repository" -> obj(
            "type" -> str("git"),
            "url" -> str("git+https://github.com/scommons/far-js.git")
          ),
          "bugs" -> obj(
            "url" -> str("https://github.com/scommons/far-js/issues")
          ),
          "homepage" -> str("https://github.com/scommons/far-js#readme"),
          "browserslist" -> str("maintained node versions"),
          "private" -> bool(false),
          "license" -> str("MIT"),
          "author" -> str("viktor-podzigun"),
          "keywords" -> arr(
            str("farmanager"),
            str("react"),
            str("reactjs"),
            str("react-js"),
            str("react-blessed"),
            str("blessed"),
            str("scala"),
            str("scalajs"),
            str("scala-js"),
            str("cli"),
            str("cli-app"),
            str("terminal"),
            str("xterm"),
            str("file-manager"),
            str("filemanager"),
            str("filemanager-ui"),
            str("filemanagement"),
            str("file-edit"),
            str("file-editor"),
            str("editor"),
            str("console"),
            str("tui"),
            str("text-ui")
          ),
          "bin" -> obj(
            "farjs" -> str("bin/farjs.js")
          ),
          "files" -> arr(
            str("bin/farjs.js"),
            str("build/far.js"),
            str("LICENSE.txt"),
            str("README.md")
          )
        )
      }
    )

  override val internalDependencies: Seq[ClasspathDep[ProjectReference]] = Seq(
    FarjsFileList.definition
  )
}
