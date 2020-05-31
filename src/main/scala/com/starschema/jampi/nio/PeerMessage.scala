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

  private def netlog(loggerFunct: String => Unit, message: String)(implicit conn: PeerConnection) {
    loggerFunct( s"src=${conn.serverSocket.getLocalAddress()} dst=${conn.destinationAddress}" ++
      s" rp:${conn.receiveBuffer.position()}" ++
      s"/${conn.receiveBuffer.limit()}" ++
      s" sp:${conn.sendBuffer.position()}" ++
      s"/${conn.sendBuffer.limit()} -- " ++
      message)
  }

  def shiftArray(sp: PeerConnection, sourceArray: Array[_]): Try[PeerConnection] = {

    // TODO: implement iteration
    sourceArray match {
      case sa:Array[Int] => {
        sp.sendBuffer.rewind().asIntBuffer.put(sa)
        sp.sendBuffer.limit(sa.length*4)
        sp.receiveBuffer.limit(sa.length*4)
      }
      case sa:Array[Float] => {
        sp.sendBuffer.rewind().asFloatBuffer.put(sa)
        sp.sendBuffer.limit(sa.length*8)
      }
      case _ => throw new NotImplementedError("Only float and integers are supported")
    }

    val new_sp = shiftBuffer(sp) : Try[PeerConnection]

    // TODO: error handling
    sourceArray match {
      case sa:Array[Int] => {
        netlog(log.trace,"Converting buffer byte->int")(sp)
        sp.receiveBuffer.rewind().asIntBuffer.get(sa)
      }
      case sa:Array[Float] => sp.receiveBuffer.flip.asFloatBuffer.get(sa)
    }

    sp.receiveBuffer.clear()

    new_sp
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

              netlog(log.trace,s"Read ${receivedBytes} bytes")(conn)
              netlog(log.trace,s"Write ${sentBytes} bytes ")(conn)
            }
            conn
          }
        }
        case _ => throw new IllegalStateException("Socket should be open at this point")
      }

  }

}
