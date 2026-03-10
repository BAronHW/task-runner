ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.18"

val catsEffectVersion = "3.6.3"
val circeVersion      = "0.14.15"
val fs2Version        = "3.10.2"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect"    % catsEffectVersion,
  "co.fs2"        %% "fs2-core"       % fs2Version,
  "co.fs2"        %% "fs2-io"         % fs2Version,
  "io.circe"      %% "circe-core"     % circeVersion,
  "io.circe"      %% "circe-generic"  % circeVersion,
  "io.circe"      %% "circe-parser"   % circeVersion,
  "io.circe"      %% "circe-yaml"     % "1.15.0",
  "com.monovore"  %% "decline-effect" % "2.4.1"
)

lazy val root = (project in file("."))
  .settings(
    name := "task-runner"
  )