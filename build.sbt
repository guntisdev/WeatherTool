ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "WeatherTool"
  )

val http4sVersion = "0.23.16"
val circeVersion = "0.14.1"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % "0.23.9",
  "org.http4s" %% "http4s-blaze-client" % "0.23.13",
  "org.http4s" %% "http4s-circe" % http4sVersion,

  "ch.qos.logback" % "logback-classic" % "1.2.9",
  "com.typesafe" % "config" % "1.4.1",
  "org.scala-lang" % "scala-reflect" % "2.13.10",


"io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-optics" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
)
