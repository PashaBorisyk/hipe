package com.bori.hipe.controllers.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import androidx.annotation.RequiresApi
import com.bori.hipe.controllers.views.AutoFitTextureView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2Helper : CameraController {

    companion object {
        const val TAG = "CameraController.kt"
    }

    private val onSurfaceTextureAvailable = PublishSubject.create<Array<Surface>>()


    override fun prepareCameraAndStartPreview(context: Context) = Observable.create<Size> { sizeObservable ->
        Log.d(TAG, "Camera2Helper.prepareCamera")

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraWithCharacteristics = Camera2Service.createCameraWithFacing(cameraManager, 1)
        cameraWithCharacteristics
                ?: throw Exception("Can not initialize camera with its characteristics")

        onSurfaceTextureAvailable
                .firstElement()
                .toObservable()
                .flatMap { surfaces ->
                    Camera2Service.openCamera(cameraWithCharacteristics.first, cameraManager, surfaces)
                }.filter { deviceCameraSurfaces ->
                    deviceCameraSurfaces.first == DeviceStateEvents.ON_OPEND
                }
                .map { deviceCameraSurfaces ->
                    deviceCameraSurfaces.second to deviceCameraSurfaces.third
                }
                .flatMap { cameraSurfaces ->
                    Camera2Service.createCaptureSession(cameraSurfaces.first, cameraSurfaces.second.asList())
                }
                .filter { eventSessionSurfaces ->
                    eventSessionSurfaces.first == CaptureSessionStateEvents.ON_CONFIGURED
                }
                .map { eventSessionSurfaces ->
                    eventSessionSurfaces.second to eventSessionSurfaces.third
                }
                .flatMap { sessionSurfaces ->
                    val previewBuilder = createPreviewBuilder(sessionSurfaces.first, cameraWithCharacteristics, sessionSurfaces.second)
                    return@flatMap Camera2Service.fromSetRepeatingRequest(sessionSurfaces.first, previewBuilder.build())
                }.subscribe()

        val previewSize = Camera2Service.getPreviewSize(cameraWithCharacteristics.second)
        sizeObservable.onNext(previewSize)

    }!!

    override fun prepareSurface(
            autoFitTextureView: AutoFitTextureView,
            surfaces: Array<Surface>,
            previewSize: Size
    ) = Observable.create<Unit> { observable ->
        autoFitTextureView.setAspectRatio(previewSize.height, previewSize.width)
        autoFitTextureView.surfaceTextureListener = MySurfaceTextureListener(previewSize, surfaces)
        observable.onNext(Unit)
    }

    private fun createPreviewBuilder(
            captureSession: CameraCaptureSession,
            cameraWithCharacteristics: Pair<String, CameraCharacteristics>,
            surfaces: List<Surface>
    ): CaptureRequest.Builder {
        Log.d(TAG, "Camera2Helper.createPreviewBuilder")

        val builder = captureSession.device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        builder.addTarget(surfaces[0])
        builder.addTarget(surfaces[1])
        configureFeatures(builder, cameraWithCharacteristics)
        return builder
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

    override fun stopCameraPreview(surfaces: List<Surface>) {
        Log.d(TAG, "Camera2Helper.stopCameraPreview")
        surfaces.forEach { it.release() }

    }

    internal inner class MySurfaceTextureListener(
            private val param: Any?,
            private val surfaces: Array<Surface>
    ) : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            Log.d(TAG, "Camera2Helper.onSurfaceTextureSizeChanged")

        }

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}

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