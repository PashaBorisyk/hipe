package com.bori.hipe.controllers.camera

import android.view.Surface


abstract class CameraController{
    abstract fun startCameraPreview(param: Any?, vararg surfaces: Surface)
    abstract fun stopCameraPreview(surfaces: List<Surface>)
}