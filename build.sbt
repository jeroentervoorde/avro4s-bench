lazy val root = (project in file(".")).
  enablePlugins(JmhPlugin).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "avro4s-bench",
    libraryDependencies ++= Seq(
      "com.sksamuel.avro4s" %% "avro4s-core" % "2.0.4-SNAPSHOT"
    )
  )
