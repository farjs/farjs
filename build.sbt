import definitions._
import scommons.sbtplugin.project.CommonModule
import scommons.sbtplugin.project.CommonModule.ideExcludedDirectories

lazy val `farjs` = (project in file("."))
  .settings(CommonModule.settings: _*)
  .settings(FarjsModule.settings: _*)
  .settings(
    publish / skip := true,
    publish := ((): Unit),
    publishLocal := ((): Unit),
    publishM2 := ((): Unit)
  )
  .settings(
    ideExcludedDirectories ++= {
      val base = baseDirectory.value
      List(
        base / "docs" / "_site",
        base / "node_modules"
      )
    }
  )
  .aggregate(
    `farjs-ui`,
    `farjs-filelist`,
    `farjs-archiver`,
    `farjs-app`
  )

lazy val `farjs-ui` = FarjsUi.definition
lazy val `farjs-filelist` = FarjsFileList.definition
lazy val `farjs-archiver` = FarjsArchiver.definition
lazy val `farjs-app` = FarjsApp.definition
