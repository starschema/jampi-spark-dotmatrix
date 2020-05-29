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
import com.starschema.jampi.nio.{PeerConnection, PeerMessage}
import org.apache.log4j.Logger

import scala.reflect.ClassTag


object DotProduct {
  @transient lazy val log = Logger.getLogger(getClass.getName)


  protected def log(processorInfo: ProcessorInfo, x: Any): Unit = {
//    println(processorInfo.pos + ": " + x)
    log.info(processorInfo.pos + ": " + x)
  }

  // Call the appropriate matrix multiplier, according to the T type
  // only Int and Float support as of now
  def mmul(p: Integer, sa: Array[_], sb: Array[_], sc:Array[_]): Unit =
    (sa,sb,sc) match {
      case (a: Array[Int], b: Array[Int], c: Array[Int]) => DotProductVector.mmulPanama(p,a,b,c)
      case (a: Array[Float], b: Array[Float], c: Array[Float]) => DotProductVector.mmulPanama(p,a,b,c)
      case _ => throw new UnsupportedOperationException
    }

  def dotProduct[T : ClassTag](pos: Integer, p: Integer, sa: Array[T], sb: Array[T]): Array[T] = {
    val pi = CartesianTopology.getPosition(pos, p)
    val sc = new Array[T](sa.length)
//    val c_result = new Array[T](sa.length)


    // XXX: not sure, it could happen that left is source and right is dest
    val initialHorizontalSa = PeerConnection.connectPier(pi.pos, "localhost", pi.initial.left).get
    val initialVerticalSa = PeerConnection.connectPier(pi.pos + 10000, "localhost", pi.initial.up + 10000).get

    log(pi, "Sending sa to " + pi.initial.left + ", receiving from " + pi.initial.right)
    PeerMessage.shiftArray(initialHorizontalSa,sa)
    PeerConnection.close(initialHorizontalSa)

    log(pi, "Sending sb to " + pi.initial.up + ", receiving from " + pi.initial.down)
    PeerMessage.shiftArray(initialVerticalSa ,sa)
    PeerConnection.close(initialVerticalSa)


    val horizontalSa = PeerConnection.connectPier(pi.pos, "localhost", pi.neighbors.left).get
    val verticalSa = PeerConnection.connectPier(pi.pos + 10000, "localhost", pi.neighbors.up + 10000).get

    for (i <- 0 to pi.p_sqrt - 1) {
      mmul( Math.sqrt(sa.length).toInt, sa, sb, sc)

      // XXX: not sure we need it
      //DotProductVector.sumVectors(sc,c_result)

      // last shift is not required if we don't use sa/sb
      log(pi, "iter " + i + " sending sa to " + pi.neighbors.left + ", receiving from " + pi.neighbors.right)
      PeerMessage.shiftArray(horizontalSa, sa)
      log(pi, "iter " + i + " sending sb to " + pi.neighbors.up + ", receiving from " + pi.neighbors.down)
      PeerMessage.shiftArray(verticalSa, sb)
    }

    PeerConnection.close(horizontalSa)
    PeerConnection.close(verticalSa)

    sc
  }


  def main(args: Array[String]): Unit = {
    val sa =  Array.fill[Int](64*64) {scala.util.Random.nextInt(1000) }
    val sb =  Array.fill[Int](64*64) {1}
    val fsa =  Array.fill[Float](64*64) {scala.util.Random.nextInt(1000) }
    val fsb =  Array.fill[Float](64*64) {1}

    val res = dotProduct(0, 1, sa, sb)
    val res2 = dotProduct(0, 1, fsa, fsb)

  }
}
