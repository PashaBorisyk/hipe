package com.bori.hipe.controllers.camera

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.Surface
import com.bori.hipe.models.Trio
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import java.nio.ByteBuffer
import java.util.*


internal enum class DeviceStateEvents {
    ON_OPEND,
    ON_CLOSED,
    ON_DISCONNECTED
}

internal enum class CaptureSessionStateEvents {
    ON_CONFIGURED,
    ON_READY,
    ON_ACTIVE,
    ON_CLOSED,
    ON_SURFACE_PREPARED
}

internal enum class CaptureSessionEvents {
    ON_STARTED,
    ON_PROGRESSED,
    ON_COMPLETED,
    ON_SEQUENCE_COMPLETED,
    ON_SEQUENCE_ABORTED
}

internal data class CaptureSessionData(
        val event: CaptureSessionEvents,
        val session: CameraCaptureSession,
        val request: CaptureRequest,
        val result: CaptureResult
)

@TargetApi(21)
internal object CameraService {

    init {
        System.loadLibrary("native-lib")
    }

    internal val TAG = CameraService::class.java.simpleName
    private const val MAX_PREVIEW_WIDTH = 1920
    private const val MAX_PREVIEW_HEIGHT = 1920
    private const val MAX_STILL_IMAGE_WIDTH = 1920
    private const val MAX_STILL_IMAGE_HEIGHT = 1920

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun createCameraWithFacing(cameraManager: CameraManager, lensFacing: Int): Pair<String, CameraCharacteristics>? {
        Log.d(TAG, "CameraService.createCameraWithFacing")

        var possibleCandidate: String? = null
        var cameraCharactersitics: CameraCharacteristics? = null
        val cameraIds = cameraManager.cameraIdList

        if (cameraIds.isEmpty())
            return null

        for (cameraId in cameraIds) {

            cameraCharactersitics = cameraManager.getCameraCharacteristics(cameraId)

            val facing = cameraCharactersitics[CameraCharacteristics.LENS_FACING]
            if (facing != null && facing == lensFacing)
                return cameraId to cameraCharactersitics

            possibleCandidate = cameraId
        }
        if (possibleCandidate != null)
            return possibleCandidate to cameraCharactersitics!!


        return cameraIds[0] to cameraManager.getCameraCharacteristics(cameraIds[0])
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("MissingPermission")
    fun openCamera(
            cameraId: String, cameraManager: CameraManager,
            surfaces: Array<Surface>
    ) = Observable.create<Trio<DeviceStateEvents, CameraDevice, Array<Surface>>> { observable ->
        android.util.Log.d(TAG, "CameraService.openCamera")
        cameraManager.openCamera(cameraId,
                object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        Log.d(TAG, "CameraService.onOpened")
                        observable.onNext(Trio(DeviceStateEvents.ON_OPEND, camera, surfaces))
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        Log.d(TAG, "CameraService.onDisconnected")
                        observable.onNext(Trio(DeviceStateEvents.ON_CLOSED, camera, surfaces))
                        observable.onComplete()
                    }

                    override fun onError(camera: CameraDevice?, error: Int) {
                        Log.d(TAG, "CameraService.onError")
                        observable.onError(CameraAccessException(error, "Some error accrued"))
                    }

                    override fun onClosed(camera: CameraDevice) {
                        Log.d(TAG, "CameraService.onClosed")
                        observable.onNext(Trio(DeviceStateEvents.ON_CLOSED, camera, surfaces))
                        observable.onComplete()
                    }
                }, null)

    }


    internal fun createCaptureSession(
            cameraDevice: CameraDevice, surfaceList: List<Surface>
    ) = Observable.create<Trio<CaptureSessionStateEvents, CameraCaptureSession, List<Surface>>> {
        Log.d(TAG, "CameraService.createCaptureSession")
        cameraDevice.createCaptureSession(surfaceList,
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(session: CameraCaptureSession) {
                        Log.d(TAG, "CameraService.onConfigured")
                        it.onNext(Trio(CaptureSessionStateEvents.ON_CONFIGURED, session, surfaceList))
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.d(TAG, "CameraService.onConfigureFailed")
                        it.onError(ExceptionInInitializerError("Some error during configuration $session"))
                    }

                    override fun onReady(session: CameraCaptureSession) {
                        Log.d(TAG, "CameraService.onReady")
                        it.onNext(Trio(CaptureSessionStateEvents.ON_READY, session, surfaceList))
                    }

                    override fun onActive(session: CameraCaptureSession) {
                        Log.d(TAG, "CameraService.onActive")
                        it.onNext(Trio(CaptureSessionStateEvents.ON_ACTIVE, session, surfaceList))
                    }

                    override fun onClosed(session: CameraCaptureSession) {
                        Log.d(TAG, "CameraService.onClosed")
                        it.onNext(Trio(CaptureSessionStateEvents.ON_CLOSED, session, surfaceList))
                        it.onComplete()
                    }

                    override fun onSurfacePrepared(session: CameraCaptureSession, surface: Surface?) {
                        Log.d(TAG, "CameraService.onSurfacePrepared")
                        it.onNext(Trio(CaptureSessionStateEvents.ON_SURFACE_PREPARED, session, surfaceList))
                    }

                }, null)

    }

    private fun createCaptureCallback(
            observableEmitter: ObservableEmitter<CaptureSessionData>
    ): CameraCaptureSession.CaptureCallback {
        android.util.Log.d(TAG, "CameraService.createCaptureCallback")
        return object : CameraCaptureSession.CaptureCallback() {

            override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
            ) {

                if (!observableEmitter.isDisposed) {

                    observableEmitter.onNext(CaptureSessionData(
                            CaptureSessionEvents.ON_COMPLETED,
                            session, request, result
                    ))

                }
            }

            override fun onCaptureFailed(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    failure: CaptureFailure
            ) {
                android.util.Log.d(TAG, "CameraService.onCaptureFailed")

                if (!observableEmitter.isDisposed) {

                    observableEmitter.onError(Exception("Failed to capture camera content"))

                }

            }

        }
    }

    internal fun fromSetRepeatingRequest(
            captureSession: CameraCaptureSession, captureRequest: CaptureRequest
    ): Observable<CaptureSessionData> {
        android.util.Log.d(TAG, "CameraService.fromSetRepeatingRequest")
        return Observable.create { captureSession.setRepeatingRequest(captureRequest, createCaptureCallback(it), null) }
    }

    internal fun getPreviewSize(characteristics: CameraCharacteristics): Size {
        Log.d(TAG, "CameraService.getPreviewSize")
        val map = characteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]
        val outputSizes = map!!.getOutputSizes(SurfaceTexture::class.java)

        if (outputSizes.isEmpty()) {
            throw IllegalStateException("No supported sizes for SurfaceTexture")
        }
        val filteredOutputSizes = Observable.fromArray<Size>(*outputSizes)
                .filter { size -> size.width <= MAX_PREVIEW_WIDTH && size.height <= MAX_PREVIEW_HEIGHT }
                .toList()
                .blockingGet()

        return if (filteredOutputSizes.isEmpty()) {
            outputSizes[0]
        } else Collections.max(filteredOutputSizes, CompareSizesByArea())

    }

    /**
     * Please note that aspect ratios should be the same for [.getPreviewSize] and [.getStillImageSize]
     */
    internal fun getStillImageSize(characteristics: CameraCharacteristics, previewSize: Size): Size {
        Log.d(TAG, "CameraService.getStillImageSize")
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val outputSizes = map!!.getOutputSizes(ImageFormat.JPEG)
        if (outputSizes.isEmpty()) {
            throw IllegalStateException("No supported sizes for JPEG")
        }
        val filteredOutputSizes = Observable.fromArray<Size>(*outputSizes)
                .filter { size -> size.width == size.height * previewSize.width / previewSize.height }
                .filter { size -> size.width <= MAX_STILL_IMAGE_WIDTH && size.height <= MAX_STILL_IMAGE_HEIGHT }
                .toList()
                .blockingGet()

        return if (filteredOutputSizes.size == 0) {
            outputSizes[0]
        } else Collections.max(filteredOutputSizes, CompareSizesByArea())

    }

    internal class CompareSizesByArea : Comparator<Size> {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun compare(lhs: Size, rhs: Size): Int {
            Log.d(TAG, "CompareSizesByArea.compare")

            return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
        }

    }

    private external fun processJNI(width: Int, height: Int, buffer: ByteBuffer)

}
