package com.starschema.jampi.spark

import com.starschema.jampi.DotProduct
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.BarrierTaskContext


object SparkMatrixDotProductExample {
  @transient lazy val log = Logger.getLogger(getClass.getName)

  def main(args: Array[String]) {
    val size = 2560
    val numExecutors= 16

    val spark = SparkSession.builder
      .appName("JAMPI dotMatrix Multiplication")
      .config("spark.master", f"local[${numExecutors}]")
      .config("spark.eventLog.dir", "file:/tmp/spark-events")
      .config("spark.eventLog.enabled",true)
      .getOrCreate()

    val sc = spark.sparkContext


    lazy val input = Array.fill[Double] (size * size) {1}
    val rdd = sc
      .parallelize( Vector.fill[Array[Double]] (numExecutors) { Array.emptyDoubleArray } , numExecutors )
      .map(_ => (input,input))

    val foo = rdd.barrier().mapPartitions { iter =>
      val context = BarrierTaskContext.get()
      log.info("partition id" + context.partitionId())

      val (matrixA,matrixB) = iter.next()

      DotProduct.dotProduct( context.partitionId(), numExecutors, matrixA, matrixB).iterator
    }

    val ret = foo.mean()

    log.info(s"ret head is ${ret} while expected is ${size * Math.sqrt(numExecutors)}")

    spark.stop()
  }
}

