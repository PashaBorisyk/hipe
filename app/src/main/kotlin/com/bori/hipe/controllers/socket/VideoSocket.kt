package com.bori.hipe.controllers.socket

import android.net.LocalServerSocket
import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.util.Log
import io.reactivex.Observable
import java.io.FileDescriptor
import java.io.InputStreamReader

class VideoSocket(
        val host: String,
        val port: Int) {

    private companion object {
        private const val TAG = "VideoSocket"
    }

    private lateinit var localSocket: LocalSocket

    fun getSocketFileDescriptor(): FileDescriptor {
        Log.d(TAG, "VideoSocket.getSocketFileDescriptor")

        if (!localSocket.isBound)
            throw IllegalStateException("Socket must be opened")

        if (!localSocket.isConnected)
            throw IllegalStateException("Socket must be connected")

        if (localSocket.isOutputShutdown)
            throw IllegalStateException("OutputStream must be opened")

        return localSocket.fileDescriptor

    }

    fun connect(): Observable<LocalSocket> {
        Log.d(TAG, "VideoSocket.connect")
        return Observable.create {

            val localServerSocket = LocalServerSocket("hipe.socket")

            val localSocket = LocalSocket()
            localSocket.connect(LocalSocketAddress("hipe.socket", LocalSocketAddress.Namespace.ABSTRACT))
            localSocket.sendBufferSize = 1024

            val socket = localServerSocket.accept()
            it.onNext(socket)
            Log.d(TAG, "Socket connected : ${socket.isConnected}")
            Log.d(TAG, "Recieved buffer size = ${socket.receiveBufferSize}")
            val inputStreamReader = InputStreamReader(socket.inputStream)


            var prevSize = 0

            while (true) {
                val read = inputStreamReader.read()
                if (read != prevSize) {
                    Log.e(TAG, "Read new size of bytes : $read")
                    prevSize = read
                }
            }

            socket.close()
            localServerSocket.close()
        }

    }

    fun isConnected(): Boolean {
        Log.d(TAG, "VideoSocket.isConnected")
        return true
    }

    fun close() {
        Log.d(TAG, "VideoSocket.close")
        localSocket.close()
    }

}