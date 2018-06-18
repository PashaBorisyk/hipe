package com.bori.hipe.controllers.rest.service

import android.util.Log
import com.bori.hipe.HipeApplication
import com.bori.hipe.controllers.rest.*
import com.bori.hipe.controllers.rest.routes.UserRouter
import com.bori.hipe.models.User

object UserService {

    private const val TAG = "UserService.kt"

    lateinit var userRouter: UserRouter

    fun registerUser(requestID: Long,username: String, password: String) {
        Log.d(TAG, "registerUser() called with: username = [$username]")
        userRouter.registerUser(username,password).enqueue(StringCallback(requestID))
    }

    fun updateUser(requestID: Long, user: User) {
        Log.d(TAG, "updateUser() called with: user = [$user]")
        userRouter.updateUser(user).enqueue(LongCallback(requestID))
    }

    fun loginUser(requestID: Long, username: String, password: String) {
        Log.d(TAG, "loginUser() called with: username = [$username], password = [$password]")
        userRouter.loginUser(username, password).enqueue(StringCallback(requestID))
    }

    fun addUserToFriend(requestID: Long, userId: Long, advancedUserId: Long) {
        Log.d(TAG, "addUserToFriend() called with: userId = [$userId], advancedUserId = [$advancedUserId]")
        userRouter.addUserToFriends(userId, advancedUserId).enqueue(LongCallback(requestID))
    }

    fun removeUserFromFriend(requestID: Long, userId: Long, advancedUserId: Long) {
        Log.d(TAG, "addUserToFriend() called with: userSelfNick = [$userId], userToFriendsNick = [$advancedUserId]")
        userRouter.removeUserFromFriends(userId, advancedUserId).enqueue(LongCallback(requestID))
    }

    fun findUser(requestID: Long, userID: Long, query: String) {
        Log.d(TAG, "findUser() called with: username = [$query]")
        userRouter.findUser(userID, query).enqueue(UserListCallback(requestID))
    }

    fun checkUserExistence(requestID: Long, nickName: String) {
        Log.d(TAG, "checkUserExistence() called with: username = [$nickName]")
        userRouter.checkUserExistence(nickName).enqueue(BooleanCallback(requestID))
    }

    fun getFriendsList(requestID: Long, userId: Long = HipeApplication.THIS_USER_ID) {
        Log.d(TAG, "getFriendsList() called with: userId = [$userId]")
        userRouter.getFriends(userId).enqueue(UserListCallback(requestID))
    }

    fun getFriendsIdsList(requestID: Long, userId: Long = HipeApplication.THIS_USER_ID) {
        Log.d(TAG, "getFriendsIdsList() called with: userId = [$userId]")
        userRouter.getFriendsIds(userId).enqueue(LongListCallback(requestID))
    }

    fun getUserById(requestID: Long, userID: Long = HipeApplication.THIS_USER_ID) {
        Log.d(TAG, "getUserById  $userID")
        userRouter.getById(userID).enqueue(UserCallback(requestID))
    }

}