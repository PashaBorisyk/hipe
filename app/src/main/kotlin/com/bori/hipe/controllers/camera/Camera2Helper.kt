package com.bori.hipe.controllers.camera

import android.content.Context
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import com.bori.hipe.controllers.media.MediaTranslationHelper
import com.bori.hipe.controllers.views.AutoFitTextureView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2Helper(
        private val activity: AppCompatActivity,
        private val autoFitTextureView: AutoFitTextureView,
        private val mediaTranslationHelper: MediaTranslationHelper) : CameraController() {

    companion object {
        const val TAG = "CameraController.kt"
    }

    private var cameraWithCharacteristics: Pair<String, CameraCharacteristics>? = null
    private val onSurfaceTextureAvailable = PublishSubject.create<Unit>()
    private val compositeDisposable = CompositeDisposable()
    private lateinit var surfacesArray: Array<Surface>
    private val context: Context

    lateinit var previewSize: Size
        private set


    init {
        Log.d(TAG, "init()")
        context = activity
        prepareCamera()

    }

    private fun prepareCamera() {
        Log.d(TAG, "Camera2Helper.prepareCamera")

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraWithCharacteristics = CameraService.createCameraWithFacing(cameraManager, 1)
        cameraWithCharacteristics ?: throw Exception("Fucking camera doesnt want to initialize")

        previewSize = CameraService.getPreviewSize(cameraWithCharacteristics!!.second)
        setTextureAspectRatio(previewSize)

        onSurfaceTextureAvailable
                .firstElement()
                .toObservable()
                .flatMap {
                    CameraService.openCamera(cameraWithCharacteristics!!.first, cameraManager)
                }.filter { pair ->
                    pair.first == DeviceStateEvents.ON_OPEND
                }
                .map {
                    it.second
                }
                .flatMap {

                    CameraService.createCaptureSession(it, surfacesArray.asList())
                }
                .filter {
                    it.first == CaptureSessionStateEvents.ON_CONFIGURED
                }
                .map {
                    it.second
                }
                .flatMap { cameraCapturedSession ->
                    val previewBuilder = createPreviewBuilder(cameraCapturedSession)
                    return@flatMap CameraService.fromSetRepeatingRequest(cameraCapturedSession, previewBuilder.build())
                }.subscribe()

    }

    private fun createPreviewBuilder(captureSession: CameraCaptureSession): CaptureRequest.Builder {
        Log.d(TAG, "Camera2Helper.createPreviewBuilder")

        val buidler = captureSession.device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        buidler.addTarget(surfacesArray[0])
        step3Auto(buidler)
        return buidler
    }

    private fun step3Auto(builder: CaptureRequest.Builder) {
        Log.d(TAG, "Camera2Helper.step3Auto")

        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)

        val minFocusDist = cameraWithCharacteristics!!.second[CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE]

        val noAFRun = minFocusDist == null || minFocusDist == 0f

        if (!noAFRun) {
            val afModes = cameraWithCharacteristics!!.second[CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES]

            if (afModes.contains(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE))
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            else
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
        }

        val aeModes = cameraWithCharacteristics!!.second[CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES]

        if (aeModes.contains(CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH))
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
        else
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)

        val awbModes = cameraWithCharacteristics!!.second[CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES]
        if (awbModes.contains(CaptureRequest.CONTROL_AWB_MODE_AUTO))
            builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)


    }

    private fun prepareRecorder() {
        Log.d(TAG, "Camera2Helper.prepareRecorder")

    }

    private fun setTextureAspectRatio(previewSize: Size) {
        Log.d(TAG, "Camera2Helper.setTextureAspectRatio")

        autoFitTextureView.setAspectRatio(previewSize.height, previewSize.width)
    }


    override fun startCameraPreview(vararg surfaces: Surface) {
        Log.d(TAG, "Camera2Helper.startCameraPreview")

        autoFitTextureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                Log.d(TAG, "Camera2Helper.onSurfaceTextureSizeChanged")

            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {

            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                Log.d(TAG, "Camera2Helper.onSurfaceTextureDestroyed")

                return true
            }

            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                Log.d(TAG, "Camera2Helper.onSurfaceTextureAvailable")

                val displaySize = Point()
                activity.windowManager?.defaultDisplay?.getSize(displaySize)

                surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)

                surfacesArray = arrayOf(Surface(surfaceTexture)) + arrayOf(*surfaces)


                onSurfaceTextureAvailable.onNext(Unit)
            }

        }
    }

    override fun stopCameraPreview() {
        Log.d(TAG, "Camera2Helper.stopCameraPreview")
        surfacesArray.forEach { it.release() }

    }

}