package com.starschema.jampi.spark

import org.apache.log4j.Logger

object LogElapsed {

  @transient lazy val log = Logger.getLogger(getClass.getName)

  def log[R](caller: String, vectorSize: Int, processors: Int, block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    log.info(s"Elapsed ${(t1 - t0)} ns, ${caller}: size:${vectorSize}x${vectorSize} on ${processors} nodes")
    println(s"elapsed,${caller},${(t1 - t0)},${vectorSize},${processors},${System.currentTimeMillis()/1000}")
    result
  }

}
