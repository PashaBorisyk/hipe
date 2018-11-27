package com.bori.hipe.controllers.camera

import android.view.Surface

abstract class CameraController{
    abstract fun startCameraPreview(vararg surfaces: Surface)
    abstract fun stopCameraPreview()
}