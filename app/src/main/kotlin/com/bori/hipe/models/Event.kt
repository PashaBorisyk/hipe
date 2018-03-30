package com.bori.hipe.models

import com.bori.hipe.MyApplication
import java.io.Serializable


data class Event(

        val id: Long = 0L,
        val creatorId: Long = MyApplication.THIS_USER_ID,
        val dateMills: Long = 0L,
        val creationDateMills: Long = 0L,
        val maxMembers: Long = 0L,
        val longtitude: Double = 0.0,
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
