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

import java.net.{InetSocketAddress, StandardSocketOptions}
import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousServerSocketChannel, AsynchronousSocketChannel}

object ShiftData {

  def connectPier(source: Int, destHost: String, destPort: Int): SocketPool = {
    implicit val sockets : SocketPool = SocketPool.getEmptySocketPool

    SocketPool
      .listenServerOnPort(1111)
      .connectToHost(destHost, destPort)
  }

  def shiftData(source: Int, dest: Int, buffer: ByteBuffer): Unit = {

    // server socket
    val listenAddr = new InetSocketAddress("0.0.0.0", 1111)
    val serverSocket = AsynchronousServerSocketChannel.open()
    serverSocket.bind(listenAddr)

    val fClient = serverSocket.accept()


    // client connect
    val peerAddr = new InetSocketAddress("127.0.0.1", 1111)

    val client = AsynchronousSocketChannel
      .open()
      .setOption[java.lang.Boolean](StandardSocketOptions.SO_KEEPALIVE, true)
      .setOption[java.lang.Boolean](StandardSocketOptions.TCP_NODELAY, true)
    client.connect(peerAddr).get()

    // send with future
    val future = client.write(buffer)

    // receive with future
    val buf = ByteBuffer.allocate(10)
    val r = fClient.get().read(buf)

    println(r.get())

    // wait
    println(future.get())


    client.close()
  }

  def main(args: Array[String]): Unit = {
    val message = "lol".getBytes()
    val buffer = ByteBuffer.wrap(message)

    shiftData(0, 0, buffer)
  }
}
