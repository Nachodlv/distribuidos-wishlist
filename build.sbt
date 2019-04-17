import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "wishlist",
    libraryDependencies += scalaTest % Test
  )

libraryDependencies ++= Seq(
  "com.typesafe"                 % "config"                 % "1.3.0"                            
    withSources() withJavadoc(),
  "com.typesafe.slick"          %% "slick"                  % "3.3.0",
  "org.slf4j"                    % "slf4j-nop"              % "1.6.4",
  "com.h2database"               % "h2"                     % "1.4.199"
)


// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.

libraryDependencies ++= Seq(
  "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
)

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

libraryDependencies += "com.github.mingchuno" %% "etcd4s-core" % "0.2.0"
//libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.26" % Test
