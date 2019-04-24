package com.bori.hipe.models

import java.io.Serializable

data class Event(

        val id: Long = 0L,
        val ownerID: Int = 0,
        val dateMills: Long = System.currentTimeMillis(),
        val creationDateMills: Long = System.currentTimeMillis(),
        val maxMembers: Long = 0L,
        val longitude: Double = 0.0,
        val latitude: Double = 0.0,
        val ownerUsername: String = "",
        val country: String = "",
        val city: String = "",
        val street: String = "",
        val localName: String = "",
        val description: String = "",
        val openedFor: UserSex = UserSex.ANY,
        val privacy: EventPrivacyType = EventPrivacyType.PUBLIC,
        val imageID: Long = 0L,
        val creatorsImageUrl: String = "",
        val state: EventState = EventState.BEFORE

) : Serializable
