package com.bori.hipe.controllers.rest.service

import android.util.Log
import com.bori.hipe.MainApplication
import com.bori.hipe.controllers.rest.callback.*
import com.bori.hipe.controllers.rest.callback.EventCallback
import com.bori.hipe.controllers.rest.callback.EventListCallback
import com.bori.hipe.controllers.rest.callback.LongCallback
import com.bori.hipe.controllers.rest.callback.LongCollectionCallback
import com.bori.hipe.controllers.rest.callback.VoidCallback
import com.bori.hipe.controllers.rest.routes.EventRoute
import com.bori.hipe.models.Event

object EventService {

    private const val TAG = "EventService"

    lateinit var eventRouter: EventRoute

    fun create(requestID: Int, event: Event,userIDs:IntArray) {
        Log.d(TAG, "create() called with: event = [$event]")
        eventRouter.create(event to userIDs, MainApplication.getToken()).enqueue(LongCallback(requestID))
    }

    fun update(requestID: Int, event: Event) {
        Log.d(TAG, "update() called with: event = [$event]")
        eventRouter.update(event,MainApplication.getToken()).enqueue(VoidCallback(requestID))
    }

    fun getByID(requestID: Int, eventID: Long) {
        Log.d(TAG, "getByID() called with eventID = [$eventID]")
        eventRouter.getByID(eventID,MainApplication.getToken()).enqueue(EventCallback(requestID))
    }

    fun getByOwnerID(requestID: Int, ownerID: Int) {
        Log.d(TAG, "getByOwnerID() called with ownerID = [$ownerID]")
        eventRouter.getByOwnerID(ownerID,MainApplication.getToken()).enqueue(EventListCallback(requestID))
    }

    fun getByMemberID(requestID: Int, userID: Int) {
        Log.d(TAG, "getEventByuserID() called with: userID = [$userID]")
        eventRouter.getByUserID(userID,MainApplication.getToken()).enqueue(EventListCallback(requestID))
    }

    fun getIDsByUserID(requestID: Int, userID: Int) {
        Log.d(TAG, "getIDsByUserID() called with userID = [$userID]")
        eventRouter.getIDsByUserID(userID,MainApplication.getToken()).enqueue(LongCollectionCallback(requestID))
    }

    fun get(requestID: Int, latitude: Double, longitude: Double, lastReadEventID: Long) {
        Log.d(TAG, "get() called with:latitude = [$latitude], longitude = [$longitude], lastReadEventID = [$lastReadEventID]")
        eventRouter.get(latitude, longitude, lastReadEventID,MainApplication.getToken()).enqueue(EventListCallback(requestID))
    }

    fun cancel(requestID: Int, eventID: Long) {
        Log.d(TAG, "cancel() called with: eventID = [$eventID]")
        eventRouter.cancel(eventID,MainApplication.getToken()).enqueue(VoidCallback(requestID))
    }

    fun removeUser(requestID: Int, eventID: Long, userID: Int) {
        Log.d(TAG, "removeUser() called with eventID = [$eventID], userID = [$userID]")
        eventRouter.removeUser(eventID, userID,MainApplication.getToken()).enqueue(VoidCallback(requestID))
    }

    fun addUser(requestID: Int, eventId: Long, userID: Int) {
        Log.d(TAG, "addUser() called with: eventId = [$eventId], username = [$userID]")
        eventRouter.addUser(eventId, userID,MainApplication.getToken()).enqueue(VoidCallback(requestID))
    }

}