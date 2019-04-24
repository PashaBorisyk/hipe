package com.bori.hipe.controllers.rest.service

import android.util.Log
import com.bori.hipe.MainApplication
import com.bori.hipe.controllers.rest.callback.BooleanCallback
import com.bori.hipe.controllers.rest.callback.IntCollectionCallback
import com.bori.hipe.controllers.rest.callback.StringCallback
import com.bori.hipe.controllers.rest.callback.UserCallback
import com.bori.hipe.controllers.rest.callback.UserListCallback
import com.bori.hipe.controllers.rest.callback.VoidCallback
import com.bori.hipe.controllers.rest.routes.UserRoutes
import com.bori.hipe.models.User
import com.bori.hipe.models.UsersRelationType

object UserService {

    private const val TAG = "UserService.kt"

    lateinit var userRouter: UserRoutes

    fun checkUserExistence(requestID: Int, nickName: String) {
        Log.d(TAG, "checkUserExistence() called with: username = [$nickName]")
        userRouter.checkUserExistence(nickName,MainApplication.getToken()).enqueue(VoidCallback(requestID))
    }

    fun login(requestID: Int, username: String, password: String) {
        Log.d(TAG, "login() called with: username = [$username], password = [$password]")
        userRouter.login(username, password).enqueue(StringCallback(requestID))
    }

    fun update(requestID: Int, user: User) {
        Log.d(TAG, "update() called with: user = [$user]")
        userRouter.update(user,MainApplication.getToken()).enqueue(VoidCallback(requestID))
    }

    fun find(requestID: Int, query: String) {
        Log.d(TAG, "find() called with: username = [$query]")
        userRouter.find(query,MainApplication.getToken()).enqueue(UserListCallback(requestID))
    }

    fun createUsersRelation(requestID: Int, userID: Int, relationType:UsersRelationType){
        Log.d(TAG,"createUsersRelation() called with userID = [$userID], relationType = [$relationType]")
        userRouter.createUsersRelation(userID,relationType.name,MainApplication.getToken()).enqueue(VoidCallback(requestID))
    }

    fun removeUsersRelation(requestID: Int, userID: Int){
        Log.d(TAG,"removeUsersRelation() called with userID = [$userID]")
        userRouter.removeUsersRelation(userID,MainApplication.getToken()).enqueue(VoidCallback(requestID))
    }

    fun getById(requestID: Int, userID: Int) {
        Log.d(TAG, "getById  $userID")
        userRouter.getByID(userID,MainApplication.getToken()).enqueue(UserCallback(requestID))
    }

    fun getFriends(requestID: Int, userID: Int) {
        Log.d(TAG, "getFriends() called with: userID = [$userID]")
        userRouter.getFriends(userID,MainApplication.getToken()).enqueue(UserListCallback(requestID))
    }

    fun getFriendsIDs(requestID: Int, userID: Int) {
        Log.d(TAG, "getFriendsIDs() called with: userID = [$userID]")
        userRouter.getFriendsIDs(userID,MainApplication.getToken()).enqueue(IntCollectionCallback(requestID))
    }

    fun getByEventID(requestID: Int,eventID: Long) {
        Log.d(TAG,"getByEventID() called with eventID = [$eventID]")
        userRouter.getByEventID(eventID,MainApplication.getToken()).enqueue(UserListCallback(requestID))
    }

    fun getIDsByEventID(requestID: Int,eventID: Long) {
        Log.d(TAG,"getIDsByEventID() called with eventID = [$eventID]")
        userRouter.getIDsByEventID(eventID,MainApplication.getToken()).enqueue(IntCollectionCallback(requestID))
    }

    fun searchUser(requestID: Int, query: String) {
        Log.d(TAG,"searchUser() called with query = [$query]")
        userRouter.search(query,MainApplication.getToken()).enqueue(UserListCallback(requestID))
    }

}