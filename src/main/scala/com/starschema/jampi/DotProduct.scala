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
package com.starschema.jampi

import com.starschema.jampi.blas.DotProductVector
import com.starschema.jampi.nio.{ShiftData, SocketPool}

import scala.reflect.ClassTag


object DotProduct {

  protected def log(processorInfo: ProcessorInfo, x: Any): Unit = {
    println(processorInfo.pos + ": " + x)
  }

  // Call the appropriate matrix multiplier, according to the T type
  // only Int and Float support as of now
  def mmul[T:Numeric](p: Integer, sa: Array[T], sb: Array[T], sc:Array[T]) =
    sa match {
      case a if a.isInstanceOf[Array[Int]] => DotProductVector.mmulPanama(p,
        sa.asInstanceOf[Array[Int]],
        sb.asInstanceOf[Array[Int]],
        sc.asInstanceOf[Array[Int]])
      case a if a.isInstanceOf[Array[Float]] => DotProductVector.mmulPanama(p,
        sa.asInstanceOf[Array[Float]],
        sb.asInstanceOf[Array[Float]],
        sc.asInstanceOf[Array[Float]])
      case _ => throw new UnsupportedOperationException
    }

  def dotProduct[T:Numeric](pos: Integer, p: Integer, sa: Array[T], sb: Array[T])(implicit m: ClassTag[T]): Unit = {
    val pi = CartesianTopology.getPosition(pos, p)
    val sc = new Array[T](sa.length)


    // XXX: not sure, it could happen that left is source and right is dest
    val initialHorizontalSa = ShiftData.connectPier(pi.pos, "localhost", pi.initial.left)
    val initialVerticalSa = ShiftData.connectPier(pi.pos + 10000, "localhost", pi.initial.up + 10000)

    log(pi, "Sending sa to " + pi.initial.left + ", receiving from " + pi.initial.right)
    ShiftData.shiftArray(initialHorizontalSa,sa)
    SocketPool.close(initialHorizontalSa)

    log(pi, "Sending sb to " + pi.initial.up + ", receiving from " + pi.initial.down)
    ShiftData.shiftArray(initialVerticalSa ,sa)
    SocketPool.close(initialVerticalSa)


    val horizontalSa = ShiftData.connectPier(pi.pos, "localhost", pi.neighbors.left)
    val verticalSa = ShiftData.connectPier(pi.pos + 10000, "localhost", pi.neighbors.up + 10000)

    for (i <- 0 to pi.p_sqrt - 1) {
      mmul( Math.sqrt(sa.length).toInt, sa, sb, sc)
      //XXX: vector sum sc + partial_sc

      // last shift is not required if we don't use sa/sb
      log(pi, "iter " + i + " sending sa to " + pi.neighbors.left + ", receiving from " + pi.neighbors.right)
      ShiftData.shiftArray(horizontalSa, sa)
      log(pi, "iter " + i + " sending sb to " + pi.neighbors.up + ", receiving from " + pi.neighbors.down)
      ShiftData.shiftArray(verticalSa, sb)
    }

    SocketPool.close(horizontalSa)
    SocketPool.close(verticalSa)

  }


  def main(args: Array[String]): Unit = {
    val sa =  Array.fill[Int](64*64) {0}
    val sb =  Array.fill[Int](64*64) {0}

    dotProduct(0, 1, sa, sb)

  }
}
