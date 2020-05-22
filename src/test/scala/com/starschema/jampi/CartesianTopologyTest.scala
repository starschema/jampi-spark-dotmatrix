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


class CartesianTopologyTest extends FunSuite {
  test("8x8 matrix should return the correct neighbors") {
    val t22 = CartesianTopology.getPosition(22, 64)
    assert( t22.pos == 22 )
    assert( t22.p == 64)
    assert( t22.coords == (2,6) )
    assert( t22.initial == GridLocation(20,16,6,38) )
    assert( t22.neighbors == GridLocation(21,23,30,14) )

    val t23 = CartesianTopology.getPosition(0, 64)
    assert( t23.pos == 0 )
    assert( t23.p == 64)
    assert( t23.coords == (0,0) )
    assert( t23.initial == GridLocation(0,0,0,0) )
    assert( t23.neighbors == GridLocation(7,1,8,56) )

    /*
    println(CartesianTopology.getPosition(22, 64))
    println(CartesianTopology.getPosition(23, 64))
    println(CartesianTopology.getPosition(59, 64))
    println(CartesianTopology.getPosition(0, 64))
    println(CartesianTopology.getPosition(63, 64))
    */

  }
}
