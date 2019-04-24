package com.bori.hipe.controllers.rest.callback

import android.util.Log
import com.bori.hipe.controllers.rest.callback.RestCallbackRepository.restCallbacks
import com.bori.hipe.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class RestCallback {

    open fun onFailure(requestID: Int, t: Throwable) {}
    open fun onUserResponse(requestID: Int, users: List<Pair<User, Image>>, responseStatus: Int) {}
    open fun onEventResponse(requestID: Int, events: List<Pair<Event, Image>>, responseStatus: Int) {}
    open fun onSimpleResponse(requestID: Int, response: Any?, responseStatus: Int) {}
    open fun onIntCollectionResponse(requestID: Int, ids: Collection<Int>, responseStatus: Int) {}
    open fun onLongCollectionResponse(requestID: Int, ids: Collection<Long>, responseStatus: Int) {}
    open fun onImageResponse(requestID: Int, images: List<Image>, responseStatus: Int) {}

}

object RestCallbackRepository {
    internal val restCallbacks = hashSetOf<RestCallback>()
    fun clearCallbacks() = restCallbacks.clear()
    fun getCallbacksSize() = restCallbacks.size
    fun registerCallback(r: RestCallback) = restCallbacks.add(r)
    fun unregisterCallback(r: RestCallback) = restCallbacks.remove(r)
}

internal class IntCollectionCallback(val requestID: Int) : Callback<Collection<Int>> {

    companion object {
        private const val TAG = "IntCollectionCallback"
    }

    override fun onResponse(call: Call<Collection<Int>>, response: Response<Collection<Int>>) {
        Log.d(TAG, "onResponse: " + response.code())
        restCallbacks.forEach { it.onIntCollectionResponse(requestID, response.body()!!, response.code()) }
    }

    override fun onFailure(call: Call<Collection<Int>>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}


internal class LongCollectionCallback(val requestID: Int) : Callback<Collection<Long>> {

    companion object {
        private const val TAG = "IntCollectionCallback"
    }

    override fun onResponse(call: Call<Collection<Long>>, response: Response<Collection<Long>>) {
        Log.d(TAG, "onResponse: " + response.code())
        restCallbacks.forEach { it.onLongCollectionResponse(requestID, response.body()!!, response.code()) }
    }

    override fun onFailure(call: Call<Collection<Long>>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}

internal class VoidCallback(val requestID: Int) : Callback<Void> {

    companion object {
        private const val TAG = "VoidCallback"
    }

    override fun onResponse(call: Call<Void>, response: Response<Void>) {
        Log.d(TAG, "onResponse: $response; code: ${response.code()}")
        restCallbacks.forEach { it.onSimpleResponse(requestID, response.body(), response.code()) }
    }

    override fun onFailure(call: Call<Void>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}

internal class UserListCallback(val requestID: Int) : Callback<List<Pair<User, Image>>> {

    companion object {
        private const val TAG = "UserListCallback"
    }

    override fun onResponse(call: Call<List<Pair<User, Image>>>, response: Response<List<Pair<User, Image>>>) {
        Log.d(TAG, "onResponse: $response; code: ${response.code()}")
        restCallbacks.forEach { it.onUserResponse(requestID, response.body().orEmpty(), response.code()) }
    }

    override fun onFailure(call: Call<List<Pair<User, Image>>>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}

internal class EventListCallback(val requestID: Int) : Callback<List<Pair<Event, Image>>> {

    companion object {
        private const val TAG = "EventListCallback"
    }

    override fun onResponse(call: Call<List<Pair<Event, Image>>>, response: Response<List<Pair<Event, Image>>>) {
        Log.d(TAG, "onResponse: $response; code: ${response.code()}")
        restCallbacks.forEach { it.onEventResponse(requestID, response.body().orEmpty(), response.code()) }
    }

    override fun onFailure(call: Call<List<Pair<Event, Image>>>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }
}

internal class BooleanCallback(val requestID: Int) : Callback<Boolean> {

    companion object {
        private const val TAG = "BooleanCallback"
    }

    override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
        Log.d(TAG, "onResponse: $response; code: ${response.code()}")
        restCallbacks.forEach { it.onSimpleResponse(requestID, response.body(), response.code()) }
    }

    override fun onFailure(call: Call<Boolean>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}

internal class UserCallback(val requestID: Int) : Callback<Pair<User, Image>> {

    companion object {
        private const val TAG = "UserCallback"
    }

    override fun onResponse(call: Call<Pair<User, Image>>, response: Response<Pair<User, Image>>) {
        Log.d(TAG, "onResponse: $response; code: ${response.code()}")
        restCallbacks.forEach { it.onUserResponse(requestID, listOfNotNull(response.body()), response.code()) }
    }

    override fun onFailure(call: Call<Pair<User, Image>>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}

internal class EventCallback(val requestID: Int) : Callback<Pair<Event, Image>> {

    companion object {
        private const val TAG = "EventCallback"
    }

    override fun onResponse(call: Call<Pair<Event, Image>>, response: Response<Pair<Event, Image>>) {
        Log.d(TAG, "onResponse: $response; code: ${response.code()}")
        restCallbacks.forEach { it.onEventResponse(requestID, listOfNotNull(response.body()), response.code()) }
    }

    override fun onFailure(call: Call<Pair<Event, Image>>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}

internal class LongCallback(val requestID: Int) : Callback<Long> {

    companion object {
        private const val TAG = "LongCallback"
    }

    override fun onResponse(call: Call<Long>, response: Response<Long>) {
        Log.d(TAG, "onResponse: $response; code: ${response.code()}")
        restCallbacks.forEach { it.onSimpleResponse(requestID, response.body(), response.code()) }
    }

    override fun onFailure(call: Call<Long>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}

internal class StringCallback(val requestID: Int) : Callback<String> {

    companion object {
        private const val TAG = "StringCallback"
    }

    override fun onResponse(call: Call<String>, response: Response<String>) {
        Log.d(TAG, "onResponse: $response; code: ${response.code()}")
        restCallbacks.forEach { it.onSimpleResponse(requestID, response.body(), response.code()) }
    }

    override fun onFailure(call: Call<String>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

}

internal class ImageCallback(val requestID: Int) : Callback<Image> {

    companion object {
        const val TAG = "ImageCallback"
    }

    override fun onFailure(call: Call<Image>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

    override fun onResponse(call: Call<Image>, response: Response<Image>) {
        Log.d(TAG, "onResponse: $response; code: ${response.code()}")
        restCallbacks.forEach { it.onImageResponse(requestID, listOfNotNull(response.body()), response.code()) }
    }
}

internal class ImageListCallback(val requestID: Int) : Callback<List<Image>> {

    companion object {
        const val TAG = "ImageCallback"
    }

    override fun onFailure(call: Call<List<Image>>, t: Throwable) =
            restCallbacks.forEach { it.onFailure(requestID, t) }

    override fun onResponse(call: Call<List<Image>>, response: Response<List<Image>>) {
        Log.d(TAG, "onResponse: $response; code: ${response.code()}")
        restCallbacks.forEach { it.onImageResponse(requestID, response.body().orEmpty(), response.code()) }
    }
}
