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

// https://mvnrepository.com/artifact/com.typesafe.play/play-ws-standalone-json
libraryDependencies += "com.typesafe.play" %% "play-ws-standalone-json" % "1.1.10"

// https://mvnrepository.com/artifact/com.typesafe.play/play-ahc-ws-standalone
libraryDependencies += "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.10"

mainClass in assembly := Some("org.zella.maryana.ui.MainApp")

test in assembly := {}

