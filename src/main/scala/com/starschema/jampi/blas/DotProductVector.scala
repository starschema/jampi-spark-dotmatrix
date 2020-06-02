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
package com.starschema.jampi.blas

import jdk.incubator.vector.{DoubleVector, FloatVector, IntVector}

object DotProductVector {

  val I128 = IntVector.SPECIES_128
  val I256 = IntVector.SPECIES_256
  val F256  = FloatVector.SPECIES_256
  val D256  = DoubleVector.SPECIES_256
  val D512 = DoubleVector.SPECIES_512

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


  /*
  * based on source from: https://github.com/richardstartin/vectorbenchmarks/blob/master/src/main/java/com/openkappa/panama/vectorbenchmarks/IntMatrixMatrixMultiplication.java#L45
  */
  def mmulPanama(n: Int, left: Array[Double], right: Array[Double], result: Array[Double]): Unit = {
    val blockWidth = if (n >= 256) 512 else 256
    val blockHeight = if (n >= 512) 8 else if (n >= 256) 16 else 32

    var columnOffset = 0
    while ( {columnOffset < n }) {
      var rowOffset = 0
      while ( {rowOffset < n }) {
        for (i <- 0 until n) {
          var j = columnOffset
          while ( {j < columnOffset + blockWidth && j < n }) {
            var sum1 = DoubleVector.fromArray(D512, result, i * n + j)
            var sum2 = DoubleVector.fromArray(D512, result, i * n + j + 8)
            var sum3 = DoubleVector.fromArray(D512, result, i * n + j + 16)
            var sum4 = DoubleVector.fromArray(D512, result, i * n + j + 24)
            var sum5 = DoubleVector.fromArray(D512, result, i * n + j + 32)
            var sum6 = DoubleVector.fromArray(D512, result, i * n + j + 40)
            var sum7 = DoubleVector.fromArray(D512, result, i * n + j + 48)
            var sum8 = DoubleVector.fromArray(D512, result, i * n + j + 56)
            var k = rowOffset
            while ( {k < rowOffset + blockHeight && k < n }) {
              val multiplier = DoubleVector.broadcast(D512, left(i * n + k))
              sum1 = multiplier.fma(DoubleVector.fromArray(D512, right, k * n + j), sum1)
              sum2 = multiplier.fma(DoubleVector.fromArray(D512, right, k * n + j + 8), sum2)
              sum3 = multiplier.fma(DoubleVector.fromArray(D512, right, k * n + j + 16), sum3)
              sum4 = multiplier.fma(DoubleVector.fromArray(D512, right, k * n + j + 24), sum4)
              sum5 = multiplier.fma(DoubleVector.fromArray(D512, right, k * n + j + 32), sum5)
              sum6 = multiplier.fma(DoubleVector.fromArray(D512, right, k * n + j + 40), sum6)
              sum7 = multiplier.fma(DoubleVector.fromArray(D512, right, k * n + j + 48), sum7)
              sum8 = multiplier.fma(DoubleVector.fromArray(D512, right, k * n + j + 56), sum8)

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


  def mmul_naive[T:Numeric](a: Array[T], b: Array[T], c: Array[T], n: Int): Unit = {
    import Numeric.Implicits._

    var in = 0
    for (i <- 0 until n) {
      var kn = 0
      for (k <- 0 until n) {
        val aik = a(in + k)
        for (j <- 0 until n) {
          c(in + j) += aik * b(kn + j)
        }
        kn += n
      }
      in += n
    }
  }

  def fastBuffered(n: Int, a: Array[Double], b: Array[Double], c: Array[Double]): Unit = {
    val bBuffer = new Array[Double](n)
    val cBuffer = new Array[Double](n)
    var in = 0
    for (i <- 0 until n) {
      var kn = 0
      for (k <- 0 until n) {
        val aik = a(in + k)
        System.arraycopy(b, kn, bBuffer, 0, n)
        saxpy(n, aik, bBuffer, cBuffer)
        kn += n
      }
      System.arraycopy(cBuffer, 0, c, in, n)
      java.util.Arrays.fill(cBuffer, 0f)
      in += n
    }
  }


  private def saxpy(n: Int, aik: Double, b: Array[Double], c: Array[Double]): Unit = {
    for (i <- 0 until n) {
      c(i) = Math.fma(aik, b(i), c(i))
    }
  }
}
