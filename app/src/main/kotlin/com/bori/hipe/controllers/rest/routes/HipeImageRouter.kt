package com.bori.hipe.controllers.rest.routes

import com.bori.hipe.models.HipeImage
import com.bori.hipe.util.Const
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface HipeImageRouter {

    @Multipart
    @POST("/hipe-image/upload/")
    fun upload(@Query(Const.USER_ID) userId: Long, @Query(Const.EVENT_ID) eventId: Long, @Part file: MultipartBody.Part): Call<Long>

    @GET("/hipe-image/get/{${Const.ID}}/")
    fun get(@Path(Const.ID) id: Long): Call<HipeImage>

    @GET("/hipe-image/get/by/user/id/{${Const.ID}}/")
    fun getByUserId(@Path(Const.ID) userId: Long): Call<List<HipeImage>>

    @DELETE("/hipe-image/get/by/event/{${Const.ID}}/")
    fun getByEventId(@Query(Const.ID) eventId: Long): Call<List<HipeImage>>
}