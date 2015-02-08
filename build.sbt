scalaVersion := "2.11.5"
libraryDependencies ++= Seq(
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
    "net.databinder.dispatch" %% "dispatch-json4s-native" % "0.11.0",
    "org.json4s" %% "json4s-native" % "3.2.11",
    "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "com.typesafe" % "config" % "1.2.1"
  )
