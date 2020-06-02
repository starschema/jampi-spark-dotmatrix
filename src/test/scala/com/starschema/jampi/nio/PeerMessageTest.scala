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

import java.nio.{ByteBuffer, IntBuffer}

import org.scalatest._
import org.scalatest.matchers.should.Matchers._

import scala.util.Try

class PeerMessageTest extends FunSuite {

  def getIntBuffers(size: Int,fill: => Integer) : (ByteBuffer,ByteBuffer) = {
    val destBuffer = ByteBuffer.allocate(4 * size * size)

    val source = new Array[Byte](4 * size * size)
    val random = Array.fill[Int](size * size) {fill}
    val sourceBuffer = ByteBuffer.wrap(source)
    sourceBuffer
      .asIntBuffer()
      .put(random)

    (sourceBuffer, destBuffer)
  }

  def getRandomIntBuffers(size: Int) : (ByteBuffer,ByteBuffer) = getIntBuffers(size, scala.util.Random.nextInt(1000))

  def socketsShouldBeOpen(sp: PeerConnection, boolean: Boolean) = {
    sp.clientSocket.get.isOpen should be (boolean)

    sp.clientServerSocket match {
      case Some(clientServerSocket) => clientServerSocket.isOpen should be (boolean)
      case None => assert(boolean == false)
    }
  }

  test("Connect pier local (1-thread)") {
    // connect sockets
    val sp = PeerConnection.connectPier(1111,"127.0.0.1",1111).get
    socketsShouldBeOpen(sp,true)

    // close sockets
    PeerConnection.close(sp)
    socketsShouldBeOpen(sp,false)
  }

  test("Shift data single thread")
  {
    val (sourceBuffer,destBuffer) = getRandomIntBuffers(128)
    val conn = PeerConnection
      .connectPier( 1111,"127.0.0.1",1111)
      .get
      .copy(sendBuffer=sourceBuffer, receiveBuffer=destBuffer)

    // connect sockets
    val sp = PeerMessage.shiftBuffer(conn)
    socketsShouldBeOpen(sp.get,true)

    assert( sourceBuffer === destBuffer)

    // close sockets
    PeerConnection.close(sp.get)
    socketsShouldBeOpen(sp.get,false)
  }

  test("Shift data 2-threads") {

    case class ThreadProcessor(sourcePort: Int, destPort: Int) extends Runnable {

      def run {
        val (sourceBuffer,destBuffer) = getIntBuffers(64,sourcePort)
        val conn = PeerConnection
          .connectPier( sourcePort,"127.0.0.1",destPort)
          .get
          .copy(sendBuffer=sourceBuffer, receiveBuffer=destBuffer)

        val sp = PeerMessage.shiftBuffer(conn)
        socketsShouldBeOpen(sp.get,true)

        // check first element
        destBuffer.rewind()
        sp.get.receiveBuffer.asIntBuffer().get(0) should be (destPort)

        // check last element
        destBuffer.rewind()
        destBuffer.asIntBuffer().get( sourceBuffer.limit() / 4 - 1) should be (destPort)

        // close sockets
        PeerConnection.close(sp.get)
        socketsShouldBeOpen(sp.get,false)
      }

    }

    val tp1 = new Thread( ThreadProcessor(1211,1212) )
    val tp2 = new Thread( ThreadProcessor(1212,1211) )

    tp1.start()
    tp2.start()
    tp1.join()
    tp2.join()

  }

  test("Array shift ") {
    val size = 64
    val sp = PeerConnection.connectPier( 1111,"127.0.0.1",1111)
    val array = Array.fill[Int] (size*size) {0}

    PeerMessage.shiftArray(sp.get, array)

  }

  test("Buffer tests ")  {
    val inta = Array[Int](1,2,3,4,5)
    val a2 = new Array[Int] (100)

    val bb = ByteBuffer.allocateDirect(100)

    bb.asIntBuffer().put(inta)

    bb.put("1234".getBytes())
    val a = bb.asIntBuffer().get(a2)

   // bb.asIntBuffer().
  }
}
