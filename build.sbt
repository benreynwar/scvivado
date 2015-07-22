scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
		    "edu.berkeley.cs" %% "chisel" % "latest.release",
		    "commons-io" % "commons-io" % "2.4",
		    "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
		    "org.slf4j" % "slf4j-api" % "1.7.5",
		    "org.slf4j" % "slf4j-simple" % "1.7.5",
		    "org.clapper" %% "grizzled-slf4j" % "1.0.2")