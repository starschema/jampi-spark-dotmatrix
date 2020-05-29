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

import scala.util.{Failure, Success, Try}

// Network state
case class PeerConnection(serverSocket: AsynchronousServerSocketChannel,
                          clientSocket: Option[AsynchronousSocketChannel],
                          clientServerSocket: Option[AsynchronousSocketChannel],
                          destinationAddress: InetSocketAddress,
                          isLoopback: Boolean,
                          sendBuffer: ByteBuffer,
                          receiveBuffer: ByteBuffer)

// Companion class to manage network state
object PeerConnection {
  private val TIMEOUT = 10L
  private val PORTBASE = 22000
  private val DIRECT_BUFFER_LEN = 8 * 1024 * 1024

  import scala.concurrent.ExecutionContext.Implicits.global

  def getPeerConnection(dest: InetSocketAddress, isLoopback: Boolean): PeerConnection = PeerConnection(
    serverSocket = AsynchronousServerSocketChannel.open,
    clientSocket = None,
    clientServerSocket =  None,
    destinationAddress = dest,
    isLoopback = isLoopback,
    ByteBuffer.allocateDirect(DIRECT_BUFFER_LEN),
    ByteBuffer.allocateDirect(DIRECT_BUFFER_LEN)
  )

  def connectPier(sourcePort: Int, destHost: String, destPort: Int): Try[PeerConnection] = {
    val dest = new InetSocketAddress(destHost, destPort + PORTBASE)
    implicit val peerConnection : PeerConnection = PeerConnection.getPeerConnection(dest, sourcePort == destPort )

    val clientSocket = PeerConnection
      .listenServerOnPort(sourcePort + PORTBASE)
      .connectClient

    val serverConnection = acceptConnection

    if ( clientSocket.isFailure || serverConnection.isFailure )
      Failure( new java.net.ConnectException("Cannot establish peer connection") )
    else
      Success(peerConnection.copy(clientSocket = clientSocket.toOption, clientServerSocket = serverConnection.toOption))
  }

  def connectClient(implicit peerConnection: PeerConnection): Try[AsynchronousSocketChannel] = {
    //case
    val clientSocket = connectToHost

    clientSocket match {
      case Failure(exception) => {
        Thread.sleep(250)
        connectClient(peerConnection)
      }
      case success => success
    }
  }

  def connectToHost(implicit peerConnection: PeerConnection): Try[AsynchronousSocketChannel] = {
    // connect to remote host
    val socket = AsynchronousSocketChannel.open()

    val fClient = socket
      .setOption[java.lang.Boolean](StandardSocketOptions.SO_KEEPALIVE, true)
      .setOption[java.lang.Boolean](StandardSocketOptions.TCP_NODELAY, true)
      .connect( peerConnection.destinationAddress )

    // make sure our client connection is accepted remotely
    val results = Try( fClient.get(1, TimeUnit.SECONDS) )

    results match {
      case Success(v) => Success(socket)
      case Failure(ex) => Failure(ex)
    }
  }

  def listenServerOnPort(port: Int)(implicit peerConnection: PeerConnection) = {
    peerConnection
      .serverSocket
      .setOption[java.lang.Boolean](StandardSocketOptions.SO_REUSEPORT, true)
      .bind(new InetSocketAddress("0.0.0.0", port))

    this
  }

  // accept client from remote location
  def acceptConnection(implicit peerConnection: PeerConnection): Try[AsynchronousSocketChannel] = {

      peerConnection.clientServerSocket match {
          // already connected
        case Some(conn) => Success(conn)
          // let's accept the connection
        case None => Try(peerConnection.serverSocket.accept().get(TIMEOUT, TimeUnit.SECONDS))
      }

  }



  def close(implicit peerConnection: PeerConnection): Unit = {
    if (peerConnection.serverSocket.isOpen) peerConnection.serverSocket.close()

    val closeSomeChannel = {channel: Option[AsynchronousSocketChannel] =>
      channel match {
        case Some(s) => if (s.isOpen) s.close()
        case None =>
      }
    }

    closeSomeChannel(peerConnection.clientSocket)
    closeSomeChannel(peerConnection.clientServerSocket)
  }

}

