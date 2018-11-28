package com.bori.hipe.controllers.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import com.bori.hipe.controllers.views.AutoFitTextureView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2Helper : CameraController() {

    companion object {
        const val TAG = "CameraController.kt"
    }

    private val onSurfaceTextureAvailable = PublishSubject.create<Array<Surface>>()


    fun prepareCamera(context: Context, autoFitTextureView: AutoFitTextureView) = Observable.create<Size> { sizeObservable ->
        Log.d(TAG, "Camera2Helper.prepareCamera")

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraWithCharacteristics = CameraService.createCameraWithFacing(cameraManager, 1)
        cameraWithCharacteristics ?: throw Exception("Fucking camera doesnt want to initialize")

        val previewSize = CameraService.getPreviewSize(cameraWithCharacteristics.second)
        setTextureAspectRatio(previewSize)

        onSurfaceTextureAvailable
                .firstElement()
                .toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap { surfaces ->
                    Log.d(TAG, "Camera2Helper.prepareCamera : opening camera")
                    CameraService.openCamera(cameraWithCharacteristics.first, cameraManager, surfaces)
                }.filter { trio ->
                    trio.first == DeviceStateEvents.ON_OPEND
                }
                .map { trio ->
                    trio.second to trio.third
                }
                .flatMap { pair ->
                    Log.d(TAG, "Camera2Helper.prepareCamera : creating capture session")
                    CameraService.createCaptureSession(pair.first, pair.second.asList())
                }
                .filter { trio ->
                    trio.first == CaptureSessionStateEvents.ON_CONFIGURED
                }
                .map { trio ->
                    trio.second to trio.third
                }
                .flatMap { pair ->
                    val previewBuilder = createPreviewBuilder(pair.first, cameraWithCharacteristics, pair.second)

                    Log.d(TAG, "Camera2Helper.prepareCamera : setting repeating request")

                    return@flatMap CameraService.fromSetRepeatingRequest(pair.first, previewBuilder.build())
                }.subscribe()

        autoFitTextureView.setAspectRatio(previewSize.height, previewSize.width)
        autoFitTextureView.surfaceTextureListener = MySurfaceTextureListener(previewSize, )

        sizeObservable.onNext(previewSize)
    }

    private fun createPreviewBuilder(
            captureSession: CameraCaptureSession,
            cameraWithCharacteristics: Pair<String, CameraCharacteristics>,
            surfaces: List<Surface>
    ): CaptureRequest.Builder {
        Log.d(TAG, "Camera2Helper.createPreviewBuilder")

        val buidler = captureSession.device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        buidler.addTarget(surfaces[0])
        configureFeatures(buidler, cameraWithCharacteristics)
        return buidler
    }

    private fun configureFeatures(
            builder: CaptureRequest.Builder,
            cameraWithCharacteristics: Pair<String, CameraCharacteristics>
    ) {
        Log.d(TAG, "Camera2Helper.configureFeatures")

        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)

        val minFocusDist = cameraWithCharacteristics.second[CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE]

        val noAFRun = minFocusDist == null || minFocusDist == 0f

        if (!noAFRun) {
            val afModes = cameraWithCharacteristics.second[CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES]

            if (afModes.contains(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE))
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            else
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
        }

        val aeModes = cameraWithCharacteristics.second[CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES]

        if (aeModes.contains(CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH))
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
        else
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)

        val awbModes = cameraWithCharacteristics.second[CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES]
        if (awbModes.contains(CaptureRequest.CONTROL_AWB_MODE_AUTO))
            builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)


    }

    private fun setTextureAspectRatio(previewSize: Size) {
        Log.d(TAG, "Camera2Helper.setTextureAspectRatio")


    }


    override fun startCameraPreview(param: Any?, vararg surfaces: Surface) {
        Log.d(TAG, "Camera2Helper.startCameraPreview")

    }

    override fun stopCameraPreview(surfaces: List<Surface>) {
        Log.d(TAG, "Camera2Helper.stopCameraPreview")
        surfaces.forEach { it.release() }

    }

    internal inner class MySurfaceTextureListener(val param: Any?, val surfaces: Array<Surface>) : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            Log.d(TAG, "Camera2Helper.onSurfaceTextureSizeChanged")

        }

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
            Log.d(TAG, "Camera2Helper.onSurfaceTextureUpdated")

        }

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
            Log.d(TAG, "Camera2Helper.onSurfaceTextureDestroyed")

            return true
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            Log.d(TAG, "Camera2Helper.onSurfaceTextureAvailable")

            if (param is Size) {
                surfaceTexture.setDefaultBufferSize(param.width, param.height)
            }
            val surfacesArray = arrayOf(Surface(surfaceTexture)) + arrayOf(*surfaces)

            onSurfaceTextureAvailable.onNext(surfacesArray)
        }

    }

}