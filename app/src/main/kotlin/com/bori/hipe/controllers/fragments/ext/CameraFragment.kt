package com.bori.hipe.controllers.fragments.ext

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
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
        requestPermissions()
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

        videoTranslationHelper.createSocketConnection()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe()

        cameraHelper.prepareCamera(this.context!!).flatMap { size ->
            Log.d(TAG, "CameraFragment.prepareCameraAndStartPreview preparing codec")
            return@flatMap videoTranslationHelper.prepareCodec(size)

        }.flatMap { surfaceWithSize ->
            Log.d(TAG, "CameraFragment.prepareCameraAndStartPreview starting encoding")
            cameraHelper.prepareSurface(autoFitTextureView, arrayOf(surfaceWithSize.first), surfaceWithSize.second)
        }.subscribe {
            videoTranslationHelper.startEncoding()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()
        }




    }

    private fun requestPermissions() {
        Log.d(TAG, "CameraFragment.requestPermissions")
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d(CameraFragment.TAG, "Camera permission is not granted")
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            activity!!, Manifest.permission.CAMERA)) {
                Log.d(CameraFragment.TAG, "Explonation needed")
            } else {
                ActivityCompat.requestPermissions(
                        activity!!, arrayOf(Manifest.permission.CAMERA), CameraFragment.CAMERA_PERMISSIONS_REQUEST
                )

            }
        }
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Microphone permission is not granted")
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            activity!!, Manifest.permission.RECORD_AUDIO)) {
                Log.d(CameraFragment.TAG, "Explonation for mic needed")
            } else {
                ActivityCompat.requestPermissions(activity!!,
                        arrayOf(Manifest.permission.RECORD_AUDIO), CameraFragment.AUDIO_PERMISSION_REQUEST)
            }
        }
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.INTERNET) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Microphone permission is not granted")
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            activity!!, Manifest.permission.INTERNET)) {
                Log.d(CameraFragment.TAG, "Explonation for mic needed")
            } else {
                ActivityCompat.requestPermissions(activity!!,
                        arrayOf(Manifest.permission.INTERNET), CameraFragment.INTERNET_PERMISSION_REQUEST)
            }
        }


    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        android.util.Log.d(TAG, "CameraFragment.onRequestPermissionsResult")
        when (requestCode) {
            CameraFragment.CAMERA_PERMISSIONS_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d(CameraFragment.TAG, "Permissions granted")
                } else {
                    Log.d(CameraFragment.TAG, "Permissions declined")
                }
                return
            }

            else -> {
                Log.d(CameraFragment.TAG, "Ignoring the rest of the requests")
            }
        }
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "CameraFragment.onClick")
        v ?: return

        when (v.id) {

            R.id.start_stream_view_id -> {

            }

        }

    }

}