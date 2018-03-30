package com.bori.hipe.models

import com.google.gson.annotations.Expose
import com.orm.dsl.Ignore
import com.orm.dsl.Table
import java.util.*

@Table
data class ChatMessageNOSQL(

        val id: Long = 0L,
        val eventID: Long = 0L,
        @Ignore
        val users: LongArray = longArrayOf(0),
        val mills: Long = System.currentTimeMillis(),
        val message: String = "message example",
        val informative: Boolean = false,
        @Expose
        val unsent: Boolean = false,
        val execute: Long = 1,
        val senderId: Long = 0L,
        val senderNickname: String = "",
        val senderSmallImageUrl: String = ""

) : java.io.Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatMessageNOSQL

        if (id != other.id) return false
        if (senderId != other.senderId) return false
        if (eventID != other.eventID) return false
        if (!Arrays.equals(users, other.users)) return false
        if (senderNickname != other.senderNickname) return false
        if (mills != other.mills) return false
        if (message != other.message) return false
        if (informative != other.informative) return false
        if (execute != other.execute) return false
        if (unsent != other.unsent) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + senderId.hashCode()
        result = 31 * result + eventID.hashCode()
        result = 31 * result + Arrays.hashCode(users)
        result = 31 * result + senderNickname.hashCode()
        result = 31 * result + mills.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + informative.hashCode()
        result = 31 * result + execute.hashCode()
        return result
    }
}