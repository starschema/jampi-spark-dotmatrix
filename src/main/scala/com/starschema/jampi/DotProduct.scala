package com.starschema.jampi

object DotProduct {

  def log(processorInfo: ProcessorInfo, x: Any): Unit = {
    println( processorInfo.pos + ": " + x )
  }


  def dotProduct( pos: Integer, p: Integer, sa: Array[Byte], sb: Array[Byte] ): Unit = {
    val pi = CartesianTopology.getPosition(pos,p)

    // XXX: not sure, it could happen that left is source and right is dest
    log(pi, "Sending sa to " + pi.initial.left + ", receiving from " + pi.initial.right )
    log(pi, "Sending sb to " + pi.initial.up + ", receiving from " + pi.initial.down )


    for (i <- 0 to pi.p_sqrt ) {
      log(pi, "iter " + i + " sending sa to " + pi.neighbors.left + ", receiving from " + pi.neighbors.right )
      log(pi, "iter " + i + " sending sb to " + pi.neighbors.up + ", receiving from " + pi.neighbors.down )
    }

  }


  def main(args: Array[String]): Unit = {

    dotProduct(9,16, "".getBytes(), "".getBytes())
  }
}
