package com.bori.hipe.controllers.media

import android.annotation.TargetApi
import android.content.Context
import android.media.MediaCodec
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.Surface
import java.io.File
import java.io.FileDescriptor

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class MediaRecorderHelper {

    private companion object {
        private const val TAG = "MediaRecorderTrans"
    }

    private val mediaRecorder = MediaRecorder()

    fun prepare(context: Context, fileDescriptor: FileDescriptor, size: Size): Surface {
        Log.d(TAG, "MediaRecorderHelper.prepare")

        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP)

        mediaRecorder.setOnErrorListener { _, what, extra ->
            Log.e(TAG, "Error : $what ; $extra")
        }

        val surface = MediaCodec.createPersistentInputSurface()

        val file = File(context.filesDir.absolutePath + "/some.mpg")
        file.createNewFile()
        Log.e(TAG, "New Video filepath : ${file.absolutePath}")

        mediaRecorder.setVideoSize(size.width, size.height)
        mediaRecorder.setOutputFile(file)
        mediaRecorder.setInputSurface(surface)

        return surface

    }

    fun start() {
        Log.d(TAG, "MediaRecorderHelper.start")

        mediaRecorder.prepare()
        mediaRecorder.start()
    }


}