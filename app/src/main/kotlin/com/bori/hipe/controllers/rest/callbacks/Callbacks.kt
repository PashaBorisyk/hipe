package com.bori.hipe.controllers.rest.callbacks

import com.bori.hipe.models.*

interface RestCallback {
    fun onFailure(requestID: Long, t: Throwable)
    fun onOk(requestID: Long)
    fun onEventNewsListResponse(requestID: Long, eventNewses: List<Tuple<EventNews, HipeImage>>?, serverStatus: Int)
    fun onUserListResponse(requestID: Long, users: List<Tuple<User, HipeImage>>?, serverStatus: Int)
    fun onEventListResponse(requestID: Long, events: List<Tuple<Event, HipeImage>>?, serverStatus: Int)
    fun onEventResponse(requestID: Long, event: Tuple<Event, HipeImage>?, serverStatus: Int)
    fun onUserResponse(requestID: Long, user: Tuple<User, HipeImage>?, serverStatus: Int)
    fun onSimpleResponse(requestID: Long, response: Any?, serverCode: Int)
    fun onLongListResponse(requestID: Long, ids: List<Long>?, serverStatus: Int)
    fun onHipeImageResponse(requestID: Long, hipeImages: List<HipeImage>?, serverStatus: Int)
}

abstract class RestCallbackAdapter : RestCallback {

    override fun onFailure(requestID: Long, t: Throwable) {}
    override fun onOk(requestID: Long) {}
    override fun onEventNewsListResponse(requestID: Long, eventNewses: List<Tuple<EventNews, HipeImage>>?, serverStatus: Int) {}
    override fun onUserListResponse(requestID: Long, users: List<Tuple<User, HipeImage>>?, serverStatus: Int) {}
    override fun onEventListResponse(requestID: Long, events: List<Tuple<Event, HipeImage>>?, serverStatus: Int) {}
    override fun onEventResponse(requestID: Long, event: Tuple<Event, HipeImage>?, serverStatus: Int) {}
    override fun onUserResponse(requestID: Long, user: Tuple<User, HipeImage>?, serverStatus: Int) {}
    override fun onSimpleResponse(requestID: Long, response: Any?, serverCode: Int) {}
    override fun onLongListResponse(requestID: Long, ids: List<Long>?, serverStatus: Int) {}
    override fun onHipeImageResponse(requestID: Long, hipeImages: List<HipeImage>?, serverStatus: Int) {}

}