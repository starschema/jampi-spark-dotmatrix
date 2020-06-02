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


import com.starschema.jampi.blas.DotProductVector

import java.nio.ByteBuffer
import java.nio.file.{Files, Paths}

import org.scalatest._
import org.scalatest.matchers.should.Matchers._


class DotProductVectorTest extends FunSuite   {

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }



  test("int 64x64 matrixes filled with 1") {
    val size = 64

    val m = Array.fill[Int](size * size) { 1 }
    val result = Array.fill[Int](size * size) { 0 }

    DotProductVector.mmulPanama(size, m, m, result)

    result should contain only size
  }

  test("compare 64x64 int results with naive implementation") {
    val size = 64

    val random1 = Array.fill[Int](size * size) { scala.util.Random.nextInt(1000) }
    val random2 = Array.fill[Int](size * size) { scala.util.Random.nextInt(1000) }
    val results1 = new Array[Int](size * size)
    val results2 = new Array[Int](size * size)

    time( DotProductVector.mmul_naive(random1,random2,results1,size) )
    time( DotProductVector.mmulPanama(size,random1,random2,results2) )

    assert( results1.deep == results2.deep )
  }

  test("compare 1024x1024 double results with naive implementation") {
    val size = 1024
    val Eps = 1e-3.toFloat

    val random1 = Array.fill[Double](size * size) { scala.util.Random.nextDouble() % 10000 }
    val random2 = Array.fill[Double](size * size) { scala.util.Random.nextDouble() % 10000 }
    val results1 = new Array[Double](size * size)
    val results2 = new Array[Double](size * size)
    val results3 = new Array[Double](size * size)

    time( DotProductVector.mmul_naive(random1,random2,results1,size) )
    time( DotProductVector.mmulPanama(size,random1,random2,results2) )
    time( DotProductVector.fastBuffered(size,random1,random2,results3) )

    //assert( results1.deep == results2.deep )
    for (i <- 0 until results1.size) results1(i) should be (results2(i) +- Eps)
    for (i <- 0 until results1.size) results1(i) should be (results3(i) +- Eps)
  }

  test("compare 64x64 float results with naive implementation") {
    val size = 1024
    val Eps = 1e-3.toFloat // Our epsilon
    val random1 = Array.fill[Float](size * size) { scala.util.Random.nextFloat }
    val random2 = Array.fill[Float](size * size) { scala.util.Random.nextFloat }
    val results1 = new Array[Float](size * size)
    val results2 = new Array[Float](size * size)

    time(DotProductVector.mmul_naive(random1,random2,results1,size))
    time(DotProductVector.mmulPanama(size,random1,random2,results2))

    for (i <- 0 until results1.size) results1(i) should be (results2(i) +- Eps)
  }


  test("int 64x64 matrix from/to file") {
    val size = 64
    val file = Paths.get("src/test/resources/64x64-i2.matrix")
    val random = Array.fill[Int](size * size) { scala.util.Random.nextInt(1000) }

    val byteArray = new Array[Byte](4 * size * size + 8)

    val buf = ByteBuffer.wrap(byteArray)
    buf
      .asIntBuffer()
      .put(random)

    Files.write(file, byteArray)
  }


}
