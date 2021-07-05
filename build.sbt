name := "agryeel"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "org.apache.commons" % "commons-math3" % "3.0",
  "org.apache.commons" % "commons-email" % "1.3.1"
)

play.Project.playJavaSettings