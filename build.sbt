ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"

lazy val root = (project in file("."))
  .settings(
    name := "timesheet",
    idePackagePrefix := Some("timesheet"),
    libraryDependencies += "net.java.dev.jna" % "jna" % "5.13.0",
    libraryDependencies += "net.java.dev.jna" % "jna-platform" % "5.13.0",
    libraryDependencies += "joda-time" % "joda-time" % "2.12.2",
    libraryDependencies += "org.yaml" % "snakeyaml" % "1.33",
    assemblyMergeStrategy := {
      case PathList("META-INF", _@_*) => MergeStrategy.discard
      case x => MergeStrategy.first
    },
    assembly / logLevel := Level.Warn,
    assembly / mainClass := Some("timesheet.CurrentWindowWorker"),
    assembly / assemblyJarName := "../timesheet.jar",
    publish / skip := true,
  )

