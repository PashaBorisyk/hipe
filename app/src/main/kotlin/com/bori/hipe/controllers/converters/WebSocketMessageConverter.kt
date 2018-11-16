package com.bori.hipe.controllers.converters


import android.util.Log
import com.bori.hipe.models.ChatMessageNOSQL

import com.google.gson.GsonBuilder

object WebSocketMessageConverter {

    val TAG = "WebSocketConverter"

    fun convertToMessage(jsonString: String): ChatMessageNOSQL {
        Log.d(TAG, "WebSocketMessageConverter.convertToMessage")

        return GsonBuilder().create().fromJson<ChatMessageNOSQL>(jsonString, ChatMessageNOSQL::class.java)
    }

    fun convertToJsonString(chatMessage: ChatMessageNOSQL): String {
        Log.d(TAG, "WebSocketMessageConverter.convertToJsonString")

        val jsonString = GsonBuilder().create().toJson(chatMessage)
        Log.e(TAG, "convertToJsonString: " + jsonString)
        return jsonString

    }

}