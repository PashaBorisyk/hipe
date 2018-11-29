package com.bori.hipe.controllers.socket

import android.annotation.TargetApi
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.os.Build.VERSION_CODES.JELLY_BEAN
import android.os.ParcelFileDescriptor
import android.support.annotation.RequiresApi
import android.util.Log
import android.util.Size
import android.view.Surface
import io.reactivex.Observable
import java.io.FileDescriptor
import java.net.InetSocketAddress
import java.net.URL
import java.nio.channels.SocketChannel

@TargetApi(JELLY_BEAN)
class VideoTranslationHelper(
        private val url: URL) {

    private companion object {
        private const val TAG = "VideoTranslationHelper"
        private const val timeoutSec = 1L
    }

    @Volatile
    var shouldRun = true

    private lateinit var encoder: MediaCodec
    private val clientSocketChannel: SocketChannel = SocketChannel.open()
    private val bufferInfo = MediaCodec.BufferInfo()


    fun getSocketFileDescriptor(): FileDescriptor {
        Log.d(TAG, "VideoTranslationHelper.getSocketFileDescriptor")

        if (!clientSocketChannel.isOpen)
            throw IllegalStateException("Socket must be opened")

        if (!clientSocketChannel.isConnected)
            throw IllegalStateException("Socket must be connected")

        if (clientSocketChannel.socket().isOutputShutdown)
            throw IllegalStateException("OutputStream must be opened")

        return ParcelFileDescriptor.fromSocket(clientSocketChannel.socket()).fileDescriptor

    }

    fun createSocketConnection(): Observable<Unit> {
        Log.d(TAG, "VideoTranslationHelper.createSocketConnection")
        return Observable.create {
            val socketAddress = InetSocketAddress(url.host, url.port)

            clientSocketChannel.configureBlocking(true)
            clientSocketChannel.connect(socketAddress)
            clientSocketChannel.finishConnect()
            Log.d(TAG, "VideoTranslationHelper.createSocketConnection finished. Connected : ${clientSocketChannel.isConnected}")
            it.onNext(Unit)
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun prepareCodec(size: Size): Observable<Pair<Surface, Size>> = Observable.create {
        Log.d(TAG, "VideoTranslationHelper.prepareCodec")

        val colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        val videoBitrate = 300000
        val videoFramePerSecond = 30
        val iFrameInterval = 2

        val format = MediaFormat.createVideoFormat("video/avc", size.width, size.height)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, videoFramePerSecond)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval)

        encoder = MediaCodec.createEncoderByType("video/avc")

        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = MediaCodec.createPersistentInputSurface()
        encoder.setInputSurface(surface)
        encoder.start()

        it.onNext(surface to size)

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun startEncoding() = Observable.create<Boolean> {
        Log.d(TAG, "VideoTranslationHelper.startEncoding")


        if (shouldRun)
            encoder.signalEndOfInputStream()

        it.onNext(shouldRun)

        while (true) {

            val status = encoder.dequeueOutputBuffer(bufferInfo, timeoutSec)

            if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!shouldRun) break

            } else if (status == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                Log.d(TAG, "VideoTranslationHelper.startEncoding OUTPUT_BUFFERS_CHANGED")
            } else if (status < 0) {

            } else {
                Log.d(TAG, "VideoTranslationHelper.startEncoding writing buffers")

                val data = encoder.getOutputBuffer(status)
                data.position(bufferInfo.offset)
                data.limit(bufferInfo.offset + bufferInfo.size)
                clientSocketChannel.configureBlocking(false)
                Log.d(TAG, "VideoTranslationHelper.startEncoding buffer : $data")
                clientSocketChannel.write(data)
                encoder.releaseOutputBuffer(status, false)

                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    Log.d(TAG, "VideoTranslationHelper.startEncoding BUFFER_FLAG_END_OF_STREAM")

                }

            }

        }

        it.onComplete()

    }

    fun isConnected(): Boolean {
        Log.d(TAG, "VideoTranslationHelper.isConnected")
        return clientSocketChannel.isConnected
    }

    fun close() {
        Log.d(TAG, "VideoTranslationHelper.close")
        clientSocketChannel.close()
        encoder.release()
    }

}