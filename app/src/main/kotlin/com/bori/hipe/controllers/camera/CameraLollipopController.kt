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
import java.io.File
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
        preapareCamera()

    }


    private fun preapareCamera() {
        Log.d(CameraFragment.TAG, "prepareCamera()")

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraWithCharacteristics = CameraStrategy.createCameraWithFacing(cameraManager, 1)
        cameraWithCharacteristics ?: throw Exception("Fucking camera doesnt want to initialize")
        previewSize = CameraStrategy.getPreviewSize(cameraWithCharacteristics!!.second)
        setTextureAspectRatio(previewSize)
        val cameraDeviceObservable = onSurfaceTextureAvailable
                .firstElement()
                .doAfterSuccess(this::setupSurface)
                .doAfterSuccess { initImageReader() }
                .toObservable()
                .flatMap { CameraStrategy.openCamera(cameraWithCharacteristics!!.first, cameraManager) }
                .share()

        val openCameraObservable = cameraDeviceObservable.filter { pair ->
            pair.first == DeviceStateEvents.ON_OPEND
        }.map { it.second }.share()

        val closeCameraObservable = cameraDeviceObservable
                .filter { pair -> pair.first == DeviceStateEvents.ON_CLOSED }
                .map { it.second }
                .share()

        val createCaptureSessionObservable = openCameraObservable
                .flatMap { cameraDevice ->
                    CameraStrategy.createCaptureSession(cameraDevice, Arrays.asList(imageReader.surface))
                }.share()

        val captureSessionConfigureObservable = createCaptureSessionObservable
                .filter { pair -> pair.first == CaptureSessionStateEvents.ON_CONFIGURED }
                .map { it.second }
                .share()

        val captureSessionClosedObservable = createCaptureSessionObservable
                .filter { pair -> pair.first == CaptureSessionStateEvents.ON_CLOSED }
                .map { it.second }

        val previewObservable = captureSessionConfigureObservable
                .flatMap { cameraCapturedSession ->
                    val previewBuilder = createPreviewBuilder(cameraCapturedSession, imageReader.surface)
                    return@flatMap CameraStrategy.fromSetRepeatingRequest(cameraCapturedSession, previewBuilder.build())
                }
                .share()

        previewObservable.subscribe()

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setupSurface(surfaceTexture: SurfaceTexture) {
        Log.d(CameraFragment.TAG, "setupSurface()")

        val displaySize = Point()
        activity.windowManager?.defaultDisplay?.getSize(displaySize)

        surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)
        surface = Surface(surfaceTexture)
    }

    fun createPreviewBuilder(captureSession: CameraCaptureSession, surface: Surface): CaptureRequest.Builder {
        val buidler = captureSession.device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        buidler.addTarget(surface)
        step3Auto(buidler)
        return buidler
    }

    fun step3Auto(builder: CaptureRequest.Builder) {

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
        Log.d(CameraFragment.TAG, "initImageReader()")

        val file = File(context.filesDir.toString() + "/photo.jpg")

        val sizeForImageReader = CameraStrategy.getStillImageSize(cameraWithCharacteristics!!.second, previewSize)

        imageReader = ImageReader.newInstance(
                sizeForImageReader.width, sizeForImageReader.height,
                ImageFormat.YUV_420_888, 5
        )

        compositeDisposable.add(
                CameraStrategy.createOnImageAvailableObservable(imageReader)
                        .observeOn(Schedulers.io())
                        .flatMap { imageReader ->
                            CameraStrategy.processWithNDK(imageReader.acquireLatestImage(),imageReader,surface).toObservable()
                        }.observeOn(AndroidSchedulers.mainThread())
                        .subscribe()
        )

    }

    private fun setTextureAspectRatio(previewSize: Size) {
        autoFitTextureView.setAspectRatio(previewSize.height, previewSize.width)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @TargetApi(Build.VERSION_CODES.KITKAT)
    override fun startPreview() {

        autoFitTextureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return true
            }

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                onSurfaceTextureAvailable.onNext(surface)
            }

        }
    }

    override fun stopPreview() {
        imageReader.close()
    }

}