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
package com.starschema.jampi.nio

import org.apache.log4j.Logger
import scala.util.{Failure, Success, Try}

object PeerMessage {
  @transient lazy val log = Logger.getLogger(getClass.getName)

  private def trace(conn: PeerConnection, message: String) {
    log.trace( s"src=${conn.serverSocket.getLocalAddress()} dst=${conn.destinationAddress}" ++
      s" rp:${conn.receiveBuffer.position()}" ++
      s"/${conn.receiveBuffer.limit()}" ++
      s" sp:${conn.sendBuffer.position()}" ++
      s"/${conn.sendBuffer.limit()} -- " ++
      message)
  }

  def shiftArray(sp: PeerConnection, sourceArray: Array[_]): Try[PeerConnection] = {

    val (elementSize,arrayLen) = sourceArray match {
      case sa:Array[Int] => (4, sa.length)
      case sa:Array[Float] => (8, sa.length)
      case _ => throw new NotImplementedError("Only float and integers are supported")
    }

    val arrayByteSize = arrayLen * elementSize
    val maxElementsInBuffer = PeerConnection.DIRECT_BUFFER_LEN / elementSize
    val numOfIterations= Math.floor((arrayByteSize-1)/PeerConnection.DIRECT_BUFFER_LEN ).toInt

    trace(sp, s"abs=${arrayByteSize} eib=${maxElementsInBuffer}")


    // iterate over the array by buffer
    val ret_sp = (0 to numOfIterations).map{offset =>
      val elementsToShift = Math.min(maxElementsInBuffer, arrayLen - offset * maxElementsInBuffer)

      trace(sp, s"offs=${offset}, maxElem=${maxElementsInBuffer} elementsToShift=${elementsToShift}")

      // set limit for buffers - mark how much do we expect on receive side
      sp.sendBuffer.limit(elementsToShift*elementSize)
      sp.receiveBuffer.limit(elementsToShift*elementSize)

      // load sendBuffer with source array contents
      sourceArray match {
        case sa:Array[Int] =>  sp.sendBuffer.rewind().asIntBuffer.put(sa, offset * maxElementsInBuffer, elementsToShift)
        case sa:Array[Float] =>  sp.sendBuffer.rewind().asFloatBuffer.put(sa, offset * maxElementsInBuffer, elementsToShift)
      }

      //  shift buffers
      val new_sp = shiftBuffer(sp) : Try[PeerConnection]

      trace(new_sp.get, s"Buffer exchanged, serializing results (offset=${offset}, es=${elementsToShift})")

      // TODO: error handling
      sourceArray match {
        case sa:Array[Int] => sp.receiveBuffer.rewind().asIntBuffer.get(sa, offset * maxElementsInBuffer,elementsToShift)
        case sa:Array[Float] => sp.receiveBuffer.rewind().asFloatBuffer.get(sa, offset * maxElementsInBuffer,elementsToShift)
      }

      sp.receiveBuffer.clear() // FIXME: move the top?

      new_sp
    }

    ret_sp.head // TODO: look for errors
  }

  @inline
  def shiftBuffer(conn: PeerConnection): Try[PeerConnection] = {

      (conn.clientServerSocket, conn.clientSocket) match {
        case (Some(r), Some(w)) => {

          Try {
            while (conn.receiveBuffer.hasRemaining || conn.sendBuffer.hasRemaining) {

              val fRead = r.read(conn.receiveBuffer)
              val fWrite = w.write(conn.sendBuffer)

              val receivedBytes = fRead.get()
              val sentBytes  = fWrite.get

              //trace(conn, s"Read ${receivedBytes} bytes")
              //trace(conn, s"Write ${sentBytes} bytes ")
            }
            conn
          }
        }
        case _ => throw new IllegalStateException("Socket should be open at this point")
      }

  }

}
