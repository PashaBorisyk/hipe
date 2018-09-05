package com.bori.hipe.controllers.camera

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import android.util.Size
import android.view.Surface
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
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
internal object CameraStrategy {

    init {
        System.loadLibrary("native-lib")
    }

    internal val TAG = CameraStrategy::class.java.simpleName
    private val MAX_PREVIEW_WIDTH = 1920
    private val MAX_PREVIEW_HEIGHT = 1920
    private val MAX_STILL_IMAGE_WIDTH = 1920
    private val MAX_STILL_IMAGE_HEIGHT = 1920

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun createCameraWithFacing(cameraManager: CameraManager, lensFacing: Int): Pair<String, CameraCharacteristics>? {

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
            cameraId: String, cameraManager: CameraManager
    ): Observable<Pair<DeviceStateEvents, CameraDevice>> =
            Observable.create {
                cameraManager.openCamera(cameraId,
                        object : CameraDevice.StateCallback() {
                            override fun onOpened(camera: CameraDevice) {
                                it.onNext(DeviceStateEvents.ON_OPEND to camera)
                            }

                            override fun onDisconnected(camera: CameraDevice) {
                                it.onNext(DeviceStateEvents.ON_CLOSED to camera)
                                it.onComplete()
                            }

                            override fun onError(camera: CameraDevice?, error: Int) {
                                it.onError(CameraAccessException(error, "Some error accured"))
                            }

                            override fun onClosed(camera: CameraDevice) {
                                it.onNext(DeviceStateEvents.ON_CLOSED to camera)
                                it.onComplete()
                            }
                        }, null)
            }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    internal fun createCaptureSession(
            cameraDevice: CameraDevice, surfaceList: List<Surface>
    ): Observable<Pair<CaptureSessionStateEvents, CameraCaptureSession>> =
            Observable.create {
                cameraDevice.createCaptureSession(surfaceList, @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(session: CameraCaptureSession) {
                        it.onNext(CaptureSessionStateEvents.ON_CONFIGURED to session)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        it.onError(ExceptionInInitializerError("Some error during configuration $session"))
                    }

                    override fun onReady(session: CameraCaptureSession) {
                        it.onNext(CaptureSessionStateEvents.ON_READY to session)
                    }

                    override fun onActive(session: CameraCaptureSession) {
                        it.onNext(CaptureSessionStateEvents.ON_ACTIVE to session)
                    }

                    override fun onClosed(session: CameraCaptureSession) {
                        it.onNext(CaptureSessionStateEvents.ON_CLOSED to session)
                        it.onComplete()
                    }

                    override fun onSurfacePrepared(session: CameraCaptureSession, surface: Surface?) {
                        it.onNext(CaptureSessionStateEvents.ON_SURFACE_PREPARED to session)
                    }

                }, null)
            }

    internal fun createCaptureCallback(
            observableEmitter: ObservableEmitter<CaptureSessionData>
    ) = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureCompleted(
                session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult
        ) {

            if (!observableEmitter.isDisposed) {

                observableEmitter.onNext(CaptureSessionData(
                        CaptureSessionEvents.ON_COMPLETED,
                        session, request, result
                ))

            }
        }

        override fun onCaptureFailed(
                session: CameraCaptureSession, request: CaptureRequest, failure: CaptureFailure
        ) {

            if (!observableEmitter.isDisposed) {

                observableEmitter.onError(Exception("Failed to capture camera content"))

            }

        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    internal fun fromSetRepeatingRequest(
            captureSession: CameraCaptureSession, captureRequest: CaptureRequest
    ): Observable<CaptureSessionData> =
            Observable.create { captureSession.setRepeatingRequest(captureRequest, createCaptureCallback(it), null) }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    internal fun fromCapture(
            cameraCaptureSession: CameraCaptureSession, captureRequest: CaptureRequest
    ): Observable<CaptureSessionData> = Observable.create {
        cameraCaptureSession.capture(captureRequest, createCaptureCallback(it), null)
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    internal fun createOnImageAvailableObservable(imageReader: ImageReader): Observable<ImageReader> =
            Observable.create { subscriber ->
                imageReader.setOnImageAvailableListener({ reader ->
                    if (!subscriber.isDisposed) {
                        subscriber.onNext(reader)
                    }
                }, null)
                subscriber.setCancellable { imageReader.setOnImageAvailableListener(null, null) }
            }

    var firstTime = true

    var t1 = 0L

    @TargetApi(Build.VERSION_CODES.KITKAT)
    internal fun processWithNDK(image: Image?, imageReader: ImageReader, targetSurface: Surface): Single<Any> =
            Single.fromCallable {

                if(firstTime){
                     t1 = System.currentTimeMillis()
                    firstTime = false
                }
                image?:return@fromCallable "Image was null"

                val format = imageReader.imageFormat
                Log.d(TAG, "bob image format: $format")

                if (format != ImageFormat.YUV_420_888)
                    throw IllegalArgumentException("Image format must be YUV_420_888.")

                val planes = image.planes

                if(planes[1].pixelStride != 1 && planes[1].pixelStride != 2)
                    throw IllegalArgumentException("image chroma plane must have a pixel stride of 1 or 2 :got ${planes[1].pixelStride}")


                val result = processJNI(image.width,image.height,planes[0].buffer,targetSurface)
                image.close()
                val mills = System.currentTimeMillis() - t1
                Log.d(TAG,"Frames per second : ${1000/mills} " )
                t1 = System.currentTimeMillis()
                result
            }

    internal fun getPreviewSize(characteristics: CameraCharacteristics): Size {
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    /**
     * Please note that aspect ratios should be the same for [.getPreviewSize] and [.getStillImageSize]
     */
    internal fun getStillImageSize(characteristics: CameraCharacteristics, previewSize: Size): Size {
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
            return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
        }

    }

    private external fun processJNI(width:Int, height:Int, buffer:ByteBuffer, destinationSurface: Surface)
}