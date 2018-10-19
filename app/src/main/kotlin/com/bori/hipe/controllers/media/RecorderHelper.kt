package com.bori.hipe.controllers.media

import android.media.MediaRecorder
import android.os.Build
import android.view.Surface
import java.io.FileDescriptor

class RecorderHelper{

    private val mediaRecorder = MediaRecorder()

    init {
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
    }

    fun prepare(fileDescriptor:FileDescriptor,surface:Surface){
        mediaRecorder.setOutputFile(fileDescriptor)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaRecorder.setInputSurface(surface)
        }
        mediaRecorder.prepare()
    }

    fun start(){
        mediaRecorder.start()
    }


}