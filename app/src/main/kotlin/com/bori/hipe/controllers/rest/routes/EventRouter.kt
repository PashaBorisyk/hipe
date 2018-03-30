package com.bori.hipe.controllers.rest.routes

import com.bori.hipe.models.Event
import com.bori.hipe.models.HipeImage
import com.bori.hipe.models.Tuple
import com.bori.hipe.util.Const
import retrofit2.Call
import retrofit2.http.*

interface EventRouter {

    @POST("/event/create")
    fun createEvent(@Body event: Event): Call<Long>

    @PUT("event/update")
    fun updateEvent(@Body event: Event): Call<Long>

    @GET("/event/get_user_events")
    fun getEvents(
            @Query(Const.USER_ID) userId: Long,
            @Query(Const.LATITUDE) latitude: Double,
            @Query(Const.LONGTITUDE) longtitude: Double,
            @Query(Const.LAST_READ_EVENT_ID) lastReadEventId: Long): Call<List<Tuple<Event, HipeImage>>>

    @DELETE("/event/cancel")
    fun cancelEvent(@Query(Const.USER_ID) userId: Long, @Query(Const.EVENT_ID) eventId: Long): Call<Long>

    @DELETE("/event/remove/member")
    fun removeMember(
            @Query(Const.USER_ID) userId: Long,
            @Query(Const.ADVANCED_USER_ID) advancedUserId: Long,
            @Query(Const.EVENT_ID) eventId: Long): Call<Long>

    @PUT("/event/add/member")
    fun addMemberToEvent(
            @Query(Const.EVENT_ID) eventId: Long,
            @Query(Const.USER_ID) userId: Long,
            @Query(Const.ADVANCED_USER_ID) advancedUserId: Long): Call<Long>

    @GET("/event/get/by/{${Const.EVENT_ID}}")
    fun getById(@Path(Const.EVENT_ID) eventId: Long): Call<Event>

    @GET("/event/get/by/owner/{${Const.EVENT_ID}}")
    fun getByOwner(@Path(Const.USER_ID) userId: Long): Call<List<Tuple<Event, HipeImage>>>

    @GET("/event/get_by_member_id/{${Const.USER_ID}}")
    fun getByMember(@Path(Const.USER_ID) userId: Long): Call<List<Tuple<Event, HipeImage>>>

}