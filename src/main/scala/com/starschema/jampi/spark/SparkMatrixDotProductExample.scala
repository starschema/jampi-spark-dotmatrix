/*
Copyright (c) 2020, Starschema Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.starschema.jampi.spark

import com.starschema.jampi.DotProduct
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.BarrierTaskContext


object SparkMatrixDotProductExample {
  @transient lazy val log = Logger.getLogger(getClass.getName)

  def main(args: Array[String]) {
    val numExecutors=  if (args.size > 1) args(1).toInt else  16
    val size = if (args.size > 0) (args(0).toInt / Math.sqrt(numExecutors) ).toInt else 1024

    val spark = SparkSession.builder
      .appName("JAMPI dotMatrix Multiplication")
//      .config("spark.master", f"local[${numExecutors}]")
      .getOrCreate()

    val sc = spark.sparkContext


    lazy val input = Array.fill[Double] (size * size) {1}
    val rdd = sc
      .parallelize( Vector.fill[Array[Double]] (numExecutors) { Array.emptyDoubleArray } , numExecutors )
      .map(_ => (input,input))

    val foo = rdd.barrier().mapPartitions { iter =>
      val context = BarrierTaskContext.get()
      val hostMap = context.getTaskInfos().map(_.address.split(':')(0))

      log.info(f"Starting barrier task partitionId=${context.partitionId} on executor=${hostMap(context.partitionId)}")

      val (matrixA,matrixB) = iter.next()

      DotProduct.dotProduct(context.partitionId(), numExecutors, matrixA, matrixB,hostMap).iterator
    }

    val ret = LogElapsed.log("jampi",size * Math.sqrt(numExecutors).toInt,numExecutors,
      foo.mean()
    )

    log.info(s"ret head is ${ret} while expected is ${size * Math.sqrt(numExecutors)}")

    sc.stop()
  }
}

