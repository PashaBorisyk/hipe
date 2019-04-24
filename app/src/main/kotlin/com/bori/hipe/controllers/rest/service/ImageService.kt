package com.bori.hipe.controllers.rest.service

import android.util.Log
import com.bori.hipe.MainApplication
import com.bori.hipe.controllers.rest.callback.ImageCallback
import com.bori.hipe.controllers.rest.callback.ImageListCallback
import com.bori.hipe.controllers.rest.routes.ImageRoutes
import com.bori.hipe.util.Const
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

object ImageService {

    private const val TAG = "ImageService"

    lateinit var imageRoutes: ImageRoutes

    fun upload(requestID: Int, eventId: Long, file: File) {
        Log.d(TAG, "upload() called with: file = [$file]")

        val reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val bPart = MultipartBody.Part.createFormData(Const.PART_FILE, file.name, reqFile)
        imageRoutes.upload(eventId, bPart,MainApplication.getToken()).enqueue(ImageCallback(requestID))
    }

    fun get(requestID:Int,imageID: Long, token:String){
        Log.d(TAG,"get called with: imageID = [$imageID], token = [$token]")
        imageRoutes.get(imageID,MainApplication.getToken()).enqueue(ImageCallback(requestID))
    }

    fun getByUserID(requestID: Int,userID: Int){
        Log.d(TAG,"getByUserID called with: userID = [$userID]")
        imageRoutes.getByUserID(userID,MainApplication.getToken()).enqueue(ImageListCallback(requestID))
    }

    fun getByEventID(requestID:Int,eventID: Int){
        Log.d(TAG,"getByEventID called with eventID = [$eventID]")
        imageRoutes.getByEventID(eventID,MainApplication.getToken()).enqueue(ImageListCallback(requestID))
    }

    fun delete(imageID : Long, token:String) {}

}