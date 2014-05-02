name := "indentation-lexical"

version := "0.1"

scalaVersion := "2.10.4"

scalacOptions ++= Seq( "-deprecation", "-feature", "-language:postfixOps", "-language:implicitConversions", "-language:existentials" )

organization := "org.funl-lang"

target := file( "/home/ed/target/" + moduleName.value )

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.0" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"

libraryDependencies ++= Seq(
	"org.funl-lang" %% "lia" % "0.1"
	)