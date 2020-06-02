name := "jampi-spark-dotmatrix"

version := "1.0"
scalaVersion := "2.12.10"

javaOptions += "--add-modules jdk.incubator.vector -XX:TypeProfileLevel=121"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.2" % Test
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.0-preview2"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.0.0-preview2"
libraryDependencies += "com.github.fommil.netlib" % "all" % "1.1.2" pomOnly()



