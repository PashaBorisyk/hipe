package com.bori.hipe.controllers.rest.service

import android.util.Log
import com.bori.hipe.HipeApplication
import com.bori.hipe.controllers.rest.EventNewsListCallback
import com.bori.hipe.controllers.rest.routes.EventNewsRouter

/**
 * Created by pasha on 25.01.2018.
 */
object EventNewsService {

    private const val TAG = "EventNewsService"

    lateinit var eventNewsEnvoker: EventNewsRouter

    fun getEventNews(requestID: Long, lastReadId: Long, userId: Long = HipeApplication.THIS_USER_ID) {
        Log.d(TAG, "getEventNews() called with: userId = [$userId)")
        eventNewsEnvoker.get(userId, lastReadId).enqueue(EventNewsListCallback(requestID))
    }

}