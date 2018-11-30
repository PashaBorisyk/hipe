package com.bori.hipe.controllers.fragments

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bori.hipe.R
import com.bori.hipe.controllers.camera.Camera2Helper
import com.bori.hipe.controllers.fragments.base.HipeBaseFragment
import com.bori.hipe.controllers.socket.VideoTranslationHelper
import com.bori.hipe.controllers.views.AutoFitTextureView
import com.bori.hipe.util.extensions.findViewById
import com.bori.hipe.util.extensions.setContentView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.net.URL

class CameraFragment : HipeBaseFragment(), View.OnClickListener {

    companion object {
        private const val TAG = "CameraFragment.kt"
        private const val CAMERA_PERMISSIONS_REQUEST = 12
        private const val AUDIO_PERMISSION_REQUEST = 13
        private const val INTERNET_PERMISSION_REQUEST = 14
    }

    private lateinit var autoFitTextureView: AutoFitTextureView
    private lateinit var cameraHelper: Camera2Helper
    private lateinit var videoTranslationHelper: VideoTranslationHelper

    private lateinit var startStreamView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "CameraFragment.onCreateView")
        setContentView(R.layout.camera_fragment, inflater, container)

        init()

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "CameraFragment.onResume")
        prepareCameraAndStartPreview()

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun init() {
        Log.d(TAG, "CameraFragment.init")
        autoFitTextureView = findViewById(R.id.camera_preview_texture)
        startStreamView = findViewById(R.id.start_stream_view_id)
        startStreamView.setOnClickListener(this)

        val url = URL(getString(R.string.video_stream_url_address))
        videoTranslationHelper = VideoTranslationHelper(url)
        cameraHelper = Camera2Helper()

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun prepareCameraAndStartPreview() {
        Log.d(TAG, "CameraFragment.prepareCameraAndStartPreview")

        cameraHelper.prepareCameraAndStartPreview(this.context!!).flatMap { size ->
            Log.d(TAG, "CameraFragment.prepareCameraAndStartPreview preparing codec")
            return@flatMap videoTranslationHelper.prepareCodec(size)

        }.flatMap { surfaceWithSize ->
            Log.d(TAG, "CameraFragment.prepareCameraAndStartPreview starting encoding")
            cameraHelper.prepareSurface(autoFitTextureView, arrayOf(surfaceWithSize.first), surfaceWithSize.second)
        }.subscribe()

        videoTranslationHelper.createSocketConnection()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe()

        videoTranslationHelper.startEncoding()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()

    }


    override fun onClick(v: View?) {
        Log.d(TAG, "CameraFragment.onClick")
        v ?: return

        when (v.id) {

            R.id.start_stream_view_id -> {
                videoTranslationHelper.shouldRun = true
            }

        }

    }

}