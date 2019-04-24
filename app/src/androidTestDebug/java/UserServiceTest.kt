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
class UserServiceTest {

    companion object {
        const val TAG = "USER_TEST_TAG"
        const val LOGIN_REQUEST_ID = 1
    }

    @get:Rule
    var mRuntimePermissionRule2: GrantPermissionRule = GrantPermissionRule
            .grant(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Volatile
    var finished = false

    private val restCallback = object : RestCallback() {

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


    @Before
    fun before() {
        Log.d(TAG, "Rest callbacks size :${RestCallbackRepository.getCallbacksSize()}")
    }

    @Test
    fun checkUserExistence() {
        UserService.checkUserExistence(1, "pashaborisyk")
        waitForResponse()
    }

    @Test
    fun login() {
        UserService.login(LOGIN_REQUEST_ID, "pashaborisyk", "password")
        waitForResponse()
    }

    @Test
    fun update() {
        UserService.update(1, getUser())
        waitForResponse()
    }

    @Test
    fun find() {
        UserService.find(1, "niki")
        waitForResponse()
    }

    @Test
    fun createUsersRelation() {
        UserService.createUsersRelation(1, 6, UsersRelationType.FOLLOW)
        waitForResponse()
    }

    @Test
    fun removeUsersRelation() {
        UserService.removeUsersRelation(1, 7)
        waitForResponse()
    }

    @Test
    fun getById() {
        UserService.getById(1, 2)
        waitForResponse()
    }

    @Test
    fun getFriends() {
        UserService.getFriends(1, 3)
        waitForResponse()
    }

    @Test
    fun getFriendsIDs() {
        UserService.getFriendsIDs(1, 2)
        waitForResponse()
    }

    @Test
    fun getByEventID() {
        UserService.getByEventID(1, 43)
        waitForResponse()
    }

    @Test
    fun getIDsByEventID() {
        UserService.getIDsByEventID(1, 32)
        waitForResponse()
    }

    @Test
    fun searchUser() {
        UserService.getIDsByEventID(1, 32)
        waitForResponse()
    }

    fun getUser() = User(
            username = "pashaborisyk",
            name = "Nikita",
            surname = "Rybakov",
            sex = UserSex.FEMALE,
            status = "Trying to change my status",
            latitude = 12.32,
            longitude = 32.4,
            email = "pasha@gmail.com",
            token = "token",
            id = 1,
            imageID = 2,
            isOnline = true,
            state = UserState.ACTIVE
    )

    fun waitForResponse() {
        while (!finished) {
        }
    }

}