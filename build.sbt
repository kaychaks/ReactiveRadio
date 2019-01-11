lazy val akkaVersion = "2.4.12"
lazy val catsVersion = "0.8.1"
lazy val akkaHttpVersion = "10.0.0"

lazy val commonSettings = Seq(
  name := "ReactiveRadio",
  version := "0.01",
  organization := "radio",
  cancelable in Global := true,
  libraryDependencies ++= {
    Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
//      "com.typesafe.akka" % "akka-http-experimental_2.11" % akkaHttpVersion,
//      "com.typesafe.akka" % "akka-http-spray-json-experimental_2.11" % akkaHttpVersion,
      "org.typelevel" %% "cats" % catsVersion,
      "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
      "org.scalacheck" %% "scalacheck" % "1.13.2" % "test"
    )
  },
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-Xfatal-warnings",
    "-feature",
    "-language:_",
    "-language:higherKinds",
    "-language:postfixOps"
  )
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*)
lazy val v1 = (project in file("v1"))
  .settings(commonSettings: _*)
  .settings(
  Seq(
    scalaVersion := "2.12.0",
    ensimeJavaFlags := Seq("-Xss2m", "-Xms1024m", "-Xmx2048m", "-XX:ReservedCodeCacheSize=256m", "-XX:MaxMetaspaceSize=512m"),
    ensimeScalaVersion := "2.11.8"
  )
)
