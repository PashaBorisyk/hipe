package com.bori.hipe.controllers.fragments.ext

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import com.bori.hipe.R
import com.bori.hipe.controllers.camera.Camera2Helper
import com.bori.hipe.controllers.fragments.base.HipeBaseFragment
import com.bori.hipe.controllers.media.MediaTranslationHelper
import com.bori.hipe.controllers.views.AutoFitTextureView
import com.bori.hipe.util.extensions.findViewById
import com.bori.hipe.util.extensions.setContentView

class CameraFragment : HipeBaseFragment(), SurfaceHolder.Callback, View.OnClickListener {

    companion object {
        private const val TAG = "CameraFragment.kt"
        private const val CAMERA_PERMISSIONS_REQUEST = 12
        private const val AUDIO_PERMISSION_REQUEST = 13
        private const val INTERNET_PERMISSION_REQUEST = 14
    }

    private lateinit var autoFitTextureView: AutoFitTextureView
    private lateinit var cameraHelper: Camera2Helper
    private lateinit var startStreamView: View
    private lateinit var mediaTranslationHelper: MediaTranslationHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "CameraFragment.onCreateView")
        setContentView(R.layout.camera_fragment, inflater, container)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "CameraFragment.onResume")

        init()
        requestPermissions()

        mediaTranslationHelper.prepareTranslation(cameraHelper.previewSize) { surface ->
            Log.d(TAG, "CameraFragment.onResume surface achieved ")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cameraHelper.startCameraPreview(surface)
            }
        }

    }

    private fun init() {
        Log.d(TAG, "CameraFragment.init")
        autoFitTextureView = findViewById(R.id.camera_preview_texture)
        startStreamView = findViewById(R.id.start_stream_view_id)
        startStreamView.setOnClickListener(this)

        mediaTranslationHelper = MediaTranslationHelper(this.context!!)
    }

    private fun createCameraSession() {
        Log.d(TAG, "CameraFragment.createCameraSession")
        if (activity is AppCompatActivity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cameraHelper = Camera2Helper(
                        activity as AppCompatActivity,
                        autoFitTextureView,
                        mediaTranslationHelper
                )
            }
        } else
            throw Exception("Parent activity must by of type AppCompatActivity")

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


        createCameraSession()

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
                    createCameraSession()
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

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        Log.d(TAG, "CameraFragment.surfaceChanged")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Log.d(TAG, "CameraFragment.surfaceDestroyed")
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun surfaceCreated(holder: SurfaceHolder?) {
        Log.d(TAG, "CameraFragment.surfaceCreated")
        cameraHelper.startCameraPreview()
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "CameraFragment.onClick")
        v ?: return

        when (v.id) {

            R.id.start_stream_view_id -> {
                mediaTranslationHelper.startTranslation()
            }

        }

    }

}