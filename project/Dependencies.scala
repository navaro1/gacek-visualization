import sbt._



object Dependencies {

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val akkaActor = "com.typesafe.akka" %% "akka-actor" % "2.5.25"
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.1.10"
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.5.25"
  lazy val sprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.10"
}
