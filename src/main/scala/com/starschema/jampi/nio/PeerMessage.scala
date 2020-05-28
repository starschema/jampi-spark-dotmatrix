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

import java.nio.ByteBuffer

object PeerMessage {
  val PORTBASE = 22000
  val NET_BUFFER_SIZE = 8 * 1024 * 1024

  def connectPier(source: Int, destHost: String, destPort: Int): PeerConnection = {
    implicit val sockets : PeerConnection = PeerConnection.getPeerConnection

    PeerConnection
      .listenServerOnPort(source + PORTBASE)
      .connectToHost(destHost, destPort + PORTBASE)
  }


  def shiftArray(sp: PeerConnection, sourceArray: Array[_]): PeerConnection = {

    //val sourceBuffer = ByteBuffer.allocateDirect(NET_BUFFER_SIZE)
    //val destBuffer = ByteBuffer.allocateDirect(NET_BUFFER_SIZE)
    val sourceBuffer = ByteBuffer.allocateDirect( sourceArray.length * 4) // FIXME
    val destBuffer = ByteBuffer.allocateDirect( sourceArray.length * 4) // FIXME

    // TODO: implement iteration
    sourceArray match {
      case sa:Array[Int] => sourceBuffer.asIntBuffer().put(sa)
      case sa:Array[Float] => sourceBuffer.asFloatBuffer().put(sa)
      case _ => throw new NotImplementedError("Only float and integers are supported")
    }

    val new_sp = shiftBuffer(sp, sourceBuffer, destBuffer) : PeerConnection

    sourceArray match {
      case sa:Array[Int] => destBuffer.flip().asIntBuffer().get(sa)
      case sa:Array[Float] => destBuffer.flip().asFloatBuffer().get(sa)
    }

    new_sp
  }

  @inline
  def shiftBuffer(sp: PeerConnection,
                  sourceBuffer: ByteBuffer,
                  destBuffer: ByteBuffer): PeerConnection = {

    // send with future
    val fWrite = sp.clientSocket.write(sourceBuffer)

    // receive with future
    val fRead = sp.clientServerSocket match {
      case Some(s) => s.read(destBuffer)
      case None => throw new IllegalStateException
    }

    // Sync buffers
    fRead.get()
    fWrite.get()

    sp
  }

}
