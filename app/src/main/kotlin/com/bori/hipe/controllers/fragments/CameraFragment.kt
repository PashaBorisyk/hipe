package com.bori.hipe.controllers.fragments

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.bori.hipe.R
import com.bori.hipe.controllers.camera.Camera2Helper
import com.bori.hipe.controllers.fragments.base.HipeBaseFragment
import com.bori.hipe.controllers.translation.VideoTranslationHelper
import com.bori.hipe.controllers.views.AutoFitTextureView
import com.bori.hipe.util.extensions.findViewById
import com.bori.hipe.util.extensions.setContentView
import io.reactivex.schedulers.Schedulers
import java.net.URL

class CameraFragment : HipeBaseFragment(), View.OnClickListener {

    companion object {
        private const val TAG = "CameraFragment.kt"
        private const val PERMISSION_ID = 12
    }

    private lateinit var autoFitTextureView: AutoFitTextureView
    private lateinit var cameraHelper: Camera2Helper
    private lateinit var videoTranslationHelper: VideoTranslationHelper

    private lateinit var startStreamView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "CameraFragment.onCreateView")
        setContentView(R.layout.fragment_video_stream_camera, inflater, container)

        init()

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "CameraFragment.onResume")

        permissionPublishSubject
                .firstElement()
                .toObservable()
                .filter { permission ->
                    permission.first == PERMISSION_ID &&
                            permission.third == PackageManager.PERMISSION_GRANTED
                }.map {
                    prepareCameraAndStartPreview()
                }.subscribe()

        requestPermissionsFast(PERMISSION_ID,
                Manifest.permission.CAMERA
        )


    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun init() {
        Log.d(TAG, "CameraFragment.init")
        autoFitTextureView = findViewById(R.id.camera_preview_texture)
        startStreamView = findViewById(R.id.start_stream_view_id)
        startStreamView.setOnClickListener(this)


    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun prepareCameraAndStartPreview() {
        Log.d(TAG, "CameraFragment.prepareCameraAndStartPreview")

        val url = URL(getString(R.string.video_stream_url_address))
        videoTranslationHelper = VideoTranslationHelper(url)
        cameraHelper = Camera2Helper()
        cameraHelper.prepareCameraAndStartPreview(this.context!!).flatMap { size ->
            Log.d(TAG, "CameraFragment.prepareCameraAndStartPreview preparing codec")
            return@flatMap videoTranslationHelper.prepare(size)

        }.flatMap { surfaceWithSize ->
            Log.d(TAG, "CameraFragment.prepareCameraAndStartPreview starting encoding")
            cameraHelper.prepareSurface(autoFitTextureView, arrayOf(surfaceWithSize.first), surfaceWithSize.second)
        }.subscribe()

        videoTranslationHelper.createSocketConnection()
                .subscribeOn(Schedulers.newThread())
                .subscribe({}, { error ->
                    Log.e(TAG, "Error accrued while creating translation : ${error.message}")
                })

        videoTranslationHelper.startEncoding().subscribeOn(Schedulers.newThread()).subscribe({}, { error ->
            Log.e(TAG, "Error accrued while creating translation : ${error.message}")
        })

    }

    override fun onClick(v: View) {
        Log.d(TAG, "CameraFragment.onClick")

        when (v.id) {

            R.id.start_stream_view_id -> {
                videoTranslationHelper.shouldRun = true
            }

        }

    }

}