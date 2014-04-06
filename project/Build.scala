import sbt._
import Keys._

object ProjectBuild extends Build {

  override lazy val settings = super.settings ++
  Seq(scalaVersion := "2.10.4", resolvers := Seq())


  val appDependencies = Seq(
    "com.github.nscala-time" %% "nscala-time" % "0.6.0"
    )

  lazy val root = Project(
    id = "timesheet",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      libraryDependencies ++= appDependencies)
    )

}
