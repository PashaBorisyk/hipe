package com.bori.hipe.controllers.rest.service

import android.util.Log
import com.bori.hipe.HipeApplication
import com.bori.hipe.controllers.rest.LongCallback
import com.bori.hipe.controllers.rest.routes.HipeImageRouter
import com.bori.hipe.util.Const
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

object HipeImageService {

    private const val TAG = "HipeImageService"

    lateinit var hipeImageRouter: HipeImageRouter

    fun upload(requestID: Long, eventId: Long, file: File, userId: Long = HipeApplication.THIS_USER_ID) {
        Log.d(TAG, "upload() called with: file = [$file]")

        val reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val bPart = MultipartBody.Part.createFormData(Const.PART_FILE, file.name, reqFile)
        hipeImageRouter.upload(userId, eventId, bPart).enqueue(LongCallback(requestID))
    }
}