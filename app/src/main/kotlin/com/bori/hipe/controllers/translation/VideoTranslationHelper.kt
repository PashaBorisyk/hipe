package com.bori.hipe.controllers.translation

import android.annotation.TargetApi
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.os.Build.VERSION_CODES.JELLY_BEAN
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresApi
import io.reactivex.Observable
import java.io.IOException
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.URL
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

@TargetApi(JELLY_BEAN)
class VideoTranslationHelper(
        val mode: Mode,
        private val url: URL
) {

    enum class Mode {
        ENCODE,
        DECODE
    }

    private companion object {
        private const val TAG = "VideoTranslationHelper"
        private const val timeoutSec = 1L
    }

    @Volatile
    var shouldRun = false

    private var isPrepared = false

    private lateinit var encoder: MediaCodec
    private lateinit var surface: Surface
    private lateinit var decoder: MediaCodec

    private val clientSocketChannel: SocketChannel = SocketChannel.open()
    private val bufferInfo = MediaCodec.BufferInfo()


    fun createSocketConnection() = Observable.create<Unit> {
        Log.d(TAG, "VideoTranslationHelper.createSocketConnection")
        val socketAddress = InetSocketAddress(url.host, url.port)

        clientSocketChannel.configureBlocking(true)
        try {
            clientSocketChannel.connect(socketAddress)
            clientSocketChannel.finishConnect()
            Log.d(TAG, "VideoTranslationHelper.createSocketConnection finished. Connected : ${clientSocketChannel.isConnected}")
            it.onNext(Unit)

        } catch (e: ConnectException) {
            e.printStackTrace()
            clientSocketChannel.close()
            it.onError(e)
        }
    }!!


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun prepare(size: Size, outputSurface: Surface? = null) = Observable.create<Pair<Surface, Size>> {
        Log.d(TAG, "VideoTranslationHelper.prepare")

        if (isPrepared)
            throw IllegalStateException("${this.javaClass.simpleName} is already prepared")

        val format = MediaFormat.createVideoFormat("video/avc", size.width, size.height)

        when (mode) {
            Mode.ENCODE -> {
                val colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                val videoBitrate = 300000
                val videoFramePerSecond = 30
                val iFrameInterval = 2

                format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
                format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate)
                format.setInteger(MediaFormat.KEY_FRAME_RATE, videoFramePerSecond)
                format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval)

                encoder = MediaCodec.createEncoderByType("video/avc")

                encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                surface = encoder.createInputSurface()
                encoder.start()
            }
            Mode.DECODE -> {

                outputSurface
                        ?: throw IllegalArgumentException("Only ${Mode.ENCODE.name} allows null output surface")
                surface = outputSurface
                val byteBuffer = ByteBuffer.allocate(123)
                format.setByteBuffer("csd-0", byteBuffer)
                val decoder = MediaCodec.createDecoderByType("video/avc")
                decoder.configure(format, surface, null, 0)
                decoder.start()

            }
        }

        isPrepared = true
        it.onNext(surface to size)

    }!!

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun startEncoding() = Observable.create<Boolean> {
        Log.d(TAG, "VideoTranslationHelper.startEncoding")

        if (mode != Mode.ENCODE)
            throw IllegalStateException("${this.javaClass.simpleName} was not created in ${Mode.ENCODE.name} mode")

        it.onNext(shouldRun)

        try {

            while (true) {
                encode()
            }

        } catch (e: IllegalStateException) {

            Log.e(TAG, e.message)
            e.printStackTrace()
            it.onError(e)

        } catch (e: IOException) {

            Log.e(TAG, e.message)
            e.printStackTrace()
            it.onError(e)

        } finally {

            clientSocketChannel.close()
            encoder.stop()
            encoder.release()
            it.onComplete()

        }


    }!!

    private fun encode() = Observable.create<Unit> {

        val status = encoder.dequeueOutputBuffer(bufferInfo, timeoutSec)

        if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {


        } else if (status == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            Log.d(TAG, "VideoTranslationHelper.startEncoding OUTPUT_BUFFERS_CHANGED")
        } else if (status < 0) {

        } else {

            if (shouldRun) {
                val data = encoder.getOutputBuffer(status)

                data.position(bufferInfo.offset)
                data.limit(bufferInfo.offset + bufferInfo.size)
                if (clientSocketChannel.isConnected)
                    clientSocketChannel.write(data)
            }
            encoder.releaseOutputBuffer(status, false)

            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                Log.d(TAG, "VideoTranslationHelper.startEncoding BUFFER_FLAG_END_OF_STREAM")

            }

        }

    }

    private fun decodeAndRender() = Observable.create<Unit> {

        if (mode != Mode.ENCODE)
            throw IllegalStateException("${this.javaClass.simpleName} was not created in ${Mode.DECODE.name} mode")

        try {

            it.onNext(Unit)
            val bufferInfo = MediaCodec.BufferInfo()
            while (shouldRun) {
                if (isPrepared) {

                    val index = decoder.dequeueOutputBuffer(bufferInfo, timeoutSec)
                    if (index >= 0) {

                        decoder.releaseOutputBuffer(index, bufferInfo.size > 0)
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                            shouldRun = false
                            break
                        }

                    }

                } else {
                    try {
                        Thread.sleep(10)
                    } catch (i: InterruptedException) {
                    }
                }
            }
        } finally {
            decoder.stop()
            decoder.release()
        }
    }

    fun startReadingFormSocket() = Observable.create<Unit> {

        if (mode != Mode.DECODE)
            throw IllegalStateException("${this.javaClass.simpleName} was not created in ${Mode.DECODE.name} mode")

        val size = 1024
        val buffer = ByteBuffer.allocate(size)

        it.onNext(Unit)

        while (isConnected() && shouldRun) {

            val read = clientSocketChannel.read(buffer)
            decodeSample(buffer.array(), 0, read, System.currentTimeMillis(), 0)

        }

        clientSocketChannel.close()
    }

    private fun decodeSample(data: ByteArray, offset: Int, size: Int, presentationTime: Long, flags: Int) {

        if (mode != Mode.DECODE)
            throw IllegalStateException("${this.javaClass.simpleName} was not created in ${Mode.DECODE.name} mode")

        if (isPrepared) {

            val index = encoder.dequeueInputBuffer(timeoutSec)
            if (index >= 0) {
                val buffer = decoder.getInputBuffer(index)
                buffer.clear()
                buffer.put(data, offset, size)
                decoder.queueInputBuffer(index, 0, size, presentationTime, flags)
            }

        }

    }

    private fun isConnected(): Boolean {
        Log.d(TAG, "VideoTranslationHelper.isConnected")
        return clientSocketChannel.isConnected
    }

    fun close() {
        Log.d(TAG, "VideoTranslationHelper.close")
        clientSocketChannel.close()
        encoder.release()
    }

}