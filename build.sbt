// properties

val SCALA_VERSION = "2.10.4"

val APP_VERSION = "1.0.0-SNAPSHOT"

val SPARK_VERSION = "1.3.0"

// settings

name := "ignition"

organization := "com.uralian"

version := APP_VERSION

scalaVersion := SCALA_VERSION

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xlint", "-Ywarn-dead-code", "-language:_", "-target:jvm-1.7", "-encoding", "UTF-8")

resolvers += "typesafe repo" at "http://repo.typesafe.com/typesafe/releases/"

parallelExecution in Test := false

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)(Seq(_))

unmanagedSourceDirectories in Test <<= (scalaSource in Test)(Seq(_))

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

libraryDependencies ++= Seq(
  "org.scala-lang"            % "scala-reflect"              % SCALA_VERSION,
  "com.typesafe"              % "config"                     % "1.2.1",
  "com.eaio.uuid"             % "uuid"                       % "3.2",
  "joda-time"                 % "joda-time"                  % "2.7",
  "org.joda"                  % "joda-convert"               % "1.7",
  "com.squants"              %% "squants"                    % "0.4.2",
  "org.mvel"                  % "mvel2"                      % "2.2.4.Final",
  "io.gatling"               %% "jsonpath"                   % "0.6.2",
  "ch.qos.logback"            % "logback-classic"            % "1.1.1",
  "org.apache.spark"         %% "spark-core"                 % SPARK_VERSION
		exclude("org.slf4j", "slf4j-log4j12"),  
  "org.apache.spark"         %% "spark-streaming"            % SPARK_VERSION,
  "org.apache.spark"         %% "spark-streaming-kafka"      % SPARK_VERSION,
  "org.apache.spark"         %% "spark-sql"                  % SPARK_VERSION,
  "com.datastax.cassandra"    % "cassandra-driver-core"      % "2.1.4",
  "org.apache.cassandra"      % "cassandra-all"              % "2.1.2",
  "com.datastax.spark"       %% "spark-cassandra-connector"  % "1.1.1",
  "org.cassandraunit"         % "cassandra-unit"             % "2.0.2.2",
  "org.specs2"               %% "specs2"                     % "2.3.12"         % "test",
  "org.mockito"               % "mockito-all"                % "1.9.5"          % "test",
  "org.scalacheck"           %% "scalacheck"                 % "1.11.3"         % "test",
  "com.novocode"              % "junit-interface"            % "0.7"            % "test->default"
)
