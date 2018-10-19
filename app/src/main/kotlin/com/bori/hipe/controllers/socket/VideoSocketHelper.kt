package com.bori.hipe.controllers.socket

import android.os.ParcelFileDescriptor
import java.io.FileDescriptor
import java.net.InetSocketAddress
import java.nio.channels.SocketChannel

class VideoSocketHelper(
        val host:String,
        val port:Int,
        private val socketAddress:InetSocketAddress = InetSocketAddress(host,port),
        private val clientSocketChannel: SocketChannel = SocketChannel.open()){

    fun getSocketFileDescriptor():FileDescriptor {

        if(!clientSocketChannel.isOpen)
            throw IllegalStateException("Socket must be opened")

        if(!clientSocketChannel.isConnected)
            throw IllegalStateException("Socket must be connected")

        if(clientSocketChannel.socket().isOutputShutdown)
            throw IllegalStateException("OutputStream must be opened")

        return ParcelFileDescriptor.fromSocket(clientSocketChannel.socket()).fileDescriptor

    }

    fun connect() = clientSocketChannel.connect(socketAddress)

    fun isConnected() = clientSocketChannel.isConnected

    fun close() = clientSocketChannel.close()

}