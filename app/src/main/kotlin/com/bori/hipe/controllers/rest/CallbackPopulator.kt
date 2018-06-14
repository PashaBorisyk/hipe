package com.bori.hipe.controllers.rest

import android.util.Log
import com.bori.hipe.controllers.rest.callbacks.RestCallbackAdapter
import com.bori.hipe.models.*
import retrofit2.Call
import retrofit2.Callback

const val SINGLE_CODE = 0x0001
const val LIST_CODE = 0x0002
internal val restCallbacks = hashSetOf<RestCallbackAdapter>()

object RestService {
    fun registerCallback(r: RestCallbackAdapter) = restCallbacks.add(r)
    fun unregisterCallback(r: RestCallbackAdapter) = restCallbacks.remove(r)
}

internal class LongListCallback(val requestID: Long) : Callback<List<Long>> {

    companion object {
        private const val TAG = "LongListCallback"
    }

    override fun onResponse(call: Call<List<Long>>, response: retrofit2.Response<List<Long>>) {
        Log.d(TAG, "onResponse: " + response.code())
        restCallbacks.forEach { it.onOk(requestID) }
        restCallbacks.forEach { it.onLongListResponse(requestID, response.body(), response.code()) }
    }

    override fun onFailure(call: Call<List<Long>>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}

internal class VoidCallback(val requestID: Long) : Callback<Void> {

    companion object {
        private const val TAG = "VoidCallback"
    }

    override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
        Log.d(TAG, "onResponse; url: ${call.request().url()}; code: ${response.code()}")
        restCallbacks.forEach { it.onOk(requestID) }
        restCallbacks.forEach { it.onSimpleResponse(requestID, response.body(), response.code()) }
    }

    override fun onFailure(call: Call<Void>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}

internal class UserListCallback(val requestID: Long) : Callback<List<Tuple<User, HipeImage>>> {

    companion object {
        private const val TAG = "UserListCallback"
    }

    override fun onResponse(call: Call<List<Tuple<User, HipeImage>>>, response: retrofit2.Response<List<Tuple<User, HipeImage>>>) {
        Log.d(TAG, "onResponse: " + response.code())
        restCallbacks.forEach { it.onOk(requestID) }
        restCallbacks.forEach { it.onUserListResponse(requestID, response.body(), response.code()) }
    }

    override fun onFailure(call: Call<List<Tuple<User, HipeImage>>>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}

internal class EventListCallback(val requestID: Long) : Callback<List<Tuple<Event, HipeImage>>> {

    companion object {
        private const val TAG = "EventListCallback"
    }

    override fun onResponse(call: Call<List<Tuple<Event, HipeImage>>>, response: retrofit2.Response<List<Tuple<Event, HipeImage>>>) {
        Log.d(TAG, "onResponse:  " + response.code())
        restCallbacks.forEach { it.onOk(requestID) }
        restCallbacks.forEach { it.onEventListResponse(requestID, response.body(), response.code()) }
    }

    override fun onFailure(call: Call<List<Tuple<Event, HipeImage>>>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }
}

internal class EventNewsListCallback(val requestID: Long) : Callback<List<Tuple<EventNews, HipeImage>>> {

    companion object {
        private const val TAG = "EventNewsListCallback"
    }

    override fun onResponse(call: Call<List<Tuple<EventNews, HipeImage>>>, response: retrofit2.Response<List<Tuple<EventNews, HipeImage>>>) {
        Log.d(TAG, "onResponse: " + response.code())
        restCallbacks.forEach { it.onOk(requestID) }
        restCallbacks.forEach { it.onEventNewsListResponse(requestID, response.body(), response.code()) }
    }

    override fun onFailure(call: Call<List<Tuple<EventNews, HipeImage>>>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}

internal class BooleanCallback(val requestID: Long) : Callback<Boolean> {

    companion object {
        private const val TAG = "BooleanCallback"
    }

    override fun onResponse(call: Call<Boolean>, response: retrofit2.Response<Boolean>) {
        Log.d(TAG,call.request().url().toString())
        Log.d(TAG, "onResponse: " + response.code())
        restCallbacks.forEach { it.onOk(requestID) }
        restCallbacks.forEach { it.onSimpleResponse(requestID, response.body(), response.code()) }
    }

    override fun onFailure(call: Call<Boolean>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}

internal class UserCallback(val requestID: Long) : Callback<Tuple<User, HipeImage>> {

    companion object {
        private const val TAG = "BooleanCallback"
    }

    override fun onResponse(call: Call<Tuple<User, HipeImage>>, response: retrofit2.Response<Tuple<User, HipeImage>>) {
        Log.d(TAG, "onResponse: " + response.code())
        restCallbacks.forEach { it.onOk(requestID) }
        restCallbacks.forEach { it.onUserResponse(requestID, response.body(), response.code()) }
    }

    override fun onFailure(call: Call<Tuple<User, HipeImage>>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}

internal class EventCallback(val requestID: Long) : Callback<Tuple<Event, HipeImage>> {

    companion object {
        private const val TAG = "BooleanCallback"
    }

    override fun onResponse(call: Call<Tuple<Event, HipeImage>>, response: retrofit2.Response<Tuple<Event, HipeImage>>) {
        Log.d(TAG, "onResponse:  " + response.code())
        restCallbacks.forEach { it.onOk(requestID) }
        restCallbacks.forEach { it.onEventResponse(requestID, response.body(), response.code()) }
    }

    override fun onFailure(call: Call<Tuple<Event, HipeImage>>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}

internal class LongCallback(val requestID: Long) : Callback<Long> {

    companion object {
        private const val TAG = "BooleanCallback"
    }

    override fun onResponse(call: Call<Long>, response: retrofit2.Response<Long>) {
        Log.d(TAG, "onResponse: " + response.code())
        restCallbacks.forEach { it.onOk(requestID) }
        restCallbacks.forEach { it.onSimpleResponse(requestID, response.body(), response.code()) }
    }

    override fun onFailure(call: Call<Long>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}