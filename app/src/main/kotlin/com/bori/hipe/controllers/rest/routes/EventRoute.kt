package com.bori.hipe.controllers.rest.routes

import com.bori.hipe.controllers.rest.routes.Route.EVENT_ID
import com.bori.hipe.controllers.rest.routes.Route.HEADER_AUTHORIZATION
import com.bori.hipe.controllers.rest.routes.Route.LAST_READ_EVENT_ID
import com.bori.hipe.controllers.rest.routes.Route.LATITUDE
import com.bori.hipe.controllers.rest.routes.Route.LONGITUDE
import com.bori.hipe.controllers.rest.routes.Route.USER_ID
import com.bori.hipe.models.Event
import com.bori.hipe.models.Image
import retrofit2.Call
import retrofit2.http.*

interface EventRoute{

    @POST("/event/create/")
    fun create(@Body eventWithUsers: Pair<Event,IntArray>,
               @Header(HEADER_AUTHORIZATION) token: String): Call<Long>

    @PUT("event/update/")
    fun update(@Body event: Event,
               @Header(HEADER_AUTHORIZATION) token: String): Call<Void>

    @PUT("/event/cancel/")
    fun cancel(@Query(EVENT_ID) eventID: Long,
               @Header(HEADER_AUTHORIZATION) token: String): Call<Void>

    @PUT("/event/remove_member/")
    fun removeUser(
            @Query(EVENT_ID) eventID: Long,
            @Query(USER_ID) userID: Int,
            @Header(HEADER_AUTHORIZATION) token: String): Call<Void>

    @PUT("/event/add_member/")
    fun addUser(
            @Query(EVENT_ID) eventID: Long,
            @Query(USER_ID) userID: Int,
            @Header(HEADER_AUTHORIZATION) token: String): Call<Void>

    @GET("/event/get_by_id/{$EVENT_ID}/")
    fun getByID(@Path(EVENT_ID) eventId: Long,
                @Header(HEADER_AUTHORIZATION) token: String): Call<Pair<Event, Image>>

    @GET("/event/get_by_owner_id/{$USER_ID}/")
    fun getByOwnerID(@Path(USER_ID) userID: Int,
                     @Header(HEADER_AUTHORIZATION) token: String): Call<List<Pair<Event, Image>>>

    @GET("/event/get/")
    fun get(
            @Query(LATITUDE) latitude: Double,
            @Query(LONGITUDE) longitude: Double,
            @Query(LAST_READ_EVENT_ID) lastReadEventId: Long,
            @Header(HEADER_AUTHORIZATION) token: String): Call<List<Pair<Event, Image>>>

    @GET("/event/get_by_member_id/{$USER_ID}/")
    fun getByUserID(@Path(USER_ID) userID: Int,
                    @Header(HEADER_AUTHORIZATION) token: String): Call<List<Pair<Event, Image>>>

    @GET("/event/get_ids_by_member_id/{$USER_ID}/")
    fun getIDsByUserID(@Path(USER_ID) userID: Int,
                       @Header(HEADER_AUTHORIZATION) token: String): Call<Collection<Long>>

}