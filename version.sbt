version in ThisBuild := sys.env.getOrElse("version", default = "0.1.0-SNAPSHOT").stripPrefix("v")
