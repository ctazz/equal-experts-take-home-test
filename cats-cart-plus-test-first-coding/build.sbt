name := "cart-step-4"

organization := "org.cart"

version := "0.8.2-SNAPSHOT"

scalaVersion := "2.13.2"

scalacOptions in Compile ++= Seq("-encoding", "UTF-8",
  "-Ydelambdafy:method",
  "-deprecation",
  "-unchecked",
  "-Ywarn-dead-code",
  "-feature",
  "-language:postfixOps",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xfatal-warnings",
  "-Xlint",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-unused"
)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.8.0",
  "org.scalatest"               %%  "scalatest"         % "3.2.13" % "test",
  "org.scalamock" %% "scalamock" % "4.4.0" % "test"
)


resolvers ++= Seq(
  "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases",
  "Scala-tools" at "https://oss.sonatype.org/content/repositories/snapshots"
)


parallelExecution in Test := false



