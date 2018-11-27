package com.bori.hipe.controllers.media

import android.content.Context
import android.util.Log
import android.util.Size
import android.view.Surface
import com.bori.hipe.R
import com.bori.hipe.controllers.socket.VideoSocket
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.net.URL

class MediaTranslationHelper(val context: Context) {

    private val videoSocket: VideoSocket
    private val mediaRecorderHelper: MediaRecorderHelper

    private companion object {
        private const val TAG = "MediaTranslationHelper"
    }

    init {
        val address = context.getString(R.string.video_stream_url_address)
        val url = URL(address)
        val port = url.port
        val host = url.host
        videoSocket = VideoSocket(host, port)
        mediaRecorderHelper = MediaRecorderHelper()
    }

    fun prepareTranslation(size: Size, callback: (Surface) -> Unit) {
        Log.d(TAG, "MediaTranslationHelper.prepareTranslation")

        val s = videoSocket.connect()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .doOnError {
                    Log.e(TAG, it.message)
                }
                .subscribe { createdSocket ->
                    if (createdSocket.isConnected) {

                        Log.d(TAG, "MediaTranslationHelper.prepareTranslation is connected : ${videoSocket.isConnected()}")
                        val mediaRecorderSurface = mediaRecorderHelper.prepare(
                                context, createdSocket.fileDescriptor, size
                        )
                        callback(mediaRecorderSurface)
                    }
                }

    }

    fun startTranslation() {
        mediaRecorderHelper.start()
    }

}