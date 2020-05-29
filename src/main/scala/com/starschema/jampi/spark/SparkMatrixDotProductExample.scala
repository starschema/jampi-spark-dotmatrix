package com.starschema.jampi.spark

import com.starschema.jampi.DotProduct
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.BarrierTaskContext


object SparkMatrixDotProductExample {
  @transient lazy val log = Logger.getLogger(getClass.getName)

  def main(args: Array[String]) {
    val size = 64

    val spark = SparkSession.builder
      .appName("JAMPI dotMatrix Multiplication")
      .config("spark.master", "local[4]")
      .config("spark.eventLog.dir", "file:/tmp/spark-events")
      .config("spark.eventLog.enabled",true)
      .getOrCreate()

    val sc = spark.sparkContext

    val input = Array.fill[Int] (64 * 64) {1}
    val rdd = sc.parallelize( Vector(input,input,input,input) , 4 )

    val foo = rdd.barrier().mapPartitions { iter =>
      val context = BarrierTaskContext.get()
      log.info("partition id" + context.partitionId())

      val matrix = iter.next()

      DotProduct.dotProduct( context.partitionId(), 4, matrix, matrix).iterator
    }

    val ret = foo.collect()

    spark.stop()
  }
}

