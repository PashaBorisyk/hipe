package com.bori.hipe.controllers.rest.routes

import com.bori.hipe.controllers.rest.routes.Route.EVENT_ID
import com.bori.hipe.controllers.rest.routes.Route.HEADER_AUTHORIZATION
import com.bori.hipe.controllers.rest.routes.Route.IMAGE_ID
import com.bori.hipe.controllers.rest.routes.Route.USER_ID
import com.bori.hipe.models.Image
import com.bori.hipe.util.Const
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ImageRoutes{

    @Multipart
    @POST("/image/upload/")
    fun upload(@Query(Const.EVENT_ID) eventID: Long,
               @Part file: MultipartBody.Part,
               @Header(HEADER_AUTHORIZATION) token:String): Call<Image>

    @GET("/image/get_by_id/{$IMAGE_ID}/")
    fun get(@Path(IMAGE_ID) imageID: Long,
            @Header(HEADER_AUTHORIZATION) token:String): Call<Image>

    @GET("/image/get_by_user_id/{$USER_ID}/")
    fun getByUserID(@Path(USER_ID) userID: Int,
                    @Header(HEADER_AUTHORIZATION) token:String): Call<List<Image>>

    @GET("/image/get_by_event_id/{$EVENT_ID}/")
    fun getByEventID(@Path(EVENT_ID) eventID: Int,
                     @Header(HEADER_AUTHORIZATION) token:String): Call<List<Image>>

    @DELETE("/image/delete/{$IMAGE_ID}/")
    fun delete(@Path(IMAGE_ID) imageID : Long,
               @Header(HEADER_AUTHORIZATION) token:String) : Call<Void>
}