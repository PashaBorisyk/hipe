package com.bori.hipe.controllers.rest.routes

import com.bori.hipe.models.HipeImage
import com.bori.hipe.models.Tuple
import com.bori.hipe.models.User
import com.bori.hipe.util.Const
import retrofit2.Call
import retrofit2.http.*

interface UserRouter {

    @GET("/user/check_existence/{${Const.USERNAME}}/")
    fun checkUserExistence(@Path(Const.USERNAME) username: String): Call<Boolean>

    @POST("/user/register/")
    fun registerUser(@Query(Const.USERNAME) username: String, @Query(Const.PASSWORD) password: String): Call<String>

    @GET("/user/login/")
    fun loginUser(@Query(Const.USERNAME) username: String, @Query(Const.PASSWORD) password: String): Call<String>

    @GET("/user/get_by_id/{${Const.USER_ID}}/")
    fun getById(@Path(Const.USER_ID) userId: Long): Call<Tuple<User, HipeImage>>

    @GET("user/get_friends/{${Const.USER_ID}}/")
    fun getFriends(@Path(Const.USER_ID) userId: Long): Call<List<Tuple<User, HipeImage>>>

    @GET("user/get_friends_ids/{${Const.USER_ID}}/")
    fun getFriendsIds(@Path(Const.USER_ID) userId: Long): Call<List<Long>>

    @GET("/user/get_by_event_id/{${Const.EVENT_ID}}/")
    fun getByEventId(@Path(Const.EVENT_ID) eventId: Long): Call<List<Tuple<User, HipeImage>>>

    @GET("/user/find/{${Const.USER_ID}}/")
    fun findUser(@Path(Const.USER_ID) userId: Long, @Query(Const.QUERY) query: String): Call<List<Tuple<User, HipeImage>>>

    @PUT("/user/update/")
    fun updateUser(@Body user: User): Call<Long>

    @POST("/user/add_user_to_friends/")
    fun addUserToFriends(@Query(Const.USER_ID) userId: Long, @Query(Const.ADVANCED_USER_ID) advancedUserID: Long): Call<Long>

    @DELETE("/user/remove_user_from_friends/")
    fun removeUserFromFriends(@Query(Const.USER_ID) userId: Long, @Query(Const.ADVANCED_USER_ID) advancedUserID: Long): Call<Long>

}