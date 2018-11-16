package com.bori.hipe.controllers.camera

import android.annotation.TargetApi
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import com.bori.hipe.controllers.fragments.ext.CameraFragment
import com.bori.hipe.controllers.views.AutoFitTextureView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CameraLollipopController(
        private val activity: AppCompatActivity,
        private val autoFitTextureView: AutoFitTextureView) : CameraController() {

    companion object {
        const val TAG = "CameraController.kt"
    }


    private var cameraWithCharacteristics: Pair<String, CameraCharacteristics>? = null
    private val onSurfaceTextureAvailable = PublishSubject.create<SurfaceTexture>()
    private val compositeDisposable = CompositeDisposable()
    private lateinit var surface: Surface
    private lateinit var imageReader: ImageReader
    private lateinit var previewSize: Size
    private val context: Context

    init {
        Log.d(TAG, "init()")
        context = activity
        prepareCamera()

    }

    private fun prepareCamera() {
        Log.d(TAG, "CameraLollipopController.prepareCamera")

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraWithCharacteristics = CameraStrategy.createCameraWithFacing(cameraManager, 1)
        cameraWithCharacteristics ?: throw Exception("Fucking camera doesnt want to initialize")
        previewSize = CameraStrategy.getPreviewSize(cameraWithCharacteristics!!.second)
        setTextureAspectRatio(previewSize)
        onSurfaceTextureAvailable
                .firstElement()
                .doAfterSuccess(this::setupSurface)
                .doAfterSuccess { initImageReader() }
                .toObservable()
                .flatMap {
                    CameraStrategy.openCamera(cameraWithCharacteristics!!.first, cameraManager)
                }
                .filter { pair ->
                    pair.first == DeviceStateEvents.ON_OPEND
                }
                .map {
                    it.second
                }
                .flatMap {
                    CameraStrategy.createCaptureSession(it, Arrays.asList(surface, imageReader.surface))
                }
                .filter {
                    it.first == CaptureSessionStateEvents.ON_CONFIGURED
                }
                .map {
                    it.second
                }
                .flatMap { cameraCapturedSession ->
                    val previewBuilder = createPreviewBuilder(cameraCapturedSession, surface)
                    return@flatMap CameraStrategy.fromSetRepeatingRequest(cameraCapturedSession, previewBuilder.build())
                }.subscribe()

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setupSurface(surfaceTexture: SurfaceTexture) {
        Log.d(CameraFragment.TAG, "setupSurface()")

        val displaySize = Point()
        activity.windowManager?.defaultDisplay?.getSize(displaySize)

        surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)
        surface = Surface(surfaceTexture)
    }

    private fun createPreviewBuilder(captureSession: CameraCaptureSession, surface: Surface): CaptureRequest.Builder {
        Log.d(TAG, "CameraLollipopController.createPreviewBuilder")

        val buidler = captureSession.device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        buidler.addTarget(surface)
        step3Auto(buidler)
        return buidler
    }

    private fun step3Auto(builder: CaptureRequest.Builder) {
        Log.d(TAG, "CameraLollipopController.step3Auto")

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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun initImageReader() {
        Log.d(TAG, "CameraLollipopController.initImageReader")

        val sizeForImageReader = CameraStrategy.getStillImageSize(cameraWithCharacteristics!!.second, previewSize)

        imageReader = ImageReader.newInstance(
                sizeForImageReader.width, sizeForImageReader.height,
                ImageFormat.JPEG, 1
        )

        compositeDisposable.add(
                CameraStrategy.createOnImageAvailableObservable(imageReader)
                        .observeOn(Schedulers.io())
                        .flatMap { imageReader ->
                            CameraStrategy.processWithNDK(imageReader.acquireLatestImage(), imageReader).toObservable()
                        }.observeOn(AndroidSchedulers.mainThread())
                        .subscribe()
        )

    }

    private fun prepareRecorder() {
        Log.d(TAG, "CameraLollipopController.prepareRecorder")

    }

    private fun setTextureAspectRatio(previewSize: Size) {
        Log.d(TAG, "CameraLollipopController.setTextureAspectRatio")

        autoFitTextureView.setAspectRatio(previewSize.height, previewSize.width)
    }


    override fun startPreview() {
        Log.d(TAG, "CameraLollipopController.startPreview")

        autoFitTextureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                Log.d(TAG, "CameraLollipopController.onSurfaceTextureSizeChanged")

            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                Log.d(TAG, "CameraLollipopController.onSurfaceTextureDestroyed")

                return true
            }

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                Log.d(TAG, "CameraLollipopController.onSurfaceTextureAvailable")

                onSurfaceTextureAvailable.onNext(surface)
            }

        }
    }

    override fun stopPreview() {
        Log.d(TAG, "CameraLollipopController.stopPreview")

        imageReader.close()
    }

}