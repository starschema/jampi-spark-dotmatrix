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


object DotProduct {

  protected def log(processorInfo: ProcessorInfo, x: Any): Unit = {
    println(processorInfo.pos + ": " + x)
  }

  def dotProduct[T:Numeric](pos: Integer, p: Integer, sa: Array[T], sb: Array[T]): Unit = {
    val pi = CartesianTopology.getPosition(pos, p)

    // XXX: not sure, it could happen that left is source and right is dest
    log(pi, "Sending sa to " + pi.initial.left + ", receiving from " + pi.initial.right)
    log(pi, "Sending sb to " + pi.initial.up + ", receiving from " + pi.initial.down)

    for (i <- 0 to pi.p_sqrt) {
      log(pi, "iter " + i + " sending sa to " + pi.neighbors.left + ", receiving from " + pi.neighbors.right)
      log(pi, "iter " + i + " sending sb to " + pi.neighbors.up + ", receiving from " + pi.neighbors.down)
    }

  }


  def main(args: Array[String]): Unit = {

    dotProduct(9, 16, "".getBytes(), "".getBytes())

  }
}
