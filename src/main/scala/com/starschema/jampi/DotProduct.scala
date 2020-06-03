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
    log.info(processorInfo.pos + ": " + x)
  }

  // Use type information instead of parameter overloads
  def mmul(p: Integer, sa: Array[_], sb: Array[_], sc:Array[_]): Unit =
    (sa,sb,sc) match {
      case (a: Array[Int], b: Array[Int], c: Array[Int]) => DotProductVector.mmulPanama(p,a,b,c)
      case (a: Array[Float], b: Array[Float], c: Array[Float]) => DotProductVector.mmulPanama(p,a,b,c)
//      case (a: Array[Double], b: Array[Double], c: Array[Double]) => DotProductVector.mmulPanama(p,a,b,c)
      case (a: Array[Double], b: Array[Double], c: Array[Double]) => DotProductVector.fastBuffered_v2(p,a,b,c)
      //case (a: Array[Double], b: Array[Double], c: Array[Double]) => DotProductVector.mmul_naive(a,b,c,p)
      case _ => throw new UnsupportedOperationException
    }

  def dotProduct[T : ClassTag](pos: Integer,
                               p: Integer,
                               sa: Array[T],
                               sb: Array[T],
                               hostMap: Array[String]): Array[T] =
  {
    val pi = CartesianTopology.getPosition(pos, p)
    val sc = new Array[T](sa.length)

    val initialHorizontalSa = PeerConnection.connectPeer(pi.pos, hostMap(pi.initial.left), pi.initial.left).get
    val initialVerticalSa = PeerConnection.connectPeer(pi.pos + 10000, hostMap(pi.initial.up), pi.initial.up + 10000).get

    log(pi, "Sending sa to " + pi.initial.left + ", receiving from " + pi.initial.right)
    PeerMessage.shiftArray(initialHorizontalSa,sa)
    PeerConnection.close(initialHorizontalSa)

    log(pi, "Sending sb to " + pi.initial.up + ", receiving from " + pi.initial.down)
    PeerMessage.shiftArray(initialVerticalSa ,sa)
    PeerConnection.close(initialVerticalSa)

    // TODO: fix port numbers
    val horizontalSa = PeerConnection.connectPeer(pi.pos + 1000, hostMap(pi.neighbors.left), pi.neighbors.left + 1000).get
    val verticalSa = PeerConnection.connectPeer(pi.pos + 11000, hostMap(pi.neighbors.up), pi.neighbors.up + 11000).get

    for (i <- 0 until pi.p_sqrt) {
      mmul( Math.sqrt(sa.length).toInt, sa, sb, sc)

      if (i != pi.p_sqrt-1 ) {
        // shift is not required in last iteration
        log(pi, "iter " + i + " sending sa to " + pi.neighbors.left + ", receiving from " + pi.neighbors.right)
        PeerMessage.shiftArray(horizontalSa, sa)
        log(pi, "iter " + i + " sending sb to " + pi.neighbors.up + ", receiving from " + pi.neighbors.down)
        PeerMessage.shiftArray(verticalSa, sb)
      }
    }

    PeerConnection.close(horizontalSa)
    PeerConnection.close(verticalSa)

    sc
  }

}
