import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.7",
      version := "0.1.0-SNAPSHOT"
    )),
    name := "adb-app-uninstaller",
    libraryDependencies += scalaTest % Test
  )

libraryDependencies += "com.softwaremill.sttp" %% "okhttp-backend-monix" % "1.3.5"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.10"

libraryDependencies += "io.monix" %% "monix" % "2.3.3"

mainClass in assembly := Some("org.zella.maryana.ui.MainApp")

test in assembly := {}
