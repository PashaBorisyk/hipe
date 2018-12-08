package com.bori.hipe.models

import com.bori.hipe.HipeApplication
import java.io.Serializable


data class Event(

        val id: Long = 0L,
        val creatorId: Long = HipeApplication.THIS_USER_ID,
        val dateMills: Long = 0L,
        val creationDateMills: Long = 0L,
        val maxMembers: Long = 0L,
        val longitude: Double = 0.0,
        val latitude: Double = 0.0,
        val creatorNickname: String = "",
        val country: String = "",
        val city: String = "",
        val street: String = "",
        val localName: String = "",
        val description: String = "",
        val isPublic: Boolean = false,
        val isForOneGender: Boolean = false,
        val isForMale: Boolean = false,
        val eventImageId: Long = 0L,
        val creatorsImageUrl: String = ""

) : Serializable
