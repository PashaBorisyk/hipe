package com.bori.hipe.controllers.rest.service

import android.util.Log
import com.bori.hipe.MyApplication
import com.bori.hipe.controllers.rest.*
import com.bori.hipe.controllers.rest.routes.UserRouter
import com.bori.hipe.models.User

object UserService {

    private const val TAG = "UserService.kt"

    lateinit var userRouter: UserRouter

    fun registerUser(requestID: Long, user: User) {
        Log.d(TAG, "registerUser() called with: user = [$user]")
        userRouter.registerUser(user).enqueue(LongCallback(requestID))
    }

    fun updateUser(requestID: Long, user: User) {
        Log.d(TAG, "updateUser() called with: user = [$user]")
        userRouter.updateUser(user).enqueue(LongCallback(requestID))
    }

    fun loginUser(requestID: Long, nickName: String, password: String) {
        Log.d(TAG, "loginUser() called with: nickName = [$nickName], password = [$password]")
        userRouter.loginUser(nickName, password).enqueue(VoidCallback(requestID))
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
        Log.d(TAG, "findUser() called with: nickName = [$query]")
        userRouter.findUser(userID, query).enqueue(UserListCallback(requestID))
    }

    fun checkUserExistence(requestID: Long, nickName: String) {
        Log.d(TAG, "checkUserExistence() called with: nickName = [$nickName]")
        userRouter.checkUserExistence(nickName).enqueue(BooleanCallback(requestID))
    }

    fun getFriendsList(requestID: Long, userId: Long = MyApplication.THIS_USER_ID) {
        Log.d(TAG, "getFriendsList() called with: userId = [$userId]")
        userRouter.getFriends(userId).enqueue(UserListCallback(requestID))
    }

    fun getFriendsIdsList(requestID: Long, userId: Long = MyApplication.THIS_USER_ID) {
        Log.d(TAG, "getFriendsIdsList() called with: userId = [$userId]")
        userRouter.getFriendsIds(userId).enqueue(LongListCallback(requestID))
    }

    fun getUserById(requestID: Long, userID: Long = MyApplication.THIS_USER_ID) {
        Log.d(TAG, "getUserById  $userID")
        userRouter.getById(userID).enqueue(UserCallback(requestID))
    }

}