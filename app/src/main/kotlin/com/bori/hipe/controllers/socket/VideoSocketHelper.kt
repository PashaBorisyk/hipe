package com.bori.hipe.controllers.socket

import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileDescriptor
import java.net.InetSocketAddress
import java.nio.channels.SocketChannel

class VideoSocketHelper(
        val host: String,
        val port: Int,
        private val socketAddress: InetSocketAddress = InetSocketAddress(host, port),
        private val clientSocketChannel: SocketChannel = SocketChannel.open()) {

    private companion object {
        private const val TAG = "VideoSocketHelper"
    }

    fun getSocketFileDescriptor(): FileDescriptor {
        Log.d(TAG, "VideoSocketHelper.getSocketFileDescriptor")

        if (!clientSocketChannel.isOpen)
            throw IllegalStateException("Socket must be opened")

        if (!clientSocketChannel.isConnected)
            throw IllegalStateException("Socket must be connected")

        if (clientSocketChannel.socket().isOutputShutdown)
            throw IllegalStateException("OutputStream must be opened")

        return ParcelFileDescriptor.fromSocket(clientSocketChannel.socket()).fileDescriptor

    }

    fun connect() {
        Log.d(TAG, "VideoSocketHelper.connect")
        clientSocketChannel.connect(socketAddress)
    }

    fun isConnected() {
        Log.d(TAG, "VideoSocketHelper.isConnected")
        clientSocketChannel.isConnected
    }

    fun close() {
        Log.d(TAG, "VideoSocketHelper.close")
        clientSocketChannel.close()
    }

}