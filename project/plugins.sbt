//resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

//use patched versions by now, to make scoverage work with scalajs-bundler
addSbtPlugin(("org.scommons.patched" % "sbt-scalajs-bundler" % "0.14.0-SNAPSHOT").force())

addSbtPlugin(("org.scommons.sbt" % "sbt-scommons-plugin" % "1.0.0-SNAPSHOT").changing())

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.29")
