package com.bori.hipe.models

import java.io.Serializable

data class User(
        val id: Int = 0,
        val username: String = "",
        val token: String = "",
        val name: String = "",
        val surname: String = "",
        val sex: UserSex = UserSex.ANY,
        @JvmField
        val isOnline: Boolean = true,
        val status: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val imageID: Long = 1L,
        val email: String = "",
        val state: UserState = UserState.REGISTRATION
) : Serializable