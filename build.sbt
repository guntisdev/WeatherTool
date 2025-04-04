ThisBuild / version := "0.1.1-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "WeatherTool",
    Compile / mainClass := Some("Main"),

    // Assembly settings for consistent JAR name
    assembly / assemblyJarName := "app.jar",

    // Merge strategy for assembly to handle conflicts
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case "module-info.class" => MergeStrategy.discard
      case x if x.endsWith(".conf") => MergeStrategy.concat
      case x => MergeStrategy.first
    }
  )

val doobieVersion = "1.0.0-RC1"
val http4sVersion = "0.23.18"
val circeVersion = "0.14.1"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-server" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,

  "commons-net" % "commons-net" % "3.9.0",

  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,

  "ch.qos.logback" % "logback-classic" % "1.2.9",
  "org.scala-lang" % "scala-reflect" % "2.13.10",
  "com.github.pureconfig" %% "pureconfig" % "0.17.4",

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-optics" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "org.scalatest" %% "scalatest" % "3.2.16" % Test,
  "org.scalamock" %% "scalamock" % "5.2.0" % Test
)
