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

import jdk.incubator.vector.{FloatVector, IntVector}
import jdk.incubator.vector.IntVector


object DotProduct {

  val I128 = IntVector.SPECIES_128
  val I256 = IntVector.SPECIES_256
  val F256  = FloatVector.SPECIES_256

  /*
   * Source: https://github.com/richardstartin/vectorbenchmarks/blob/master/src/main/java/com/openkappa/panama/vectorbenchmarks/IntMatrixMatrixMultiplication.java#L45
   */
  def mmulPanama(n: Int, left: Array[Int], right: Array[Int], result: Array[Int]): Unit = {
    val blockWidth = if (n >= 256) 512 else 256
    val block_height = if (n >= 512) 8 else if (n >= 256) 16 else 32
    var columnOffset = 0

    while ( { columnOffset < n }) {
      var rowOffset = 0
      while ( { rowOffset < n }) {
        for (i <- 0 until n) {
          var j = columnOffset
          while ( { j < columnOffset + blockWidth && j < n }) {
            var sum1 = IntVector.fromArray(I256, result, i * n + j)
            var sum2 = IntVector.fromArray(I256, result, i * n + j + 8)
            var sum3 = IntVector.fromArray(I256, result, i * n + j + 16)
            var sum4 = IntVector.fromArray(I256, result, i * n + j + 24)
            var sum5 = IntVector.fromArray(I256, result, i * n + j + 32)
            var sum6 = IntVector.fromArray(I256, result, i * n + j + 40)
            var sum7 = IntVector.fromArray(I256, result, i * n + j + 48)
            var sum8 = IntVector.fromArray(I256, result, i * n + j + 56)
            var k = rowOffset
            while ( { k < rowOffset + block_height && k < n } ) {
              val multiplier = IntVector.broadcast(I256, left(i * n + k))
              sum1 = sum1.add(multiplier.mul(IntVector.fromArray(I256, right, k * n + j)))
              sum2 = sum2.add(multiplier.mul(IntVector.fromArray(I256, right, k * n + j + 8)))
              sum3 = sum3.add(multiplier.mul(IntVector.fromArray(I256, right, k * n + j + 16)))
              sum4 = sum4.add(multiplier.mul(IntVector.fromArray(I256, right, k * n + j + 24)))
              sum5 = sum5.add(multiplier.mul(IntVector.fromArray(I256, right, k * n + j + 32)))
              sum6 = sum6.add(multiplier.mul(IntVector.fromArray(I256, right, k * n + j + 40)))
              sum7 = sum7.add(multiplier.mul(IntVector.fromArray(I256, right, k * n + j + 48)))
              sum8 = sum8.add(multiplier.mul(IntVector.fromArray(I256, right, k * n + j + 56)))

              k += 1
            }
            sum1.intoArray(result, i * n + j)
            sum2.intoArray(result, i * n + j + 8)
            sum3.intoArray(result, i * n + j + 16)
            sum4.intoArray(result, i * n + j + 24)
            sum5.intoArray(result, i * n + j + 32)
            sum6.intoArray(result, i * n + j + 40)
            sum7.intoArray(result, i * n + j + 48)
            sum8.intoArray(result, i * n + j + 56)

            j += 64
          }
        }

        rowOffset += block_height
      }

      columnOffset += blockWidth
    }
  }

  /*
   * Source: https://github.com/richardstartin/vectorbenchmarks/blob/master/src/main/java/com/openkappa/panama/vectorbenchmarks/FloatMatrixMatrixMultiplication.java
   */
  def mmulPanama(n: Int, left: Array[Float], right: Array[Float], result: Array[Float]): Unit = {
    val blockWidth = if (n >= 256) 512 else 256
    val blockHeight = if (n >= 512) 8 else if (n >= 256) 16 else 32

    var columnOffset = 0
    while ( {columnOffset < n }) {
      var rowOffset = 0
      while ( {rowOffset < n }) {
        for (i <- 0 until n) {
          var j = columnOffset
          while ( {j < columnOffset + blockWidth && j < n }) {
            var sum1 = FloatVector.fromArray(F256, result, i * n + j)
            var sum2 = FloatVector.fromArray(F256, result, i * n + j + 8)
            var sum3 = FloatVector.fromArray(F256, result, i * n + j + 16)
            var sum4 = FloatVector.fromArray(F256, result, i * n + j + 24)
            var sum5 = FloatVector.fromArray(F256, result, i * n + j + 32)
            var sum6 = FloatVector.fromArray(F256, result, i * n + j + 40)
            var sum7 = FloatVector.fromArray(F256, result, i * n + j + 48)
            var sum8 = FloatVector.fromArray(F256, result, i * n + j + 56)
            var k = rowOffset
            while ( {k < rowOffset + blockHeight && k < n }) {
              val multiplier = FloatVector.broadcast(F256, left(i * n + k))
              sum1 = multiplier.fma(FloatVector.fromArray(F256, right, k * n + j), sum1)
              sum2 = multiplier.fma(FloatVector.fromArray(F256, right, k * n + j + 8), sum2)
              sum3 = multiplier.fma(FloatVector.fromArray(F256, right, k * n + j + 16), sum3)
              sum4 = multiplier.fma(FloatVector.fromArray(F256, right, k * n + j + 24), sum4)
              sum5 = multiplier.fma(FloatVector.fromArray(F256, right, k * n + j + 32), sum5)
              sum6 = multiplier.fma(FloatVector.fromArray(F256, right, k * n + j + 40), sum6)
              sum7 = multiplier.fma(FloatVector.fromArray(F256, right, k * n + j + 48), sum7)
              sum8 = multiplier.fma(FloatVector.fromArray(F256, right, k * n + j + 56), sum8)

              k += 1
            }
            sum1.intoArray(result, i * n + j)
            sum2.intoArray(result, i * n + j + 8)
            sum3.intoArray(result, i * n + j + 16)
            sum4.intoArray(result, i * n + j + 24)
            sum5.intoArray(result, i * n + j + 32)
            sum6.intoArray(result, i * n + j + 40)
            sum7.intoArray(result, i * n + j + 48)
            sum8.intoArray(result, i * n + j + 56)

            j += 64
          }
        }

        rowOffset += blockHeight
      }

      columnOffset += blockWidth
    }
  }

  def log(processorInfo: ProcessorInfo, x: Any): Unit = {
    println(processorInfo.pos + ": " + x)
  }


  def dotProduct(pos: Integer, p: Integer, sa: Array[Byte], sb: Array[Byte]): Unit = {
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
