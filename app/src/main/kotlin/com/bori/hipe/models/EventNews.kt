package com.bori.hipe.models

import java.io.Serializable

data class EventNews(

        val id: Long = 0L,
        val eventId: Long = 0L,
        val creationMills: Long = 0L,
        val whoWrote: Long = 0L,
        val newEventMember: Long = 0L,
        val removedEventMember: Long = 0L,
        val description: String = "",
        val isAcceptedByAdmin: Boolean = false,
        val isReadByAdmin: Boolean = false,
        val memberRemoved: Boolean = false,
        val doneByAdmin: Boolean = false,
        val eventHoleDeleted: Boolean = false

) : Serializable
