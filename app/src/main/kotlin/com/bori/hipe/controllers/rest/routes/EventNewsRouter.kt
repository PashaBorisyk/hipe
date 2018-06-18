package com.bori.hipe.controllers.rest.routes

import com.bori.hipe.models.EventNews
import com.bori.hipe.models.HipeImage
import com.bori.hipe.models.Tuple
import com.bori.hipe.util.Const
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface EventNewsRouter {

    @GET("/event-news/get/")
    fun get(@Query(Const.USER_ID) userId: Long, @Query(Const.LAST_READ_EVENT_ID) lastReadId: Long): Call<List<Tuple<EventNews, HipeImage>>>

}