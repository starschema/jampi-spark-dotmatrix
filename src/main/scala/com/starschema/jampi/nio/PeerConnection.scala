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

import java.net.{ConnectException, InetSocketAddress, StandardSocketOptions}
import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousServerSocketChannel, AsynchronousSocketChannel}
import java.util.concurrent.TimeUnit

// Network state
case class PeerConnection(serverSocket: AsynchronousServerSocketChannel,
                          clientSocket: AsynchronousSocketChannel,
                          clientServerSocket: Option[AsynchronousSocketChannel],
                          sendBuffer: ByteBuffer,
                          receiveBuffer: ByteBuffer)

// Companion class to manage network state
object PeerConnection {
  private val TIMEOUT = 10L
  private val DIRECT_BUFFER_LEN = 8 * 1024 * 1024

  def getPeerConnection: PeerConnection = PeerConnection(
    serverSocket = AsynchronousServerSocketChannel.open,
    clientSocket = AsynchronousSocketChannel.open(),
    clientServerSocket =  None,
    ByteBuffer.allocateDirect(DIRECT_BUFFER_LEN),
    ByteBuffer.allocateDirect(DIRECT_BUFFER_LEN)
  )

  def listenServerOnPort(port: Int)(implicit socketPool: PeerConnection) = {
    socketPool
      .serverSocket
      .setOption[java.lang.Boolean](StandardSocketOptions.SO_REUSEPORT, true)
      .bind(new InetSocketAddress("0.0.0.0", port))
    this
  }

  def connectToHost(destHost: String, destPort: Int)(implicit socketPool: PeerConnection) = {
    // connect to remote host
    val dest = new InetSocketAddress(destHost, destPort)
    val fClient = socketPool.clientSocket
      .setOption[java.lang.Boolean](StandardSocketOptions.SO_KEEPALIVE, true)
      .setOption[java.lang.Boolean](StandardSocketOptions.TCP_NODELAY, true)
      .connect(dest)

    // accept client from remote location
    val clientServer = socketPool.serverSocket.accept().get(TIMEOUT, TimeUnit.SECONDS)

    // make sure our client connection is accepted remotely
    try {
      fClient.get()
    } catch {
      case e: Exception => {
        println("Exception, sleeping a bit")
        Thread.sleep(1000)
        socketPool.clientSocket.connect(dest).get()
      }
    }

    // add clientServerSocket to our connection state
    socketPool.copy(clientServerSocket = Some(clientServer) )
  }

  def close(implicit socketPool: PeerConnection): Unit = {
    if (socketPool.clientSocket.isOpen) socketPool.clientSocket.close()
    if (socketPool.serverSocket.isOpen) socketPool.serverSocket.close()

    socketPool.clientServerSocket match {
      case Some(s) => if (s.isOpen) s.close()
      case None =>
    }
  }

}

