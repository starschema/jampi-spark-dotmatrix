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

import org.scalatest._
import org.scalatest.matchers.should.Matchers._

class DotProductTest extends FunSuite   {

  test("int, double and float 64x64 matrices filled with 1") {
    val size = 64

    val hostMap = Array[String] ("127.0.0.1")
    val i = Array.fill[Int](size * size) { 1 }
    val f = Array.fill[Float](size * size) { 1 }
    val d = Array.fill[Double](size * size) { 1 }

    val iresult = DotProduct.dotProduct(0, 1, i, i, hostMap)
    val fresult = DotProduct.dotProduct(0, 1, f, f, hostMap)
    val dresult = DotProduct.dotProduct(0, 1, d, d, hostMap)

    iresult should contain only size
    fresult should contain only size
    dresult should contain only size
  }

  test("int, double and float large matrices filled with 1") {
    val size = 1024
    val hostMap = Array[String] ("127.0.0.1")

    val i = Array.fill[Int](size * size) { 1 }
    val f = Array.fill[Float](size * size) { 1 }
    val d = Array.fill[Double](size * size) { 1 }

    val iresult = DotProduct.dotProduct(0, 1, i, i, hostMap)
    val fresult = DotProduct.dotProduct(0, 1, f, f, hostMap)
    val dresult = DotProduct.dotProduct(0, 1, d, d, hostMap)

    iresult should contain only size
    fresult should contain only size
    dresult should contain only size

  }
}
