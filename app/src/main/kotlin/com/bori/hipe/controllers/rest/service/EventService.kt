package com.bori.hipe.controllers.rest.service

import android.util.Log
import com.bori.hipe.HipeApplication
import com.bori.hipe.controllers.rest.EventListCallback
import com.bori.hipe.controllers.rest.LongCallback
import com.bori.hipe.controllers.rest.routes.EventRouter
import com.bori.hipe.models.Event

object EventService {

    private const val TAG = "EventService"

    lateinit var eventEvoke: EventRouter

    fun createNewEvent(requestID: Long, event: Event) {
        Log.d(TAG, "createNewEvent() called with: event = [$event]")
        eventEvoke.createEvent(event).enqueue(LongCallback(requestID))
    }

    fun updateEvent(requestID: Long, event: Event) {
        Log.d(TAG, "updateEvent() called with: event = [$event]")
        eventEvoke.updateEvent(event).enqueue(LongCallback(requestID))
    }

    fun cancelEvent(requestID: Long, eventId: Long, userId: Long = HipeApplication.THIS_USER_ID) {
        Log.d(TAG, "cancelEvent() called with: eventId = [$eventId]")
        eventEvoke.cancelEvent(userId, eventId).enqueue(LongCallback(requestID))
    }

    fun addMemberToEvent(requestID: Long, eventId: Long, advancedUserId: Long, userId: Long = HipeApplication.THIS_USER_ID) {
        Log.d(TAG, "addMemberToEvent() called with: eventId = [$eventId], username = [$userId], advancedUserId = [$advancedUserId]")
        eventEvoke.addMemberToEvent(eventId, userId, advancedUserId).enqueue(LongCallback(requestID))
    }

    fun getEvents(requestID: Long, latitude: Double, longtitude: Double, plastReadEventId: Long, userId: Long = HipeApplication.THIS_USER_ID) {
        Log.d(TAG, "getEvents() called with: userId = [$userId], latitude = [$latitude], longitude = [$longtitude], plastReadEventId = [$plastReadEventId]")
        eventEvoke.getEvents(userId, latitude, longtitude, plastReadEventId).enqueue(EventListCallback(requestID))
    }

    fun getByMemberId(requestID: Long, userId: Long = HipeApplication.THIS_USER_ID) {
        Log.d(TAG, "getEventByUserID() called with: userId = [$userId]")
        eventEvoke.getByMember(userId).enqueue(EventListCallback(requestID))
    }

}