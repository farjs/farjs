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
    ideExcludedDirectories += baseDirectory.value / "docs" / "_site"
  )
  .aggregate(
    `farjs-ui`,
    `farjs-dao`,
    `farjs-filelist`,
    `farjs-fs`,
    `farjs-archiver`,
    `farjs-viewer`,
    `farjs-app`
  )

lazy val `farjs-ui` = FarjsUi.definition
lazy val `farjs-dao` = FarjsDao.definition
lazy val `farjs-filelist` = FarjsFileList.definition
lazy val `farjs-fs` = FarjsFs.definition
lazy val `farjs-archiver` = FarjsArchiver.definition
lazy val `farjs-viewer` = FarjsViewer.definition
lazy val `farjs-app` = FarjsApp.definition
