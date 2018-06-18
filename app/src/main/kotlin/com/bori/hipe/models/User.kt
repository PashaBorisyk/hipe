package com.bori.hipe.models

import com.google.gson.annotations.Expose

data class User(
        @Expose
        val id: Long = 1L,
        @Expose
        val username: String = "pashaborisyk",
        val password: String = "Puschinarij1",
        @Expose
        val name: String = "pasha",
        @Expose
        val surname: String = "borisyk",
        val isMale: Boolean = true,
        val isOnline: Boolean = false,
        val status: String = "Lets dance",
        val latitude: Double = 10.0,
        val longitude: Double = 10.0,
        @Expose
        val imageId: Long = 1L
) : java.io.Serializable {
    companion object {
        val thisUser: User = User()
    }
}