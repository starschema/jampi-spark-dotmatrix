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

case class GridLocation(left: Int, right: Int, up: Int, down: Int)

case class ProcessorInfo(p: Int, pos: Int, coords: (Int, Int), initial: GridLocation, neighbors: GridLocation)

object CartesianTopology {

  def shiftPos(pos: Int, p_sqrt: Int, x: Int, y: Int, hOffs: Int, vOffs: Int): GridLocation = {
    val p = p_sqrt * p_sqrt

    // left/right neighbors, unless it is on edge, then the other side of the row
    val left = if (y - hOffs >= 0) pos - hOffs else pos + p_sqrt - hOffs
    val right = if (y >= p_sqrt - hOffs) pos - p_sqrt + hOffs else pos + hOffs

    // top/down neighbors, opposite side of the matrix when on edge
    val up = if ((pos + p_sqrt * vOffs) >= p) pos + p_sqrt * vOffs - p else pos + p_sqrt * vOffs
    val down = if (pos - p_sqrt * vOffs < 0) pos - p_sqrt * vOffs + p else pos - p_sqrt * vOffs

    GridLocation(left, right, up, down)
  }

  def posToCoords(pos: Int, p_sqrt: Int): (Int, Int) = {
    (Math.floor(pos / p_sqrt).intValue(), pos % p_sqrt)
  }

  def getPosition(pos: Int, p: Int): ProcessorInfo = {
    val p_sqrt = (Math sqrt p).intValue()
    val (x, y) = posToCoords(pos, p_sqrt)

    ProcessorInfo(p, pos, (x, y),
      shiftPos(pos, p_sqrt, x, y, x, y),
      shiftPos(pos, p_sqrt, x, y, 1, 1))
  }

}

object Test {
  def main(args: Array[String]): Unit = {
  }

}

