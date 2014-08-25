name := "indentation-lexical"

version := "0.1"

scalaVersion := "2.11.2"

scalacOptions ++= Seq( "-deprecation", "-feature", "-language:postfixOps", "-language:implicitConversions", "-language:existentials" )

incOptions := incOptions.value.withNameHashing(true)

organization := "org.funl-lang"

//resolvers += Resolver.sonatypeRepo( "snapshots" )

resolvers += "Hyperreal Repository" at "http://hyperreal.ca/maven2"

libraryDependencies ++= Seq(
	"org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2"
	)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.3" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"

libraryDependencies ++= Seq(
	"org.funl-lang" %% "lia" % "0.12"
	)


publishMavenStyle := true

publishTo := Some( Resolver.sftp( "private", "hyperreal.ca", "/var/www/hyperreal.ca/maven2" ) )

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

licenses := Seq("LGPL" -> url("http://opensource.org/licenses/LGPL-3.0"))

homepage := Some(url("https://github.com/FunL/indentation-lexical"))

pomExtra := (
  <scm>
    <url>git@github.com:FunL/indentation-lexical.git</url>
    <connection>scm:git:git@github.com:FunL/indentation-lexical.git</connection>
  </scm>
  <developers>
    <developer>
      <id>edadma</id>
      <name>Edward A. Maxedon, Sr.</name>
      <url>http://funl-lang.org</url>
    </developer>
  </developers>)
