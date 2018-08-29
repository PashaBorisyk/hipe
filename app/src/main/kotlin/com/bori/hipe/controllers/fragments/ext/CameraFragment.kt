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
import com.bori.hipe.controllers.camera.CameraLollipopController
import com.bori.hipe.controllers.fragments.base.HipeBaseFragment
import com.bori.hipe.controllers.views.AutoFitSurfaceView
import com.bori.hipe.controllers.views.AutoFitTextureView
import com.bori.hipe.util.extensions.findViewById
import com.bori.hipe.util.extensions.setContentView
import net.majorkernelpanic.streaming.Session


class CameraFragment : HipeBaseFragment(), Session.Callback, SurfaceHolder.Callback {



    companion object {
        const val TAG = "CameraFragment.kt"
        const val CAMERA_PERMISSIONS_REQUEST = 12
    }

    private lateinit var autoFitTextureView: AutoFitTextureView
    private lateinit var autoFitSurfaceView: AutoFitSurfaceView
    private lateinit var cameraController: CameraLollipopController
    private lateinit var session: Session

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setContentView(R.layout.camera_fragment, inflater, container)
        init()
        requestPermissions()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cameraController.startPreview()
        }

    }

    private fun init() {
        autoFitTextureView = findViewById(R.id.camera_preview_texture)
//        session = SessionBuilder.getInstance()
//                .setCallback(this)
//                .setSurfaceView(autoFitSurfaceView)
//                .setContext(context!!)
//                .setPreviewOrientation(90)
//                .setAudioEncoder(SessionBuilder.VIDEO_NONE)
//                .setAudioQuality(AudioQuality(16000, 32000))
//                .setVideoEncoder(SessionBuilder.VIDEO_H264)
//                .setVideoQuality(VideoQuality(1920,1080,20,500000))
//                .build()

//        autoFitSurfaceView.holder.addCallback(this)


    }

    private fun createCameraSession() {
        if (activity is AppCompatActivity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cameraController = CameraLollipopController(activity as AppCompatActivity, autoFitTextureView)
            }
        } else
            throw Exception("Parent activity must by of type AppCompatActivity")
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(activity!!,
                        Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(CameraFragment.TAG, "Permission is not granted")
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            activity!!, Manifest.permission.CAMERA)) {
                Log.d(CameraFragment.TAG, "Explonation needed")
            } else {
                ActivityCompat.requestPermissions(
                        activity!!, arrayOf(Manifest.permission.CAMERA), CameraFragment.CAMERA_PERMISSIONS_REQUEST
                )

            }
        } else {
            createCameraSession()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
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

    override fun onBitrateUpdate(bitrate: Long) {
        Log.d(TAG,"onBitrateUpdate")
    }

    override fun onSessionError(reason: Int, streamType: Int, e: java.lang.Exception?) {
        Log.e(TAG,e?.message)
    }

    override fun onPreviewStarted() {
        Log.d(TAG,"onPreviewStarted")
    }

    override fun onSessionConfigured() {
        Log.d(TAG,"onSessionConfigured")
        session.start()
    }

    override fun onSessionStarted() {
        Log.d(TAG,"onSessionStarted")
    }

    override fun onSessionStopped() {
        Log.d(TAG,"onSessionStopped")
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun surfaceCreated(holder: SurfaceHolder?) {
        cameraController.startPreview()
//        session.startPreview()
    }

}