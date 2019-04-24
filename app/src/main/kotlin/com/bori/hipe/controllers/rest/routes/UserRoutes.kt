package com.bori.hipe.controllers.rest.routes

import com.bori.hipe.controllers.rest.routes.Route.EVENT_ID
import com.bori.hipe.controllers.rest.routes.Route.HEADER_AUTHORIZATION
import com.bori.hipe.controllers.rest.routes.Route.PASSWORD
import com.bori.hipe.controllers.rest.routes.Route.QUERY
import com.bori.hipe.controllers.rest.routes.Route.RELATION_TYPE
import com.bori.hipe.controllers.rest.routes.Route.USERNAME
import com.bori.hipe.controllers.rest.routes.Route.USER_ID
import com.bori.hipe.models.Image
import com.bori.hipe.models.User
import com.bori.hipe.util.Const
import retrofit2.Call
import retrofit2.http.*

interface UserRoutes {

    @GET("/user/check_existence/{$USERNAME}/")
    fun checkUserExistence(@Path(USERNAME) username: String,
                           @Header(HEADER_AUTHORIZATION) token: String): Call<Void>

    @GET("/user/login/")
    fun login(@Query(USERNAME) username: String,
              @Query(PASSWORD) password: String) : Call<String>

    @Headers("Content-Type: application/json",
            "Accept: */*")
    @PUT("user/update/")
    fun update(@Body user: User,
               @Header(HEADER_AUTHORIZATION) token: String): Call<Void>

    @GET("/user/find/")
    fun find(@Query(Const.QUERY) query: String,
             @Header(HEADER_AUTHORIZATION) token: String): Call<List<Pair<User, Image>>>

    @PUT("/user/create_users_relation/")
    fun createUsersRelation(@Query(USER_ID) userID: Int,
                            @Query(RELATION_TYPE) relationType: String,
                            @Header(HEADER_AUTHORIZATION) token: String): Call<Void>

    @DELETE("/user/remove_users_relation/")
    fun removeUsersRelation(@Query(USER_ID) userID: Int,
                            @Header(HEADER_AUTHORIZATION) token: String): Call<Void>

    @GET("/user/get_by_id/{$USER_ID}/")
    fun getByID(@Path(USER_ID) userID: Int,
                @Header(HEADER_AUTHORIZATION) token: String): Call<Pair<User, Image>>

    @GET("user/get_friends/{$USER_ID}/")
    fun getFriends(@Path(USER_ID) userID: Int,
                   @Header(HEADER_AUTHORIZATION) token: String): Call<List<Pair<User, Image>>>

    @GET("user/get_friends_ids/{$USER_ID}/")
    fun getFriendsIDs(@Path(USER_ID) userID: Int,
                      @Header(HEADER_AUTHORIZATION) token: String): Call<Collection<Int>>

    @GET("/user/get_by_event_id/{$EVENT_ID}/")
    fun getByEventID(@Path(EVENT_ID) eventID: Long,
                     @Header(HEADER_AUTHORIZATION) token: String): Call<List<Pair<User, Image>>>

    @GET("/user/get_ids_by_event_id/{$EVENT_ID}/")
    fun getIDsByEventID(@Path(EVENT_ID) eventID: Long,
                        @Header(HEADER_AUTHORIZATION) token: String): Call<Collection<Int>>

    @GET("/user/search/")
    fun search(@Query(QUERY) query: String,
               @Header(HEADER_AUTHORIZATION) token: String): Call<List<Pair<User, Image>>>

}