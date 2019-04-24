package com.bori.hipe.controllers.rest.service

import android.util.Log

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.bori.hipe.controllers.rest.callback.RestCallback
import com.bori.hipe.controllers.rest.callback.RestCallbackRepository
import com.bori.hipe.models.*
import com.bori.hipe.util.web.Status
import org.junit.*

import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventServiceTest {

    @get:Rule
    var mRuntimePermissionRule2: GrantPermissionRule = GrantPermissionRule
            .grant(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION)

    val restCallback = object : RestCallback() {

        override fun onFailure(requestID: Int, t: Throwable) {
            Log.e(TAG, "Failed to perform request: ", t)
            Assert.assertTrue(false)
            finished = true
        }

        override fun onSimpleResponse(requestID: Int, response: Any?, responseStatus: Int) {
            Log.d(TAG, "Request finished onSimpleResponse with response: $response; server status : $responseStatus")
            Assert.assertFalse(Status.isClientError(responseStatus) || Status.isServerError(responseStatus))
            finished = true
        }

        override fun onUserResponse(requestID: Int, users: List<Pair<User, Image>>, responseStatus: Int) {
            Log.d(TAG, "Request finished onUserResponse with response: $users; server status : $responseStatus")
            Assert.assertFalse(Status.isClientError(responseStatus) || Status.isServerError(responseStatus))
            finished = true
        }

        override fun onEventResponse(requestID: Int, events: List<Pair<Event, Image>>, responseStatus: Int) {
            Log.d(TAG, "Request finished onEventResponse with response: $events; server status : $responseStatus")
            Assert.assertFalse(Status.isClientError(responseStatus) || Status.isServerError(responseStatus))
            finished = true
        }

        override fun onIntCollectionResponse(requestID: Int, ids: Collection<Int>, responseStatus: Int) {
            Log.d(TAG, "Request finished onIntCollectionResponse with response: $ids; server status : $responseStatus")
            Assert.assertFalse(Status.isClientError(responseStatus) || Status.isServerError(responseStatus))
            finished = true
        }

        override fun onLongCollectionResponse(requestID: Int, ids: Collection<Long>, responseStatus: Int) {
            Log.d(TAG, "Request finished onLongCollectionResponse with response: $ids; server status : $responseStatus")
            Assert.assertFalse(Status.isClientError(responseStatus) || Status.isServerError(responseStatus))
            finished = true
        }

        override fun onImageResponse(requestID: Int, images: List<Image>, responseStatus: Int) {
            Log.d(TAG, "Request finished onImageResponse with response: $images; server status : $responseStatus")
            Assert.assertFalse(Status.isClientError(responseStatus) || Status.isServerError(responseStatus))
            finished = true
        }
    }

    init {
        RestCallbackRepository.clearCallbacks()
        RestCallbackRepository.registerCallback(restCallback)
    }

    companion object {
        const val TAG = "EVENT_TEST_TAG"
    }

    @Volatile
    var finished = false

    @Before
    fun before(){
        Log.d(TAG,"Rest callbacks size :${RestCallbackRepository.getCallbacksSize()}")
    }

    @Test
    fun create() {
        Log.d(TAG, "create")
        EventService.create(1, getEvent(), intArrayOf(1, 2, 3))
        waitForResponse()
    }

    @Test
    fun update() {
        Log.d(TAG, "update")
        EventService.update(1, getEvent())
        waitForResponse()
    }

    @Test
    fun getByID() {
        Log.d(TAG, "getByID")
        EventService.getByID(1, 1)
        waitForResponse()
    }

    @Test
    fun getByOwnerID() {
        Log.d(TAG, "getByOwnerID")
        EventService.getByOwnerID(1, 1)
        waitForResponse()
    }

    @Test
    fun getByMemberID() {
        Log.d(TAG, "getByMemberID")
        EventService.getByOwnerID(1, 1)
        waitForResponse()
    }

    @Test
    fun getIDsByUserID() {
        Log.d(TAG, "getIDsByUserID")
        EventService.getIDsByUserID(1, 1)
        waitForResponse()
    }

    @Test
    fun get() {
        Log.d(TAG, "get")
        EventService.get(1, longitude = 123.2,
                latitude = 43.52, lastReadEventID = 0)
    }

    @Test
    fun cancel() {
        Log.d(TAG, "cancel")
        EventService.cancel(1, 1)
    }

    @Test
    fun addUser() {
        Log.d(TAG, "addUser")
        EventService.addUser(1, 1, 5)
    }

    @Test
    fun removeUser() {
        Log.d(TAG, "removeUser")
        EventService.removeUser(1, 1, 5)
    }


    fun getEvent() = Event(
            ownerID = 1,
            city = "Minsk",
            country = "BY",
            openedFor = UserSex.FEMALE,
            maxMembers = 10,
            description = "Some description Here",
            street = "Raduznaja",
            privacy = EventPrivacyType.PUBLIC,
            longitude = 123.2,
            latitude = 43.52
    )

    fun waitForResponse() {
        while (!finished) {}
    }


}