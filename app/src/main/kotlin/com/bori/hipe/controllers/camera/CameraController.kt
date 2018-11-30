package com.bori.hipe.controllers.camera

import android.content.Context
import android.util.Size
import android.view.Surface
import com.bori.hipe.controllers.views.AutoFitTextureView
import io.reactivex.Observable


interface CameraController {

    fun prepareCameraAndStartPreview(context: Context): Observable<*>

    fun stopCameraPreview(surfaces: List<Surface>)

    fun prepareSurface(autoFitTextureView: AutoFitTextureView, surfaces: Array<Surface>,
                       previewSize: Size): Observable<*>
}